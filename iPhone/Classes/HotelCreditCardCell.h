//
//  HotelCreditCardCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface HotelCreditCardCell : UITableViewCell
{
	UILabel*	creditCardNameLabel;
	UILabel*	creditCardNumberLabel;
}


@property (nonatomic, strong) IBOutlet UILabel	*creditCardNameLabel;
@property (nonatomic, strong) IBOutlet UILabel	*creditCardNumberLabel;


+(HotelCreditCardCell*)makeCell:(UITableView*)tableView owner:owner cardName:(NSString*)cardName cardNumber:(NSString*)cardNumber;


@end
