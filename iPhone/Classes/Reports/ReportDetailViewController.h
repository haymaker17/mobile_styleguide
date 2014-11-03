//
//  ReportDetailViewController.h
//  ConcurMobile
//
//  Created by yiwen on 4/25/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportHeaderViewControllerBase.h"
#import "UnifiedImagePicker.h"
#import "ExpenseTypeDelegate.h"
#import "ReceiptEditorDelegate.h"
#import "Receipt.h"

@interface ReportDetailViewController : ReportHeaderViewControllerBase 
    <UIAlertViewDelegate, 
    UIActionSheetDelegate,
    UINavigationControllerDelegate, 
    ExpenseTypeDelegate,
    ReceiptEditorDelegate>
{
    //UIImage					*receiptImage;
//    Receipt                     *receipt;
}

//@property (strong, nonatomic) UIImage					*receiptImage;
//@property (strong, nonatomic) Receipt					*receipt;

// Init data
- (void)setSeedData:(NSDictionary*)pBag;
- (void)setSeedData:(ReportData*)report role:(NSString*) curRole;
- (void)loadReport:(ReportData*) report;
- (void)refreshView;
- (void)fetchReportDetail;

- (BOOL)hasReceipt;

// Add Entry methods
-(void) buttonAddExpensePressed;
-(void) buttonImportExpensesPressed;
+(void) showEntryView:(MobileViewController*)parentVC withParameterBag: (NSMutableDictionary*)pBag carMileageFlag:(BOOL)isCarMileage;

// ActionSheet
-(IBAction)buttonAddPressed:(id) sender;

-(void) refreshParent:(Msg *) msg;

// ExpenseTypeDelegate Methods 
- (void)cancelExpenseType;
- (void)saveSelectedExpenseType:(ExpenseTypeData*) et;

+(void)showReportDetail:(MobileViewController*)pvc withReport:(ReportData*)report withRole:(NSString*) curRole;

-(void) actionOnNoData:(id)sender;

//Receipt actions
- (void)showReceiptViewer;

@end
