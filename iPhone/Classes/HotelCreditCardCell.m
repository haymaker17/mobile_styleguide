//
//  HotelCreditCardCell.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelCreditCardCell.h"


@implementation HotelCreditCardCell


@synthesize creditCardNameLabel;
@synthesize creditCardNumberLabel;


NSString * const HOTEL_CREDIT_CARD_CELL_IDENTIFIER = @"HOTEL_CREDIT_CARD_CELL_IDENTIFIER";


+(HotelCreditCardCell*)makeCell:(UITableView*)tableView owner:owner cardName:(NSString*)cardName cardNumber:(NSString*)cardNumber
{
	HotelCreditCardCell *cell = (HotelCreditCardCell *)[tableView dequeueReusableCellWithIdentifier: HOTEL_CREDIT_CARD_CELL_IDENTIFIER];
	
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"HotelCreditCardCell" owner:self options:nil];
		for (id oneObject in nib)
		{
			if ([oneObject isKindOfClass:[HotelCreditCardCell class]])
			{
				cell = (HotelCreditCardCell *)oneObject;
				break;
			}
		}
	}
	
	cell.creditCardNameLabel.text = cardName;
	cell.creditCardNumberLabel.text = cardNumber;
	
	return cell;
}


// Called by the framework to get the reuse identifier for this cell
-(NSString*)reuseIdentifier
{
	return HOTEL_CREDIT_CARD_CELL_IDENTIFIER;
}



@end
