//
//  CreditCardTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CreditCardTableViewCell.h"

@implementation CreditCardTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)awakeFromNib
{
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(CTECreditCard *) cteCreditCard
{
    self.creditCardName.text = cteCreditCard.name;
    self.creditCardLastFourDigits.text = cteCreditCard.lastFourDigits;
}

@end
