//
//  AllowanceAmountViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 3/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AllowanceAmountViewController : UIViewController

@property IBOutlet UITextField *amountField;

@property NSString *amount;
@property NSString *meal;
@property NSInteger *section;
@property NSString *mode;


@end
