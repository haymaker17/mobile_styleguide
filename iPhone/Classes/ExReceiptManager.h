//
//  ExReceiptManager.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

// Purpose: Manages all stages of receipt handling from receipt viewing, updating, event handling to caching.

#import <Foundation/Foundation.h>
#import "ExpenseReceiptData.h"
#import "ReceiptManager.h"
#import "BaseManager.h"
#import "EntityReceipt.h"


@protocol ExReceiptManagerDelegate <NSObject>
-(void) handleReceiptFailure;
@optional
-(void) receiptManagerReturnedWithImage:(UIImage*)img;
@end

@interface ExReceiptManager : NSObject <ExMsgRespondDelegate,NSFetchedResultsControllerDelegate>{
    id <ExReceiptManagerDelegate> __weak delegate;
    
    ExpenseReceiptData* data;
	BOOL isReceiptStore;
	RootViewController *rvc;	
	NSString *role;
	int currentReceiptUploadType;
    BOOL networkCallInProgress;
    BOOL bypass;
    BOOL isNewExpenseEntry;
}

@property (nonatomic,weak) id <ExReceiptManagerDelegate> delegate;
@property (nonatomic,strong) NSString *role;
@property (nonatomic,strong) ExpenseReceiptData* data;
@property (nonatomic,assign) BOOL isReceiptStore;
@property (nonatomic,assign) int currentReceiptUploadType;
@property (nonatomic,assign) BOOL networkCallInProgress;
@property (nonatomic,assign) BOOL bypass;
@property (nonatomic,assign) BOOL isNewExpenseEntry;

+(ExReceiptManager*) sharedInstance;
-(void) configureReceiptManagerForDelegate:(id)viewController 
                             andInvoiceKey:(NSString *)key;
-(void) configureReceiptManagerForDelegate:(id)viewController 
                            andReportEntry:(EntryData*)rptEntry 
                              andReporData:(ReportData*)rptData 
                           andExpenseEntry:(OOPEntry*)expEntry
                                   andRole:(NSString*)userRole;
-(void) configureReceiptManagerForDelegate:(id)viewController 
                         isNewExpenseEntry:(BOOL)isNew;
-(void) reset;
-(int) getReceiptUploadType;

#pragma mark Access cached/online receipts
-(void) fetchOnlineReportEntryReceipt;
-(void) fetchOnlineReportReceipt;
-(void) fetchOnlineMobileEntryReceipt;
-(void) getReceiptImageUrl;
-(NSMutableData*) getCachedReceiptPDFForKey:(NSString*)key;
-(UIImage *)getCachedInvoiceReceiptImage;

-(UIImage*) fetchCachedReceipt;
-(UIImage*) fetchCachedReceiptForImageId:(NSString*)imgId;

-(void) updateReportEntryImage:(UIImage*)img;
-(void) updateMobileEntryImage:(UIImage*)img;

-(void) cacheReceipt;
-(void) cacheReportEntryReceipt;
-(void) cacheReportPDFReceipt:(NSData*)pdfData;
-(void) cacheMobileEntryReceipt;
-(void) cacheInvoiceReceipt:(UIImage *)img;
-(void) cacheReceiptStoreReceiptImage:(UIImage*)img withImageId:(NSString*)imageId;
-(void) deleteCachedReceipt;
-(void) removeFromReceiptStoreCache:(NSString*)key;

#pragma mark Upload/Associate receipts
-(NSData *) receiptImageDataFromReceiptImage:(UIImage*)receiptImage;
-(void) uploadReceipt:(UIImage*)receiptImage appendToReceiptId:(NSString*)appendToReceiptId;
-(void) uploadReceipt:(UIImage*)receiptImage;
-(void) continueSaveReceipt;
-(void) saveReportEntryWithNewReceipt:(NSString*) receiptId appendToReceiptId:(NSString*)appendToReceiptId;
-(void) addReceiptToReport;

#pragma mark SU entry receipt cache for PDF
-(UIImage*) getCachedReceiptImageForReportEntry:(NSString*)rpeKey;

#pragma receipt error handling
+(BOOL) handleImageConfigError:(NSString*) errCode;

@end
