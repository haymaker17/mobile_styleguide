//
//  QEFormVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormFieldData.h"
#import "FormFieldCell.h"
#import "DrillCell.h"
#import "DateTimeFormatter.h"
#import "FormViewControllerBase.h"
#import "FormatUtils.h"
#import "MobileActionSheet.h"
#import "ExpenseTypeDelegate.h"
#import "ReceiptEditorDelegate.h"
#import "Receipt.h"
#import "ReportActionDelegate.h"

@class EntityMobileEntry;
@class EntityUploadQueueItem;

@interface QEFormVC : FormViewControllerBase <UITableViewDelegate, UITableViewDataSource,
    ExpenseTypeDelegate,
    ReceiptEditorDelegate,
    UIActionSheetDelegate,
    ReportActionDelegate>
{
    EntityMobileEntry *entry;
    BOOL isSaving, updatedReceiptImageId, promptSaveAlert;
    NSMutableDictionary *requiredDict;
    Receipt *receipt; // Receipt object with receiptId, image, pdf, etc.
    NSManagedObjectContext *_managedObjectContext;
}

#define		kAlertViewRateApp	101781
@property (strong, nonatomic) NSMutableDictionary *requiredDict;
@property BOOL isSaving;
@property BOOL updatedReceiptImageId;
@property BOOL promptSaveAlert;
@property BOOL isInUploadQueue;

@property (strong, nonatomic) EntityMobileEntry *entry;
@property (strong, nonatomic) Receipt *receipt;
@property (nonatomic, strong, readonly) NSManagedObjectContext *managedObjectContext;

#pragma mark - Instantiation
-(id)initWithEntryOrNil:(EntityMobileEntry*) mobileEntry;
-(id) initWithEntryOrNil:(EntityMobileEntry*)mobileEntry withCloseButton:(BOOL)withCloseButton;

#pragma mark - Make Fields Array
-(void) makeFieldsArray:(id)sender;
-(void) makeSaveButton:(id)sender;
-(void) saveOOPE:(id)sender;
-(void) processFieldsToEntry:(id)sender;

-(void) btnClearReceipt;

-(void) fieldUpdated:(FormFieldData*) field;
-(void) fieldCanceled:(FormFieldData*) field;
- (void)showReceiptViewerAndAllowEdits:(BOOL)allowEdits excludeReceiptStoreOption:(BOOL)excludeReceiptStoreOption;
-(void) cancelExpenseType;
-(void) closeMe:(id)sender;

-(BOOL) isNewQuickExpense;
-(BOOL) isQueuedQuickExpense;
@end
