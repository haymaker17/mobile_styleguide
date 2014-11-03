//
//  CreditCardTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTECreditCard.h"

@interface CreditCardTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *creditCardName;
@property (weak, nonatomic) IBOutlet UILabel *creditCardLastFourDigits;

-(void)setCellData:(CTECreditCard *) cteCreditCard;

@end
