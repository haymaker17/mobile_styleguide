//
//  iPadHome9Cell.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface iPadHome9Cell : UITableViewCell

// Expense
@property (strong, nonatomic) IBOutlet UIView *viewCell1;
@property (strong, nonatomic) IBOutlet UILabel * lblViewCell1Title;
@property (strong, nonatomic) IBOutlet UILabel * lblViewCell1SubTitle;
@property (strong, nonatomic) IBOutlet UIButton *btnViewCell1;
@property (strong, nonatomic) IBOutlet UILabel *lblViewCell1whiteback;
@property (strong, nonatomic) IBOutlet UIImageView *ivViewCell1Indicator;
@property (strong, nonatomic) IBOutlet UIImageView *ivViewCell1Icon;

// Expense Reports
@property (strong, nonatomic) IBOutlet UIView *viewCell2;
@property (strong, nonatomic) IBOutlet UILabel *lblViewCell2Title;
@property (strong, nonatomic) IBOutlet UILabel *lblViewCell2SubTitle;
@property (strong, nonatomic) IBOutlet UIImageView *ivViewCell2Icon;
@property (strong, nonatomic) IBOutlet UILabel *lblViewCell2whiteback;
@property (strong, nonatomic) IBOutlet UIImageView *ivViewCell2Indicator;
@property (strong, nonatomic) IBOutlet UIButton *btnViewCell2;

// Approvals
@property (strong, nonatomic) IBOutlet UIView *viewCell3;
@property (strong, nonatomic) IBOutlet UILabel *lblViewCell3Title;
@property (strong, nonatomic) IBOutlet UILabel *lblViewCell3SubTitle;
@property (strong, nonatomic) IBOutlet UIImageView *ivViewCell3Icon;
@property (strong, nonatomic) IBOutlet UILabel *lblViewCell3whiteback;
@property (strong, nonatomic) IBOutlet UIImageView *ivViewCell3Indicator;
@property (strong, nonatomic) IBOutlet UIButton *btnViewCell3;

@end
