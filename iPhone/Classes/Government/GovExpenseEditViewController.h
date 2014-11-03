//
//  GovExpenseEditViewController.h
//  ConcurMobile
//
//  Created by ernest cho on 9/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "FormViewControllerBase.h"
#import "GovExpenseType.h"
#import "GovExpenseTypeDelegate.h"
#import "Receipt.h"
#import "ReceiptEditorDelegate.h"
#import "GovExpenseEditDelegate.h"

@interface GovExpenseEditViewController : FormViewControllerBase <GovExpenseTypeDelegate, ReceiptEditorDelegate>
{
    BOOL    doReload;
    NSString       *expenseId;
    Receipt        *receipt;
    NSMutableDictionary     *formAttributes; // non-fields related attributes inside Get response
	id<GovExpenseEditDelegate>       __weak _delegate;
}

@property (copy, nonatomic) NSString         *expenseId;
@property (strong, nonatomic) Receipt        *receipt;
@property(nonatomic, strong) NSMutableDictionary        *formAttributes;
@property (weak, nonatomic) id<GovExpenseEditDelegate>         delegate;

-(void) setSeedDelegate:(id<GovExpenseEditDelegate>)del;
-(BOOL) isReceiptUpdated;

@end
