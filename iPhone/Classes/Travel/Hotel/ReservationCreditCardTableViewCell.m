//
//  CreditCardTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ReservationCreditCardTableViewCell.h"

@implementation ReservationCreditCardTableViewCell

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

-(void)setCellData:(NSArray *)creditCards creditCardSelectedIndex:(int)index
{
    if ([creditCards count] > 1) {
        // if credit card is not selected, display the default one
        if (!index) {
            for (CTECreditCard *card in creditCards) {
                if (card.isDefault) {
                    self.labelCreditCardName.text = card.name;
                    break;
                }
            }
        }
        // display the selected credit card
        else {
            self.labelCreditCardName.text = ((CTECreditCard *)creditCards[index]).name;
        }
        
        self.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    else if ([creditCards count] == 1) {
        self.accessoryType = UITableViewCellAccessoryNone;
        self.labelCreditCardName.text = ((CTECreditCard *)creditCards[0]).name;
    }
    // else don't need to pay!
}

@end
