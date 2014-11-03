//
//  ReceiptDetailViewController.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/16/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "UnifiedImagePicker.h"
#import "SaveReportEntryReceipt.h"
#import "ExReceiptManager.h"

@protocol ReceiptDetailViewDelegate <NSObject>
@optional
- (void)savedReportEntryReceipt:(SaveReportEntryReceipt*)responseData;
- (void)savedReportReceipt;

// Used if ExReceiptManager is bypassed (when rpeKey/meKey is nil)
- (void)selectedImageForNewEntry:(UIImage*)image;
- (void)selectedReceiptStoreImageForNewEntry:(NSString *)receiptImageId;

- (void)updatedReportEntryReceipt: (Msg*)msg;

@end

@interface ReceiptDetailViewController : MobileViewController <UIScrollViewDelegate, 
                                                                UIActionSheetDelegate, 
                                                                UnifiedImagePickerDelegate,
                                                                ExReceiptManagerDelegate, 
                                                                UIAlertViewDelegate>
{
    BOOL    isLoadingReceipt;
    BOOL    canUpdateReceipt;
    BOOL    isViewerOnlyMode;
    BOOL    isMimeTypePDF;
    BOOL    enableDeleteReceipt;
    BOOL    canShowActionSheet;
    
    UIImage *receiptImg;
    UIImageView *receiptImgView;
    
    NSMutableData  *pdfData;
    UIWebView   *pdfWebView;
    UIScrollView    *scrollView;
    id<ReceiptDetailViewDelegate> __weak delegate;
    
    UIActionSheet *receiptActionSheet;
    NSString * role;
    ReportData				*rpt;
    BOOL    isApprovalReport;
    BOOL    excludeReceiptStoreOption;
    BOOL    isReportView;
}

@property (nonatomic, strong) NSString *role;
@property (nonatomic, assign) BOOL  isLoadingReceipt;
@property (nonatomic, assign) BOOL  canUpdateReceipt;
@property (nonatomic, assign) BOOL  isViewerOnlyMode;
@property (nonatomic, strong) UIImage   *receiptImg;
@property (nonatomic, strong) IBOutlet UIImageView  *receiptImgView;
@property (nonatomic, strong) IBOutlet UIScrollView *scrollView;
@property (nonatomic, assign) BOOL  isMimeTypePDF;
@property (nonatomic, strong) NSMutableData *pdfData;
@property (nonatomic, strong) UIWebView *pdfWebView;
@property (nonatomic, weak) id<ReceiptDetailViewDelegate> delegate;
@property (nonatomic, assign) BOOL  enableDeleteReceipt;
@property (nonatomic, assign) BOOL  canShowActionSheet;
@property (nonatomic, strong) UIActionSheet *receiptActionSheet;
@property (nonatomic, strong) ReportData *rpt;
@property (nonatomic, assign) BOOL    isApprovalReport;
@property (nonatomic, assign) BOOL    excludeReceiptStoreOption;
@property (nonatomic, assign) BOOL  isReportView;

- (NSString*)getViewIDKey;
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil isReportView:(BOOL)isReportLevel;
@end
