//
//  AirlineTicket.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface AirlineTicket : NSObject 
{
	NSString			*recordLocator, *PlatingCarrierNumericCode, *PlatingControlNumber, *IssueDateTime, *PassengerName, *Ticketless, *ProgramCarrierCode,
						*ProgramMembershipNumber, *BaseFare, *BaseFareCurrency, *TotalFare, *TotalFareCurrency, *Endorsements, *LinearFareConstructor, *idKey;
	NSMutableDictionary	*airlineTicketCoupons, *airlineTicketTaxes;
}

@end
