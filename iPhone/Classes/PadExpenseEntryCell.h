//
//  PadExpenseEntryCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 10/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface PadExpenseEntryCell : UITableViewCell {
	UIImageView		*iv1, *iv2, *iv3, *iv4, *iv5, *ivTop, *ivBody;
	UILabel			*lblExpenseType, *lblAmountLabel, *lblAmount, *lblRequestLabel, *lblRequest, *lblLine1, *lblLine2, *lblSep;
	UILabel			*lblLabelExtra1, *lblValueExtra1, *lblLabelExtra2, *lblValueExtra2, *lblLabelExtra3, *lblValueExtra3;
}

@property (strong, nonatomic) IBOutlet UIImageView		*iv1;
@property (strong, nonatomic) IBOutlet UIImageView		*iv2;
@property (strong, nonatomic) IBOutlet UIImageView		*iv3;
@property (strong, nonatomic) IBOutlet UIImageView		*iv4;
@property (strong, nonatomic) IBOutlet UIImageView		*iv5;
@property (strong, nonatomic) IBOutlet UIImageView		*ivTop;
@property (strong, nonatomic) IBOutlet UIImageView		*ivBody;

@property (strong, nonatomic) IBOutlet UILabel			*lblExpenseType;
@property (strong, nonatomic) IBOutlet UILabel			*lblAmountLabel;
@property (strong, nonatomic) IBOutlet UILabel			*lblAmount;
@property (strong, nonatomic) IBOutlet UILabel			*lblRequestLabel;
@property (strong, nonatomic) IBOutlet UILabel			*lblRequest;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine1;
@property (strong, nonatomic) IBOutlet UILabel			*lblLine2;
@property (strong, nonatomic) IBOutlet UILabel			*lblSep;

@property (strong, nonatomic) IBOutlet UILabel			*lblLabelExtra1;
@property (strong, nonatomic) IBOutlet UILabel			*lblValueExtra1;
@property (strong, nonatomic) IBOutlet UILabel			*lblLabelExtra2;
@property (strong, nonatomic) IBOutlet UILabel			*lblValueExtra2;
@property (strong, nonatomic) IBOutlet UILabel			*lblLabelExtra3;
@property (strong, nonatomic) IBOutlet UILabel			*lblValueExtra3;
@end
