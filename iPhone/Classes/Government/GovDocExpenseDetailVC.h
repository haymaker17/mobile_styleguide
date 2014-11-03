//
//  GovDocExpenseDetailVC.h
//  ConcurMobile
//
//  Created by charlottef on 2/5/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "GovDocExpense.h"
#import "Receipt.h"
#import "ReceiptEditorDelegate.h"

@interface GovDocExpenseDetailVC : MobileViewController <UITableViewDataSource, UITableViewDelegate, ReceiptEditorDelegate>
{
    UITableView             *tableList;
    GovDocExpense           *expense;
    NSString                *docType;
    NSString                *docName;
    Receipt                 *receipt;
}

@property (nonatomic, strong) IBOutlet UITableView  *tableList;
@property (nonatomic, strong) GovDocExpense         *expense;
@property (nonatomic, strong) NSString              *docType;
@property (nonatomic, strong) NSString              *docName;
@property (nonatomic, strong) Receipt               *receipt;

-(void)setSeedDataWithExpense:(GovDocExpense*) documentExpense docType:(NSString*)documentType docName:(NSString*)documentName;

@end
