//
//  GovDocExpensesVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "GovDocumentDetail.h"
#import "GovDocHeaderShortProtocol.h"

@interface GovDocExpensesVC : MobileViewController<UITableViewDataSource, UITableViewDelegate, GovDocHeaderShortProtocol, UIActionSheetDelegate>
{
    GovDocumentDetail       *doc;
}

@property (strong, nonatomic) IBOutlet UILabel			*lblName;
@property (strong, nonatomic) IBOutlet UILabel			*lblAmount;
@property (strong, nonatomic) IBOutlet UILabel			*lblStatus;
@property (strong, nonatomic) IBOutlet UILabel			*lblDocName;
@property (strong, nonatomic) IBOutlet UILabel			*lblDates;
@property (strong, nonatomic) IBOutlet UIImageView		*img1;
@property (strong, nonatomic) IBOutlet UIImageView		*img2;
@property (strong, nonatomic) IBOutlet UITableView      *tableList;
@property (strong, nonatomic) UIActionSheet             *addExpenseAction;

@property (strong, nonatomic) GovDocumentDetail         *doc;

+(void)showDocExpenses:(UIViewController*)pvc withDoc:(GovDocumentDetail*) docDetail;
+(void)drawHeader:(id< GovDocHeaderShortProtocol>)pvc withDoc:(GovDocumentDetail*) document;

// Init data
-(void)setUpBarItems;

// add Entry Method
-(void)buttonAddExpensePressed;
-(void)btnSelectFromExistingPressed;

// ActionSheet
-(IBAction)buttonAddPressed:(id) sender;
-(void) navigateBack:(id)sender;
@end
