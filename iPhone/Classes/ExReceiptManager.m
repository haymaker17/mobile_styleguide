//
//  ReceiptManager.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ExReceiptManager.h"
#import "UploadReceiptData.h"
#import "ReceiptImageMetaData.h"
#import "ImageUtil.h"
#import "SaveReportEntryReceipt.h"
#import "DataConstants.h"
#import "ExSystem.h" 
#import "AppendReceiptData.h"

#define MAX_CACHING_INTERVAL 7200.0f
static ExReceiptManager *sharedInstance;

@interface ExReceiptManager (Private)
-(UIImage*) getCachedReceiptImageForKey:(NSString*)key;
@end

@implementation ExReceiptManager
@synthesize delegate;
@synthesize isReceiptStore,data,currentReceiptUploadType,role,networkCallInProgress,bypass,isNewExpenseEntry;

enum 
{
	ReportReceipt = 0, 
	ReportEntryReceipt = 1,
	MobileEntryReceipt = 2, 
	ReceiptStoreReceipt = 3
} ReceiptUploadType;


-(NSString*) getViewIDKey
{
	return EXPENSE_RECEIPT_MANAGER;
}


+(ExReceiptManager*) sharedInstance
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
				sharedInstance = [[ExReceiptManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(ExReceiptManager*) init
{
    self = [super init];
	if (self)
	{
        self.role = nil;
		self.data = [[ExpenseReceiptData alloc] init];
        self.delegate = nil;
		self.isReceiptStore = NO;
		self.currentReceiptUploadType = -1;
        self.bypass = NO;
    }
   return self;
}

-(void) configureReceiptManagerForDelegate:(id)viewController 
andInvoiceKey:(NSString *)key
{
    [self reset];
    self.delegate = viewController;
    self.data.invoiceKey = key;
}

   
-(void) configureReceiptManagerForDelegate:(id)viewController 
isNewExpenseEntry:(BOOL)isNew
{
    [self reset];
    self.delegate = viewController;
    self.role = nil;
    self.isReceiptStore = NO; 
    self.networkCallInProgress = NO;
    self.isNewExpenseEntry = YES;
    /* If bypass is YES, then ReceiptDetailViewController, will not use ExReceiptManager to save receipt, but let the QEFormVC handle it. For example, on a new Quick Expense Form, the self.entry is nil and there is no meKey or in case of new line item, there is no rpeKey to save the new entry receipt. So set bypass to YES and let the QEFormVC/ReportEntryViewController save the receipt upon saving the form. 
     */
    self.bypass = YES; 
}


-(void) configureReceiptManagerForDelegate:(id)viewController 
                            andReportEntry:(EntryData*)rptEntry 
                              andReporData:(ReportData*)rptData 
                           andExpenseEntry:(OOPEntry*)expEntry
                                   andRole:(NSString*)userRole
{
    [self reset];
    self.delegate = viewController;
    self.data.reportData = rptData;
    self.data.reportEntry = rptEntry;
    self.data.expenseEntry = expEntry;
    self.role = userRole;
    self.isReceiptStore = NO; 
    self.networkCallInProgress = NO;
    
    /* If bypass is YES, then ReceiptDetailViewController, will not use ExReceiptManager to save receipt, but let the QEFormVC handle it. For example, on a new Quick Expense Form, the self.entry is nil and there is no meKey or in case of new line item, there is no rpeKey to save the new entry receipt. So set bypass to YES and let the QEFormVC/ReportEntryViewController save the receipt upon saving the form. 
     */
    self.bypass = NO; 
}

-(void) reset
{
    self.delegate = nil;

    self.data = [[ExpenseReceiptData alloc] init];

//    self.data.reportData = nil;
//    self.data.reportEntry = nil;
//    self.data.expenseEntry = nil;
    self.role = nil;
    self.currentReceiptUploadType = -1;
    
    if (networkCallInProgress) {
        // Cancel any request sent by ExReceiptManager instance if already in progress
        [MsgHandler cancelAllRequestsForDelegate:self];
    }
    
    self.networkCallInProgress = NO;    
}


-(int) getReceiptUploadType
{
    if (data.reportEntry != nil && data.expenseEntry == nil) 
    {
        currentReceiptUploadType = ReportEntryReceipt;
    }
    else if (data.expenseEntry == nil && data.reportData != nil && data.reportEntry == nil)
    {
        currentReceiptUploadType = ReportReceipt;
    }
    else if (data.expenseEntry != nil && data.reportEntry == nil && data.reportData == nil) 
    {
        currentReceiptUploadType = MobileEntryReceipt;
    }
    else if (isNewExpenseEntry)
    {
        currentReceiptUploadType = MobileEntryReceipt;
    }
    else 
    {
        currentReceiptUploadType = ReceiptStoreReceipt;
    }
	
	return currentReceiptUploadType;
}

#pragma mark -
#pragma mark Receipt caching methods using Core Data
-(void) removeFromReceiptStoreCache:(NSString*)key
{
    EntityReceipt *entity = nil;
    if ([[ReceiptManager sharedInstance] hasAnyReceipt])
    {
        entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:[NSString stringWithFormat:@"RS_KEY_%@",key]];
        if (entity != nil) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",key]];
        }        
    }
}

-(void) cacheReceipt
{
	switch ([self getReceiptUploadType])
	{
		case ReportEntryReceipt:
		{
			[self cacheReportEntryReceipt];
		}
            break;
		case ReportReceipt:
		{
            // Receipts attached at the report level are not cached (Size of PDF is large).
		}			
            break;
		case MobileEntryReceipt:
		{
            [self cacheMobileEntryReceipt];
		}
            break;
        case ReceiptStoreReceipt:
		{
            [self cacheReceiptStoreReceiptImage:data.receiptImageForCaching withImageId:data.receiptImageId];
		}
            break;
		default:
            break;
	}
}


-(void) cacheMobileEntryReceipt
{
    EntityReceipt *entity = nil;
    NSString *meKey = [NSString stringWithFormat:@"ME_KEY_%@",data.expenseEntry.meKey];
    
    if ([[ReceiptManager sharedInstance] hasAnyReceipt])
    {
        entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:meKey];
        if (entity != nil) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",meKey]];
        }        
    }
    
    entity = (EntityReceipt *)[[ReceiptManager sharedInstance] makeNew];
    
    entity.key = meKey;
    entity.imageID = data.expenseEntry.receiptImageId; 
    entity.fullscreenImageLocalPath = [ImageUtil saveReceiptImageToDocumentsFolder:data.expenseEntry.receiptImage ImageName:[NSString stringWithFormat:@"%@.png",meKey]];
    entity.dateCreated = [NSDate date];
    entity.dateLastModified  = [NSDate date];
    entity.fullscreenUrl = nil;
    entity.thumbUrl = nil;
    entity.tag = nil;
    entity.type = RECEIPT_TYPE_MOBILE_ENTRY_RECEIPT;
    
    [[ReceiptManager sharedInstance] saveReceipt];
}

-(void) cacheReportEntryReceipt
{
    EntityReceipt *entity = nil;
    NSString *key = [NSString stringWithFormat:@"RPE_KEY_%@",data.reportEntry.rpeKey];
    if ([[ReceiptManager sharedInstance] hasAnyReceipt])
    {
        entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:key];
        if (entity != nil) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",key]];
        }        
    }
    
    if (isReceiptStore) 
    {
        self.data.reportEntry.receiptImage = [self fetchCachedReceiptForImageId:self.data.reportEntry.receiptImageId];
    }
    
    entity = (EntityReceipt *)[[ReceiptManager sharedInstance] makeNew];
    
    entity.key = key;
    entity.imageID = data.reportEntry.receiptImageId;
    entity.fullscreenImageLocalPath = [ImageUtil saveReceiptImageToDocumentsFolder:data.reportEntry.receiptImage ImageName:[NSString stringWithFormat:@"%@.png",key]];
    entity.dateCreated = [NSDate date];
    entity.dateLastModified  = [NSDate date];
    entity.fullscreenUrl = nil;
    entity.thumbUrl = nil;
    entity.tag = nil;
    entity.type = RECEIPT_TYPE_REPORT_ENTRY_RECEIPT;
    
    [[ReceiptManager sharedInstance] saveReceipt];
}

-(void) cacheInvoiceReceipt:(UIImage *)img
{
    EntityReceipt *entity = nil;
    NSString *key = [NSString stringWithFormat:@"INV_KEY_%@",data.invoiceKey];
    if ([[ReceiptManager sharedInstance] hasAnyReceipt])
    {
        entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:key];
        if (entity != nil) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",key]];
        }        
    }
    
    entity = (EntityReceipt *)[[ReceiptManager sharedInstance] makeNew];
    
    entity.key = key;
    entity.imageID = data.reportEntry.receiptImageId;
    entity.fullscreenImageLocalPath = [ImageUtil saveReceiptImageToDocumentsFolder:img ImageName:[NSString stringWithFormat:@"%@.png",key]];
    entity.dateCreated = [NSDate date];
    entity.dateLastModified  = [NSDate date];
    entity.fullscreenUrl = nil;
    entity.thumbUrl = nil;
    entity.tag = nil;
    entity.type = RECEIPT_TYPE_INVOICE_RECEIPT;
    
    [[ReceiptManager sharedInstance] saveReceipt];
}


-(void) cacheReportPDFReceipt:(NSData*)pdfData
{
    EntityReceipt *entity = nil;
    NSString *key = [NSString stringWithFormat:@"RPT_KEY_%@",data.reportData.rptKey];
    if ([[ReceiptManager sharedInstance] hasAnyReceipt])
    {
        entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:key];
        if (entity != nil) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.pdf",key]];
        }        
    }
        
    entity = (EntityReceipt *)[[ReceiptManager sharedInstance] makeNew];
    
    entity.key = key;
    entity.imageID = nil;
    entity.fullscreenImageLocalPath = [ImageUtil saveReceiptDataToDocumentsFolder:pdfData ImageName:[NSString stringWithFormat:@"%@.pdf",key]];
    entity.dateCreated = [NSDate date];
    entity.dateLastModified  = [NSDate date];
    entity.fullscreenUrl = nil;
    entity.thumbUrl = nil;
    entity.tag = nil;
    entity.type = RECEIPT_TYPE_REPORT_RECEIPT;
    
    [[ReceiptManager sharedInstance] saveReceipt];
}

-(void) cacheReceiptStoreReceiptImage:(UIImage*)img withImageId:(NSString*)imageId
{
    EntityReceipt *entity = nil;
    NSString * rsKey = [NSString stringWithFormat:@"RS_KEY_%@",imageId];
    
    if ([[ReceiptManager sharedInstance] hasAnyReceipt])
    {
        entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:rsKey];
        if (entity != nil) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",rsKey]];
        }        
    }
    
    entity = (EntityReceipt *)[[ReceiptManager sharedInstance] makeNew];
    
    entity.key = rsKey;
    entity.imageID = data.reportEntry.receiptImageId;
    entity.fullscreenImageLocalPath = [ImageUtil saveReceiptImageToDocumentsFolder:img ImageName:[NSString stringWithFormat:@"%@.png",rsKey]];
    entity.dateCreated = [NSDate date];
    entity.dateLastModified  = [NSDate date];    
    entity.fullscreenUrl = nil;
    entity.thumbUrl = nil;
    entity.tag = nil;
    entity.type = RECEIPT_TYPE_RECEIPT_STORE;
    
    [[ReceiptManager sharedInstance] saveReceipt];
}

-(void) updateReportEntryImage:(UIImage*)img
{
    self.data.reportEntry.receiptImage = img;    
    [self cacheReportEntryReceipt];
}

-(void) updateMobileEntryImage:(UIImage*)img
{
    self.data.expenseEntry.receiptImage = img; 
    
    if (img == nil) {
        self.data.expenseEntry.hasReceipt = @"N";
//        [self deleteCachedReceipt];
    }
    else
    {
        self.data.expenseEntry.hasReceipt = @"Y";
//        [self cacheMobileEntryReceipt];
    }
}

#pragma mark Receipt Fetch/Upload/Save
-(void) deleteCachedReceipt
{
    NSString *key = nil;
    
    switch ([self getReceiptUploadType])
	{
        case ReportEntryReceipt:
        {            
            key = [NSString stringWithFormat:@"RPE_KEY_%@",data.reportEntry.rpeKey];

		}
            break;
        case MobileEntryReceipt:
        {
            key = [NSString stringWithFormat:@"ME_KEY_%@",data.expenseEntry.meKey];
        }
            break;            
		default:
			break;
    }    
    
    if(key!= nil)
    {
        EntityReceipt *entity = (EntityReceipt *)[[ReceiptManager sharedInstance] fetchReceipt:key];
        if (entity != nil) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",key]];
        }
    }
}

-(UIImage*) fetchCachedReceipt
{
    switch ([self getReceiptUploadType])
	{
        case ReportEntryReceipt:
        {            
            NSString *key = [NSString stringWithFormat:@"RPE_KEY_%@",data.reportEntry.rpeKey];
            UIImage *cachedReceiptImage = (UIImage*)[self getCachedReceiptImageForKey:key];
            return cachedReceiptImage;
		}
        break;
        case ReportReceipt:
            // Caching not supported for PDFs.
            return nil;
            break;
        case MobileEntryReceipt:
        {
            NSString *key = [NSString stringWithFormat:@"ME_KEY_%@",data.expenseEntry.meKey];
            UIImage *cachedReceiptImage = (UIImage*)[self getCachedReceiptImageForKey:key];
            return cachedReceiptImage;
        }
            break;            
		default:
            return nil;
			break;
    }
}


-(UIImage *)getCachedInvoiceReceiptImage
{
    NSString *key = [NSString stringWithFormat:@"INV_KEY_%@",data.invoiceKey];
    EntityReceipt *entity = nil;
    if ((entity = (EntityReceipt*)[[ReceiptManager sharedInstance] fetchReceipt:key]) != nil)
    {
        NSDate *timeNow = [NSDate date];
        if ([timeNow timeIntervalSinceDate:entity.dateLastModified] > MAX_CACHING_INTERVAL) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",key]];
            return nil;
        }
        else
        {
            UIImage *img = [[UIImage alloc] initWithContentsOfFile:entity.fullscreenImageLocalPath];                             
            return img;
        }
    }
    else
    {
        return nil;
    }
}

-(UIImage*) getCachedReceiptImageForKey:(NSString*)key
{
    EntityReceipt *entity = nil;
    if ((entity = (EntityReceipt*)[[ReceiptManager sharedInstance] fetchReceipt:key]) != nil)
    {
        NSDate *timeNow = [NSDate date];
        if ([timeNow timeIntervalSinceDate:entity.dateLastModified] > MAX_CACHING_INTERVAL) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.png",key]];
            return nil;
        }
        else
        {
            UIImage *img = [[UIImage alloc] initWithContentsOfFile:entity.fullscreenImageLocalPath];                             
            return img;
        }
    }
    else
    {
        return nil;
    }
}


-(NSMutableData*) getCachedReceiptPDFForKey:(NSString*)key
{
    EntityReceipt *entity = nil;
    NSString *rptkey = [NSString stringWithFormat:@"RPT_KEY_%@",data.reportData.rptKey];
    if ((entity = (EntityReceipt*)[[ReceiptManager sharedInstance] fetchReceipt:rptkey]) != nil)
    {
        NSDate *timeNow = [NSDate date];
        if ([timeNow timeIntervalSinceDate:entity.dateLastModified] > MAX_CACHING_INTERVAL) 
        {
            [[ReceiptManager sharedInstance] deleteObj:entity];
            [ImageUtil killImageFromDocumentsFolder:[NSString stringWithFormat:@"%@.pdf",rptkey]];
            return nil;
        }
        else
        {
            NSMutableData *imgData = [NSMutableData dataWithContentsOfFile:entity.fullscreenImageLocalPath];
            return imgData;
        }
    }
    else
    {
        return nil;
    }
}

-(UIImage*) fetchCachedReceiptForImageId:(NSString*)imgId
{
    NSString *imageId = [NSString stringWithFormat:@"RS_KEY_%@",imgId];
    EntityReceipt *entity = nil;
    
    if ((entity = (EntityReceipt*)[[ReceiptManager sharedInstance] fetchReceipt:imageId]) != nil)
    {
        UIImage *img = [[UIImage alloc] initWithContentsOfFile:entity.fullscreenImageLocalPath];                             
        return img;
    }
    else
    {
        return nil;
    }
}

-(void) fetchOnlineReportEntryReceipt
{   
    if (data.reportEntry.receiptImageId != nil) 
    {
        [self getReceiptImageUrl];
    }
    else 
    {
        // Backward compatiblity : Receipt may have been an old receipt uploaded as a blob.
        NSString* imageURL = [NSString stringWithFormat:@"%@/Mobile/Expense/GetMobileEntryReceipt/%@/jpeg/Large/%@", 
                              [ExSystem sharedInstance].entitySettings.uri, data.reportEntry.meKey, data.reportData.rptKey];
        [RequestController retrieveImageFromUrlWithoutCaching:imageURL MsgId:@"OOPE_IMAGE" SessionID:[ExSystem sharedInstance].sessionID MVC:(MobileViewController*)self.delegate ParameterBag:nil];
    }
}

-(void) fetchOnlineReportReceipt
{   
    NSString *imageUrl = [NSString stringWithFormat:@"%@",data.reportData.realPdfUrl];
    [RequestController retrieveReportPDFImageFromUrlNoCaching:imageUrl MsgId:@"OOPE_IMAGE" SessionID:[ExSystem sharedInstance].sessionID MVC:(MobileViewController*)self.delegate ParameterBag:nil];
}

-(void) fetchOnlineMobileEntryReceipt
{
    if (data.expenseEntry.receiptImageId != nil) 
    {
        [self getReceiptImageUrl];
    }
    else 
    {
        NSString* imageURL = [NSString stringWithFormat:@"%@/Mobile/Expense/GetMobileEntryReceipt/%@/jpeg/Large", 
                              [ExSystem sharedInstance].entitySettings.uri, data.expenseEntry.meKey];
        [RequestController retrieveImageFromUrlWithoutCaching:imageURL MsgId:@"OOPE_IMAGE" SessionID:[ExSystem sharedInstance].sessionID MVC:(MobileViewController*)self.delegate ParameterBag:nil];  
    }
}

-(void) handleInvalidImageData
{
    NSDictionary *dict = @{@"Failure": @"Failed to capture or reduce resolution for receipt image"};
    [Flurry logEvent:@"Receipts: Failure" withParameters:dict];

    // MOB-9416 handle image conversion failure b/c memory shortage
    NSString *errMsg = [Localizer getLocalizedText:@"Free up memory and retry receipt upload"];
    
    UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Receipt upload failed"] 
                                                        message:errMsg 
                                                       delegate:nil 
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                              otherButtonTitles:
                          nil];
    [alert show];
    
    // return to calling view controller
    if (self.delegate != nil && [self.delegate respondsToSelector:@selector(handleReceiptFailure)])
    {
        [self.delegate handleReceiptFailure];
    }
}

-(NSData *) receiptImageDataFromReceiptImage:(UIImage*)receiptImage
{
    // MOB-10877 For iOS 6 only, receiptImage.imageOrientation is 3 - UIImageOrientationRight.
    if ([ExSystem is6Plus])
    {
        UIImage* correctedImage = nil;
        if(UIImageOrientationRight == [receiptImage imageOrientation])
        {
            correctedImage = [ImageUtil fixOrientation:receiptImage];
            if (correctedImage != nil)
                receiptImage = correctedImage;
        }
    }
    
    NSData *imgData = nil;
	if (receiptImage != nil)
	{
        imgData = UIImageJPEGRepresentation(receiptImage, 0.9f);
        //        imgData = nil;//###MOB-9416##test only
    }
    
    return imgData;
}

-(void) uploadReceipt:(UIImage*)receiptImage
{
    [self uploadReceipt:receiptImage appendToReceiptId:nil];  
}
// MOB-12019
-(void) uploadReceipt:(UIImage*)receiptImage appendToReceiptId:(NSString*)appendToReceiptId
{
    NSData *imgData = [self receiptImageDataFromReceiptImage:receiptImage];
    if (imgData == nil)
    {   // MOB-9416 validate image before uploading
        [self handleInvalidImageData];
    }
    else
    {
        // ###MOB-9414## Induce failure by inserting an 'x' on the url
		NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/SaveReceipt/%@", [ExSystem sharedInstance].entitySettings.uri, @"mobile"];
		NSMutableDictionary *pBag  = nil;
		networkCallInProgress = YES;
        
		switch ([self getReceiptUploadType])
		{
			case ReportEntryReceipt:
			{
// MOB-9414 do not set this until after success
//				data.reportEntry.receiptImage = receiptImage;
                self.data.receiptImageForCaching = receiptImage;  // TODO - conflict with ReportReceipt?
				pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",imgData, @"IMAGE", data.reportEntry,@"ENTRY",[self getViewIDKey], @"TO_VIEW",nil];
				//MOB-12019
                if (appendToReceiptId != nil)
                    pBag[@"APPEND_TO_RECEIPT_ID"] = appendToReceiptId;
				[[ExSystem sharedInstance].msgControl createMsg:UPLOAD_IMAGE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
			}
				break;
			case ReportReceipt:
			{
				self.data.receiptImageForCaching = receiptImage;
				pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",imgData,@"IMAGE",[self getViewIDKey], @"TO_VIEW",nil];
				[[ExSystem sharedInstance].msgControl createMsg:UPLOAD_IMAGE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
				
			}
				break;
			case ReceiptStoreReceipt:
			{
				pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",imgData,@"IMAGE",[self getViewIDKey], @"TO_VIEW",nil];
				[[ExSystem sharedInstance].msgControl createMsg:UPLOAD_IMAGE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:(MobileViewController*)self.delegate];
			}	
				break;
			case MobileEntryReceipt:
			{
                data.expenseEntry.receiptImage = receiptImage;
//				path = [NSString stringWithFormat:@"%@/mobile/Expense/SaveMobileEntryReceipt/%@", [ExSystem sharedInstance].entitySettings.uri, data.expenseEntry.meKey];
				pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",imgData,@"IMAGE",[self getViewIDKey], @"TO_VIEW",nil];
				[[ExSystem sharedInstance].msgControl createMsg:UPLOAD_IMAGE_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
			}	
				break;
			default:
                networkCallInProgress = NO;
				break;
		}
		
	}
}


-(void) saveReportEntryWithNewReceipt:(NSString*) receiptId appendToReceiptId:(NSString*)appendToReceiptId
{
//MOB-12019
    if (appendToReceiptId == nil)
    {
        NSDictionary *dict = @{@"Added To": @"Report Entry"};
        [Flurry logEvent:@"Receipts: Add" withParameters:dict];
        
        networkCallInProgress = YES;
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                     data.reportData, @"REPORT", 
                                     data.reportEntry, @"ENTRY",
                                     self.role, @"ROLE_CODE",
                                     nil];
        
        pBag[@"RECEIPT_IMAGE_ID"] = receiptId;
        [[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_ENTRY_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
    else
    {
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:
                                        receiptId, @"FROM_RECEIPT_IMAGE_ID",
                                        appendToReceiptId, @"TO_RECEIPT_IMAGE_ID",
                                        nil];

        [[ExSystem sharedInstance].msgControl createMsg:APPEND_RECEIPT CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }

}


-(void) getReceiptImageUrl
{	
	switch ([self getReceiptUploadType])
	{
		case ReportEntryReceipt:
		{
			// For report entry receipts
			NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReceiptImageUrl/%@", [ExSystem sharedInstance].entitySettings.uri, data.reportEntry.receiptImageId];
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",[self getViewIDKey], @"TO_VIEW",nil];
			[[ExSystem sharedInstance].msgControl createMsg:GET_RECEIPT_URL CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:(MobileViewController*)self.delegate];
		}
            break;
        case MobileEntryReceipt:
		{
			// For report entry receipts
			NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReceiptImageUrl/%@", [ExSystem sharedInstance].entitySettings.uri, data.expenseEntry.receiptImageId];
			NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:path,@"URL",[self getViewIDKey], @"TO_VIEW",nil];
			[[ExSystem sharedInstance].msgControl createMsg:GET_RECEIPT_URL CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:(MobileViewController*)self.delegate];
		}
            break;
		default:
			break;
	}
}


-(void) addReceiptToReport
{
    NSDictionary *dict = @{@"Added To": @"Report"};
    [Flurry logEvent:@"Receipts: Add" withParameters:dict];

    networkCallInProgress = YES;
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:data.reportData, @"REPORT", [self getViewIDKey], @"TO_VIEW", nil];
	[[ExSystem sharedInstance].msgControl createMsg:SAVE_REPORT_RECEIPT2 CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


-(void) continueSaveReceipt
{
	switch ([self getReceiptUploadType])
	{
// MOB-9414 Do not cache receipt image id in entry before save succeeds.  Do not use the following code
//		case ReportEntryReceipt:
//		{
//			[self saveReportEntryWithNewReceipt];
//		}
//            break;
		case ReportReceipt:
		{
			[self addReceiptToReport];
		}
            break;
		default:
			break;
	}
}

#pragma mark -
#pragma mark Handle Messages
-(void) didProcessMessage:(Msg *)msg
{
    networkCallInProgress = NO;
    
	if ([msg.idKey isEqualToString:UPLOAD_IMAGE_DATA]) 
	{
		UploadReceiptData *receiptData = (UploadReceiptData*)msg.responder;
		switch ([self getReceiptUploadType]) 
		{
			case ReportEntryReceipt:
			{
				if (msg.errBody == nil && [receiptData.returnStatus isEqualToString:@"SUCCESS"]) 
				{
//					data.reportEntry.receiptImageId = receiptData.receiptImageId;
//                  [self continueSaveReceipt];
                    
                    NSString *appendToReceiptId = (msg.parameterBag)[@"APPEND_TO_RECEIPT_ID"];
                    [self saveReportEntryWithNewReceipt:receiptData.receiptImageId appendToReceiptId:appendToReceiptId];

				}
                else
                {
                    NSDictionary *dict = @{@"Failure": @"Failed to upload receipt image"};
                    [Flurry logEvent:@"Receipts: Failure" withParameters:dict];

                    // return to calling view controller
                    if (self.delegate != nil && [self.delegate respondsToSelector:@selector(didProcessMessage:)])
                    {
                        [(MobileViewController*)self.delegate didProcessMessage:msg];
                    }
                }
			}
                break;
			case ReportReceipt:
			{
                if (msg.errBody == nil && [receiptData.returnStatus isEqualToString:@"SUCCESS"]) 
				{
                    data.reportData.receiptImageId = receiptData.receiptImageId;
                    [self continueSaveReceipt];
                }
                else
                {
                    // return to calling view controller
                    if (self.delegate != nil && [self.delegate respondsToSelector:@selector(didProcessMessage:)])
                    {
                        [(MobileViewController*)self.delegate didProcessMessage:msg];
                    }
                }
			}
                break;
			case MobileEntryReceipt:
			{
                if (msg.errBody == nil && [receiptData.returnStatus isEqualToString:@"SUCCESS"]) 
				{
                    data.expenseEntry.receiptImageId = receiptData.receiptImageId;
                   [self cacheMobileEntryReceipt];
                }
                
                if (self.delegate != nil && [self.delegate respondsToSelector:@selector(didProcessMessage:)])
                {
                    [(MobileViewController*)self.delegate didProcessMessage:msg];
                }
			}
                break;
			default:
				break;
		}
	}
	// MOB-12019
    else if ([msg.idKey isEqualToString:APPEND_RECEIPT])
    {
        AppendReceiptData *appendReceiptData = (AppendReceiptData*)msg.responder;

        if (msg.errBody == nil && [appendReceiptData.actionStatus.status isEqualToString:@"SUCCESS"])
        {
            
        }
        else
        {
            // I think this is not called. The previous event that was here shows no data after August 2013
            // Event was previously: Failed to append receipt to report entry
            // Changed for safety, consistency
            NSDictionary *dict = @{@"Failure": @"Failed to append receipt to report entry"};
            [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
        }

        if (self.delegate != nil && [self.delegate respondsToSelector:@selector(didProcessMessage:)])
        {
            [(MobileViewController*)self.delegate didProcessMessage:msg];
        }
      
        [self reset];
    }
	else if ([msg.idKey isEqualToString:SAVE_REPORT_ENTRY_RECEIPT]) 
	{
		// Will be invoked only in case of Report entry receipt
		SaveReportEntryReceipt *rptEntryReceipt = (SaveReportEntryReceipt*)msg.responder;
		
        // MOB-9414 Check for failure, save data only if success.
        if (msg.errBody == nil && [rptEntryReceipt.actionStatus.status isEqualToString:@"SUCCESS"]) 
        {
            data.previousMeKeyForReportEntry = data.reportEntry.meKey;
            data.reportEntry.meKey = rptEntryReceipt.entry.meKey;
            data.reportEntry.rpeKey = rptEntryReceipt.entry.rpeKey;
            data.reportEntry.rptKey = rptEntryReceipt.entry.rptKey;
            
            data.reportEntry.receiptImage = data.receiptImageForCaching;
            data.reportEntry.receiptImageId = rptEntryReceipt.receiptImageId;
            [self cacheReportEntryReceipt];
        }
        else 
        {
            NSDictionary *dict = @{@"Failure": @"Failed to save receipt image id for entry"};
            [Flurry logEvent:@"Receipts: Failure" withParameters:dict];
        }
        if (self.delegate != nil && [self.delegate respondsToSelector:@selector(didProcessMessage:)])
        {
            [(MobileViewController*)self.delegate didProcessMessage:msg];
        }
        
        [self reset];
	}
	else if ([msg.idKey isEqualToString:SAVE_REPORT_RECEIPT2])
	{		
		if (self.delegate != nil && [self.delegate respondsToSelector:@selector(didProcessMessage:)])
		{
			[(MobileViewController*)self.delegate didProcessMessage:msg];
		}		
        
        [self reset];
	}
}

-(void) clearReceiptData
{
    [[ReceiptManager sharedInstance] clearAll];
}

// For SU entry receipt cache for PDF
-(UIImage*) getCachedReceiptImageForReportEntry:(NSString*)rpeKey
{
    NSString *key = [NSString stringWithFormat:@"RPE_KEY_%@",rpeKey];
    return [self getCachedReceiptImageForKey:key];

}

#pragma receipt error handling
+(BOOL) handleImageConfigError:(NSString*) errCode
{
    if ([@"Imaging Configuration Not Available." isEqualToString: errCode])
    {
        UIAlertView *alert = [[MobileAlertView alloc] 
                              initWithTitle:[Localizer getLocalizedText:@"Cannot access receipt"]
                              message:[Localizer getLocalizedText:@"ERROR_BAD_CONFIG_MSG"]
                              delegate:nil 
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        [alert show];
        return TRUE;
    }
    return FALSE;
}

@end
