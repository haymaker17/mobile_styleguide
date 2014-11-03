//
//  ReceiptDownloader.m
//  ConcurMobile
//
//  Created by charlottef on 11/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "ReceiptDownloader.h"
#import "ReceiptCache.h"
#import "EntityReceipt.h"
#import "ReceiptManager.h"
#import "GetReceiptUrl.h"

@interface ReceiptCache (private)
-(void) initWithReceiptId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url delegate:(id<ReceiptDownloaderDelegate>)delegate;
-(void) download;
+(NSMutableDictionary*) currentDownloads;
+(NSString*) getTrackingKeyForReceiptId:(NSString*)receiptId thumbNail:(BOOL)thumbNail;
-(void) startTracking;
-(void) stopTracking;
@end

@implementation ReceiptDownloader

static NSMutableDictionary *_currentDownloads;

@synthesize delegate = _delegate;
@synthesize receiptId, url, dataType, thumbNail, creationDate;

+(NSMutableDictionary*) currentDownloads
{
    if (_currentDownloads != nil)
    {
        return _currentDownloads;
    }
    else
    {
        @synchronized (self)
        {
            if (_currentDownloads == nil)
            {
                _currentDownloads = [[NSMutableDictionary alloc] init];
            }
        }
        return _currentDownloads;
    }
}

// Return value indicates whether the download will be attempted
+(BOOL) downloadReceiptForId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url delegate:(id<ReceiptDownloaderDelegate>)delegate
{
    return [self downloadReceiptForId:receiptId dataType:dataType thumbNail:thumbNail url:url delegate:delegate useHttpPost:NO];
}

+(BOOL) downloadReceiptForId:(NSString*)receiptId dataType:(NSString*)dataType thumbNail:(BOOL)thumbNail url:(NSString*)url delegate:(id<ReceiptDownloaderDelegate>)delegate useHttpPost:(BOOL)usePost
{
    // If we're offline, then just fail
    if (![ExSystem connectedToNetwork])
    {
        return NO;
    }

    // Check if we're already downloading this receipt
    NSString *key = [ReceiptDownloader getTrackingKeyForReceiptId:receiptId thumbNail:thumbNail];
    ReceiptDownloader *trackedInstance = [ReceiptDownloader currentDownloads][key];
    if (trackedInstance != nil)
    {
        NSDate *now = [NSDate date];
        double ageOfTrackedInstanceInSeconds = [now timeIntervalSinceDate:trackedInstance.creationDate];

        // If we're already tracking a request which is less than a minute old and has the same url,
        // then just set its delgate to this one, and do not send out another request.
        if (ageOfTrackedInstanceInSeconds < 60 && [url isEqualToString:trackedInstance.url])
        {
            trackedInstance.delegate = delegate;
            return YES;
        }

        // Stop tracking the old request.  We're going to create a new one.
        [trackedInstance stopTracking];
    }

    // Start downloading the receipt
    ReceiptDownloader *downloader = [[ReceiptDownloader alloc] initWithReceiptId:receiptId dataType:dataType thumbNail:thumbNail url:url delegate:delegate];
    downloader.userHttpPost = usePost;
    // the urls expire, need to refetch them
    if (thumbNail)
    {
        [downloader download];
    }
    else
    {
        [downloader getReceiptUrlFromServer];
    }
    return YES;
}

-(id) initWithReceiptId:(NSString*)ReceiptId dataType:(NSString*)DataType thumbNail:(BOOL)ThumbNail url:(NSString*)Url delegate:(id<ReceiptDownloaderDelegate>)Delegate
{
    self = [super init];
	if (self)
    {
        self.receiptId = ReceiptId;
        self.dataType = DataType;
        self.thumbNail = ThumbNail;
        self.url = Url;
        self.delegate = Delegate;
        self.creationDate = [NSDate date];
        [self startTracking];
    }
    return self;
}

/**
 Need to ask for the receipt url cause urls expire every 30 mins... Why do you do this to me imaging team?
 */
- (void)getReceiptUrlFromServer
{
    // For report entry receipts
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReceiptImageUrl/%@", [ExSystem sharedInstance].entitySettings.uri, self.receiptId];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",nil];
    [[ExSystem sharedInstance].msgControl createMsg:GET_RECEIPT_URL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}

-(void) download
{
    // MOB-12257 Report PDF uses POST, and receipt store images uses GET
    if (self.userHttpPost)
    {
        [RequestController retrieveReportPDFImageFromUrlNoCaching:self.url MsgId:@"RECEIPT_IMAGE" SessionID:[ExSystem sharedInstance].sessionID MVC:self ParameterBag:nil];
    }
    else
    {
        [RequestController retrieveImageFromUrl:self.url MsgId:@"RECEIPT_IMAGE" SessionID:[ExSystem sharedInstance].sessionID MVC:self ParameterBag:nil];
    }

}

-(void) recordDownloadedReceiptWithImageId:(NSString*)imageId withFilePath:(NSString*) filePath
{
    EntityReceipt *entity = nil;
    NSString * rsKey = [NSString stringWithFormat:@"RS_KEY_%@",imageId];

    if ([[ReceiptManager sharedInstance] hasAnyReceipt])
    {
        entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:rsKey];
        if (entity != nil)
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            //            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",rsKey]];
        }
    }

    entity = (EntityReceipt *)[[ReceiptManager sharedInstance] makeNew];

    entity.key = rsKey;
    entity.imageID = imageId;
    entity.fullscreenImageLocalPath = filePath; // file path only used for report PDF files, nil for receipt store receipts
    entity.dateCreated = [NSDate date];
    entity.dateLastModified  = [NSDate date];
    entity.fullscreenUrl = nil;
    entity.thumbUrl = nil;
    entity.tag = nil;
    entity.type = RECEIPT_TYPE_RECEIPT_STORE;

    [[ReceiptManager sharedInstance] saveReceipt];
}

- (void)handleURLMessage:(Msg *)msg
{
    GetReceiptUrl* rUrl = (GetReceiptUrl *)msg.responder;
    if (msg.errBody != nil || ![rUrl.status isEqualToString:@"SUCCESS"])
    {
        ALog(@"Failed to get the receipt URL for id:%@", self.receiptId);
    } else {
        if (rUrl != nil && rUrl.receiptUrl != nil)
        {
            self.url = rUrl.receiptUrl;
        }
    }

    // regardless of whether we updated the url, try to download it
    [self download];
}

- (void)handleReceiptMessage:(Msg *)msg
{
    // Remove self from list of downloading receipts

    if (!msg.didConnectionFail && msg.responseCode >= 200 && msg.responseCode <= 299)
    {
        // Correct receipt data type according to contentType
        if ([msg.contentType isEqualToString:MIME_TYPE_PDF])
        {
            self.dataType = PDF;
        }
        else if ([msg.contentType isEqualToString:@"text/html;charset=ISO-8859-1"])
        {
            self.dataType = @""; // MOB-13007 No receipt, imaging server return html text
        }

        if (self.dataType == nil)
            self.dataType = @"";

        NSData *receiptData = msg.data;
        if (receiptData != nil)
        {
            // Write the receipt to the cache
            ReceiptCache *receiptCache = [ReceiptCache sharedInstance];
            if (self.thumbNail)
                [receiptCache cacheThumbNailReceiptData:receiptData dataType:self.dataType receiptId:self.receiptId];
            else
            {
                if ([self.dataType length])
                {
                    // MOB-13007 cache and record in core data valid downloaded receipts
                    [self recordDownloadedReceiptWithImageId:self.receiptId withFilePath:nil];
                    [receiptCache cacheFullSizeReceiptData:receiptData dataType:self.dataType receiptId:self.receiptId];
                }
            }
            // Notify the delegate
            [self.delegate didDownloadReceiptId:self.receiptId dataType:self.dataType thumbNail:self.thumbNail url:self.url];
        }
    }
    else
    {
        [self.delegate didFailToDownloadReceiptId:self.receiptId dataType:self.dataType thumbNail:self.thumbNail url:self.url];
    }

    [self stopTracking];
}

-(void) didProcessMessage:(Msg *)msg
{
    if ([msg.idKey isEqualToString:GET_RECEIPT_URL]) {
        [self handleURLMessage:msg];
    }

	if ([msg.idKey isEqualToString:@"RECEIPT_IMAGE"])
	{
        [self handleReceiptMessage:msg];
 	}
}

#pragma mark - Tracking Methods

+(NSString*) getTrackingKeyForReceiptId:(NSString*)receiptId thumbNail:(BOOL)thumbNail
{
    NSString *key = [NSString stringWithFormat:@"%@%@", receiptId, (thumbNail ? @"_THUMBNAIL" : @"")];
    return key;
}

-(void) startTracking
{
    NSString *key = [ReceiptDownloader getTrackingKeyForReceiptId:self.receiptId thumbNail:self.thumbNail];
    [ReceiptDownloader currentDownloads][key] = self;

    // TODO: purge any old stuff out of the dictionary so it doesn't grow too large
}

-(void) stopTracking
{
    NSString *key = [ReceiptDownloader getTrackingKeyForReceiptId:self.receiptId thumbNail:self.thumbNail];
    ReceiptDownloader *trackedInstance = [ReceiptDownloader currentDownloads][key];
    if (trackedInstance == self)
        [[ReceiptDownloader currentDownloads] removeObjectForKey:key];
}

@end
