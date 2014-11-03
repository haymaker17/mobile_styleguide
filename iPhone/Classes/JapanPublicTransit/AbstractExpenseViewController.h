//
//  AbstractExpenseViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AbstractViewController.h"
#import "DCRoundSwitch.h"
#import "ReportData.h"
#import "RouteExpense.h"

@interface AbstractExpenseViewController : AbstractViewController <UIAlertViewDelegate, UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UILabel *tripSynopsisLabel;
@property (weak, nonatomic) IBOutlet UILabel *tripMetaDataLabel;
@property (weak, nonatomic) IBOutlet UILabel *tripPriceLabel;
@property (weak, nonatomic) IBOutlet UILabel *tripDateLabel;

@property (weak, nonatomic) IBOutlet UITextField *purposeTextField;
@property (weak, nonatomic) IBOutlet UITextField *commentTextField;

@property (weak, nonatomic) IBOutlet UILabel *favoriteLabel;
@property (weak, nonatomic) IBOutlet UILabel *personalExpenseLabel;

@property (weak, nonatomic) IBOutlet UISwitch *toggleFavoriteSwitchPlaceholder;
@property (weak, nonatomic) IBOutlet UISwitch *togglePersonalExpenseSwitchPlaceholder;

@property (strong, nonatomic) DCRoundSwitch *toggleFavoriteSwitch;
@property (strong, nonatomic) DCRoundSwitch *togglePersonalExpenseSwitch;

@end
