//
//  CreateExpenseDS.h
//  ConcurMobile
//
//  Created by Shifan Wu on 10/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormFieldData.h"
#import "FormFieldCell.h"
#import "DrillCell.h"
#import "DateTimeFormatter.h"
#import "FormVCBaseInline.h"
#import "FormatUtils.h"
#import "MobileActionSheet.h"
#import "AppRating.h"
#import "ExpenseTypeDelegate.h"
#import "ReceiptEditorDelegate.h"
#import "Receipt.h"

@class EntityMobileEntry;
@class EntityUploadQueueItem;

@interface CreateExpenseDS : FormVCBaseInline <UITableViewDelegate, UITableViewDataSource, ExpenseTypeDelegate, ReceiptEditorDelegate, UIActionSheetDelegate>
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
