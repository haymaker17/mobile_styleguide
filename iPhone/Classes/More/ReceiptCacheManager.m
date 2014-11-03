//
//  ReceiptCacheManager.m
//  ConcurMobile
//
//  Created by yiwen on 11/23/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "ReceiptCacheManager.h"
#import "EntityReportReceiptInfo.h"
#import "ImageUtil.h"
#import "GetReceiptUrl.h"
#import "ReportReceiptInfoManager.h"
#import "ExReceiptManager.h"

static ReceiptCacheManager *sharedInstance;

@implementation ReceiptCacheManager
@synthesize queue, vcs, receiptsForVc;

+(ReceiptCacheManager*)sharedInstance
{
 	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[ReceiptCacheManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(id) init
{
    self = [super init];
    if (self)
    {
        queue = [[NSMutableArray alloc] init];
        vcs = [[NSMutableArray alloc] init];
        receiptsForVc = [[NSMutableArray alloc] init];
    }
    return self;
}


#pragma mark -
#pragma mark Utility Methods 
-(EntityReportReceiptInfo*) nextReceipt
{
    EntityReportReceiptInfo* result = nil;
    
    if ([queue count]>0)
    {
        result = queue[0];
        [queue removeObject:result];
    }
    return result;
}

-(void) removeFromQueue:(EntityReportReceiptInfo*) rInfo
{
    EntityReportReceiptInfo* objectToRemove = nil;
    for(EntityReportReceiptInfo* rInfo2 in queue)
    {
        if ([rInfo.rpeKey isEqualToString:rInfo2.rpeKey])
        {
            objectToRemove = rInfo2;
            break;
        }
    }
    [queue removeObject:objectToRemove];
}

-(BOOL) isSameRInfo:(EntityReportReceiptInfo*)rInfo1 with:(EntityReportReceiptInfo*) rInfo2
{
    return [rInfo1.rpeKey isEqualToString:rInfo2.rpeKey];
}

-(int) getVcIndex:(NSObject*) vc
{
    for (int ix = 0; ix<[vcs count]; ix++)
    {
        NSObject* eVc = vcs[ix];
        if (eVc == vc)
            return ix;
    }
    return -1;
}

-(BOOL) isReportDone:(ReportData*) rpt withVc:(NSObject*) vc
{
    int ix = [self getVcIndex:vc];
    return ix == -1;
}

-(void) addToQueue:(NSMutableArray*) rInfos withVc:(NSObject*) vc
{
    int ix = [self getVcIndex:vc];
    if (ix == -1)
    {
        [vcs addObject:vc];
        [receiptsForVc addObject:rInfos];
    }
    else
        receiptsForVc[ix] = rInfos;
    
    for (EntityReportReceiptInfo* rInfo in rInfos)
    {
        EntityReportReceiptInfo* old = nil;
        for (EntityReportReceiptInfo* rInfo2 in queue)
        {
            if ([self isSameRInfo:rInfo with:rInfo2])
            {
                old = rInfo2;
                break;
            }
        }
        
        if (old == nil)
            [queue addObject:rInfo];
    }
}

-(void) processNextReceipt
{
    if ([queue count]>0)
    {
        EntityReportReceiptInfo* rInfo = [self nextReceipt];
        
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"####Download receipt for Export %@", rInfo.rpeKey] Level:MC_LOG_INFO];
        // For report entry receipts
        NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReceiptImageUrl/%@", [ExSystem sharedInstance].entitySettings.uri, rInfo.receiptId];
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL", rInfo, @"RINFO", nil];
        [[ExSystem sharedInstance].msgControl createMsg:GET_RECEIPT_URL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}
// Remove the rInfo from vcToReceiptsMap
-(void) doneDownload:(EntityReportReceiptInfo*)rInfo
{
    int ixVcToRemove = -1;
    for (int ix = 0; ix< [vcs count]; ix++)
    {
        EntityReportReceiptInfo* objectToRemove = nil;
        NSMutableArray* rInfoList2 = receiptsForVc[ix];
        for (EntityReportReceiptInfo* rInfo2 in rInfoList2)
        {
            // If there is overlap, put the item in doNotRemove list
            if ([rInfo.rpeKey isEqualToString:rInfo2.rpeKey])
            {
                objectToRemove = rInfo2;
                break;
            }
        }
        
        [rInfoList2 removeObject:objectToRemove];
        if ([rInfoList2 count] ==0)
            ixVcToRemove = ix;
    }
    
    if (ixVcToRemove>-1)
    {
        [vcs removeObjectAtIndex:ixVcToRemove];
        [receiptsForVc removeObjectAtIndex:ixVcToRemove];
    }
}

-(BOOL) isCacheOutOfDate:(EntityReportReceiptInfo*)rInfo
{
    if (rInfo == nil || rInfo.dateLastModified == nil)
        return YES;
    
//    NSLog(@"Timeinterval %f", [rInfo.dateLastModified timeIntervalSinceNow]);
    if ([rInfo.dateLastModified timeIntervalSinceNow] < -600) // 10 minutes
        return YES;
    return NO;
}

-(NSString*) saveImage:(UIImage*) image forRpe:(NSString*)rpeKey
{
    NSString* relPath = [NSString stringWithFormat:@"SUCache_RPE_%@.png",rpeKey];
    [ImageUtil saveReceiptImageToDocumentsFolder:image ImageName:relPath];
//    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ReceiptCacheManager:store receipt at %@", relPath] Level:MC_LOG_DEBU];
    return relPath;
}

#pragma mark -
#pragma mark MsgResponder Methods 
-(void) didProcessMessage:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"OOPE_IMAGE"]) 
	{
        EntityReportReceiptInfo* rInfo = (msg.parameterBag)[@"RINFO"];
        if (msg.errBody == nil && ([msg.errCode isEqualToString:@"200"] || msg.errCode == nil)) 
        {
            if ([msg.contentType isEqualToString:@"application/pdf"]) 
            {
                //                isMimeTypePDF = YES;
                //                self.pdfData = [NSMutableData dataWithData:msg.data];
                //                msg.data = nil;
                //                [[ExReceiptManager sharedInstance] cacheReportPDFReceipt:msg.data];
            }
            else 
            {
                NSData *mydata = msg.data; 
                if (mydata != nil) 
                {
                    UIImage *img = [[UIImage alloc] initWithData:mydata];
                    
                    // Write img and update entity
                    /*NSFileManager *filemgr = [NSFileManager defaultManager];
                    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
                    NSString *documentsDirectory = [paths objectAtIndex:0];
                    NSString *SUCachePath = [documentsDirectory stringByAppendingPathComponent:@"SUCache"];

                    BOOL isDir;
                    if (!([filemgr fileExistsAtPath:SUCachePath isDirectory:&isDir] && isDir))
                    {
                        [filemgr createDirectoryAtPath:SUCachePath withIntermediateDirectories:YES attributes:nil error:nil];
                    }*/

                    NSString* relPath = [self saveImage:img forRpe:rInfo.rpeKey];
                    rInfo.imagePath = relPath;
                    rInfo.dateLastModified = [NSDate date];
                    [[ReportReceiptInfoManager sharedInstance] saveIt:rInfo];
                    
                    [self doneDownload:rInfo];
                }
            }
        }
        
        // Move on
        [self processNextReceipt];
        
    }
    else if ([msg.idKey isEqualToString:GET_RECEIPT_URL]) 
	{
        GetReceiptUrl* rUrl = (GetReceiptUrl*)msg.responder;
        if (msg.errBody != nil || ![rUrl.status isEqualToString:@"SUCCESS"]) 
        {
            // Move on
            [self processNextReceipt];
        }
        else
        {
            if (rUrl != nil && rUrl.receiptUrl != nil) 
            {
                [RequestController retrieveImageFromUrl:rUrl.receiptUrl MsgId:@"OOPE_IMAGE" SessionID:[ExSystem sharedInstance].sessionID MVC:self ParameterBag:msg.parameterBag];	
            }	
        }
		
	}
    
}

#pragma mark -
#pragma mark Public Methods 
-(void) retrieveReportReceipts:(ReportData*)rpt forObject:(NSObject*)vc
{
    BOOL queueWasEmpty = [queue count] ==0;
    
    NSMutableArray* rInfoList = [[NSMutableArray alloc] init];
    NSEnumerator *enumerator = [rpt.entries keyEnumerator];
    NSString* key = nil;
    while (key = [enumerator nextObject])
    {
        EntryData* rpeData = (rpt.entries)[key];
        if ([rpeData.receiptImageId length])
        {
            EntityReportReceiptInfo* entity = [[ReportReceiptInfoManager sharedInstance] fetchOrMake:rpeData.rpeKey withRptKey:rpeData.rptKey];
            if (![rpeData.receiptImageId isEqualToString:entity.receiptId])
            {
                entity.receiptId = rpeData.receiptImageId;
                entity.dateLastModified = nil;
                entity.imagePath = nil;
                [[ReportReceiptInfoManager sharedInstance] saveIt:entity];
            }
            if ([self isCacheOutOfDate:entity])
            {
                [[MCLogging getInstance] log:[NSString stringWithFormat:@"ReceiptCacheManager: load receipt image for rpe %@", entity.rpeKey] Level:MC_LOG_DEBU];

                [rInfoList addObject:entity];
            }
        }
    }
    
    //MOB-8986 Do not add vc, when there is no receipt to download, otherwise, the vc just stays in vcs list.
    if ([rInfoList count]>0)
    {
        [self addToQueue:rInfoList withVc:vc];
    
        if (queueWasEmpty)
            [self processNextReceipt];
    }
}

-(void) cancelReceiptsRetrieval:(NSObject*)vc
{
    int ixVcToRemove = [self getVcIndex:vc];

    if (ixVcToRemove>-1)
    {

        NSMutableArray* rInfoList = receiptsForVc[ixVcToRemove];
        [vcs removeObjectAtIndex:ixVcToRemove];
        [receiptsForVc removeObjectAtIndex:ixVcToRemove];
        
        NSMutableDictionary* doNotRemove = [[NSMutableDictionary alloc] init];
        if (rInfoList != nil && [rInfoList count] >0)
        {
            for (int ix = 0; ix< [vcs count]; ix++)
            {
                NSMutableArray* rInfoList2 = receiptsForVc[ix];
                for(EntityReportReceiptInfo* rInfo in rInfoList)
                {
                    for (EntityReportReceiptInfo* rInfo2 in rInfoList2)
                    {
                        // If there is overlap, put the item in doNotRemove list
                        if ([rInfo.rpeKey isEqualToString:rInfo2.rpeKey])
                        {
                            [Flurry logError:@"ReciptCacheManager doNotRemove[rInfo]" message:nil error:[NSError errorWithDomain:@"tmp" code:1 userInfo:@{@"rInfo":rInfo}]];
                            doNotRemove[rInfo] = rInfo;
                            break;
                        }
                    }
                }
            }
            
            for(EntityReportReceiptInfo* rInfo in rInfoList)
            {
                if (doNotRemove[rInfo]== nil)
                {
                    // remove from queue
                    [self removeFromQueue:rInfo];
                }
            }
        }
    
    }

}

// TODO
-(void) entryReceiptDownloaded:(EntryData*)rpe
{
    
}

-(void) entryReceiptUpdated:(EntryData*)rpe
{
    if (![rpe.rpeKey length])
        return;
    
    UIImage *cachedReceiptImage = [[ExReceiptManager sharedInstance] getCachedReceiptImageForReportEntry:rpe.rpeKey];
    
    EntityReportReceiptInfo* rInfo = [[ReportReceiptInfoManager sharedInstance] fetchOrMake:rpe.rpeKey withRptKey:rpe.rptKey];
    NSString* relPath = [self saveImage:cachedReceiptImage forRpe:rInfo.rpeKey];
    rInfo.imagePath = relPath;
    rInfo.receiptId = rpe.receiptImageId;
    rInfo.dateLastModified = [NSDate date];
    [[ReportReceiptInfoManager sharedInstance] saveIt:rInfo];
}

-(void) clearReportReceipts:(NSString*) rptKey
{
    
}

-(NSArray*) getReportReceiptInfos:(NSString*) rptKey // Array of EntityReportReceiptInfo
{
    return [[ReportReceiptInfoManager sharedInstance] getEntryReceiptInfoForRpt:rptKey];                             
}
@end
