//
//  CreateExpenseDS.h
//  ConcurMobile
//
//  Created by Shifan Wu on 10/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormFieldData.h"
#import "SmartExpenseManager2.h"

#import "EntityMobileEntry.h"
#import "EntityUploadQueueItem.h"

@protocol CreateExpenseDSDelegate;

@interface CreateExpenseDS : NSObject <UITableViewDataSource, UITableViewDelegate>
{
    UITableView         *tableList;
    EntityMobileEntry   *entry;
    Receipt             *receipt; // Receipt object with receiptId, image, pdf, etc.
    
    BOOL updatedReceiptImageID;
    
    NSMutableArray      *sections;
    NSMutableDictionary *sectionDataMap;
    NSMutableDictionary *sectionFieldsMap;
    NSMutableArray      *allFields;
    
    id<CreateExpenseDSDelegate>   __weak _delegate;
}

@property (nonatomic, strong) UITableView                   *tableList;
@property (strong, nonatomic) EntityMobileEntry             *entry;
@property (strong, nonatomic) Receipt                       *receipt;

@property BOOL updatedReceiptImageId;
@property BOOL hasCloseButton;
@property (nonatomic, strong) SmartExpenseManager2          *smartExpenseManager2;
@property BOOL isInUploadQueue;
@property (nonatomic, strong, readonly) NSManagedObjectContext *managedObjectContext;


@property(nonatomic, strong) NSMutableArray                 *sections;
@property(nonatomic, strong) NSMutableDictionary            *sectionDataMap;
@property(nonatomic, strong) NSMutableDictionary            *sectionFieldsMap;
@property(nonatomic, strong) NSMutableArray                 *allFields;

@property (nonatomic, weak) id<CreateExpenseDSDelegate>     delegate;

- (void)setSeedData:(UITableView *)tbl withDelegate:(id<CreateExpenseDSDelegate>)del;

#pragma mark - Instantiation
- (id)initWithEntryOrNil:(EntityMobileEntry*) mobileEntry;
- (id)initWithEntryOrNil:(EntityMobileEntry*)mobileEntry withCloseButton:(BOOL)withCloseButton;

#pragma mark - Make Fields Array
- (void)makeFieldsArray:(id)sender;
- (void)processFieldsToEntry:(id)sender;
@end

// Handles editing on QE form
@protocol CreateExpenseDSDelegate <NSObject>

- (void)saveOOPE:(id)sender;
- (void)fieldUpdated:(FormFieldData*) field;
- (void)fieldCanceled:(FormFieldData*) field;
- (BOOL) isNewQuickExpense;
- (BOOL) isQueuedQuickExpense;

@optional

@end
