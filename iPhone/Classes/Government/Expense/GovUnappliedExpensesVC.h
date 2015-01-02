//
//  GovUnappliedExpensesVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/31/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileTableViewController.h"
#import "SummaryCellMLines.h"
#import "GovExpenseEditDelegate.h"
#import "GovUnappliedExpensesDelegate.h"

@interface GovUnappliedExpensesVC : MobileTableViewController <NSFetchedResultsControllerDelegate, GovExpenseEditDelegate>
{
    BOOL                    isAddingMode;
    NSString                *docType;
    NSString                *docName;
    NSString                *travID;
    
    NSMutableDictionary     *selectedRows;
    NSMutableArray          *expKeys;
    
    id<GovUnappliedExpensesDelegate>        __weak _delegate;
}

@property (nonatomic, strong) NSFetchedResultsController    *fetchedResultsController;
@property (nonatomic, strong) NSManagedObjectContext        *managedObjectContext;

@property BOOL                                              isAddingMode;
@property (nonatomic, strong) NSString                      *docType;
@property (nonatomic, strong) NSString                      *docName;
@property (nonatomic, strong) NSString                      *travID;
@property (nonatomic, strong) NSMutableDictionary           *selectedRows;
@property (nonatomic, strong) NSMutableArray                *expKeys;
@property (weak, nonatomic) id<GovUnappliedExpensesDelegate> delegate;

+(void)showUnappliedExpenses:(UIViewController*)pvc;
-(void) setSeedDelegate:(id<GovUnappliedExpensesDelegate>)del;
-(void) buttonCancelPressed:(id)sender;
-(void) buttonAddToVoucherPressed:(id)sender;
-(void) buttonAddToVoucherOnePressed:(id)sender;
-(void) buttonSelectAll:(id)sender;
-(void) buttonUnSelectAll:(id)sender;

-(void) makeSelectAllButtons;
-(void) makeUnSelectAllButtons;
-(UIBarButtonItem *) makeAddToVoucherButton:(NSInteger)count;

-(void) setUpToolBar:(id)sender;

-(void) goToDocumentDetailScreen;
//-(void)setSelected:(BOOL)selected toCell:(SummaryCellMLines *)cell;
@end
