//
//  ItemizeHotelData.m
//  ConcurMobile
//
//  Created by yiwen on 1/11/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ItemizeHotelData.h"
#import "DataConstants.h"
//-
@implementation ItemizeHotelData
@synthesize additionalCharges, checkInDate, checkOutDate, numberOfNights, roomRate, roomTax, otherRoomTax1, otherRoomTax2;

-(NSString *)getMsgIdKey
{
	return ITEMIZE_HOTEL_DATA;
}

-(NSString *)makeXMLBody
{//knows how to make a post
	
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc]
								initWithString:@"<HotelItemization>"];
								//xmlns='http://schemas.datacontract.org/2004/07/Snowbird' xmlns:i='http://www.w3.org/2001/XMLSchema-instance'
	
	if (self.additionalCharges != nil && [self.additionalCharges count] > 0) 
	{
		[bodyXML appendString:@"<AdditionalCharges>"];
		for (int ix = 0; ix < [self.additionalCharges count]; ix++)
		{
			NSArray *expenseCharge = (NSArray*) additionalCharges[ix];
			[bodyXML appendString:@"<ExpenseCharge>"];
			[bodyXML appendString:[NSString stringWithFormat:@"<Amount>%@</Amount>", expenseCharge[1]]];
			[bodyXML appendString:[NSString stringWithFormat:@"<ExpKey>%@</ExpKey>", expenseCharge[0]]];
			[bodyXML appendString:@"</ExpenseCharge>"];
		}
		[bodyXML appendString:@"</AdditionalCharges>"];
	}
	[bodyXML appendString:[NSString stringWithFormat:@"<CheckInDate>%@</CheckInDate>", self.checkInDate]];
	[bodyXML appendString:[NSString stringWithFormat:@"<CheckOutDate>%@</CheckOutDate>", self.checkOutDate]];
	[bodyXML appendString:[NSString stringWithFormat:@"<NumberOfNights>%d</NumberOfNights>", self.numberOfNights]];
    if (self.roomRate > 0.0) {
        [bodyXML appendString:[NSString stringWithFormat:@"<OtherRoomTax1>%f</OtherRoomTax1>", self.otherRoomTax1]];
        [bodyXML appendString:[NSString stringWithFormat:@"<OtherRoomTax2>%f</OtherRoomTax2>", self.otherRoomTax2]];
    }
    
	[bodyXML appendString:[NSString stringWithFormat:@"<RoomRate>%f</RoomRate>", self.roomRate]];
	
	if (self.roomRate > 0.0) {
		[bodyXML appendString:[NSString stringWithFormat:@"<RoomTax>%f</RoomTax>", self.roomTax]];
    }
	[bodyXML appendString:@"</HotelItemization>"];
	return bodyXML;
}	


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.entry = parameterBag[@"ENTRY"];	// Parent entry
	self.roleCode = parameterBag[@"ROLE_CODE"];

	self.additionalCharges = parameterBag[@"ADDITIONAL_CHARGES"];
	self.checkInDate = parameterBag[@"CHECK_IN_DATE"];
	self.checkOutDate =parameterBag[@"CHECK_OUT_DATE"];
	self.numberOfNights = [(NSNumber*)parameterBag[@"NUMBER_OF_NIGHTS"] intValue];
	self.roomRate = [(NSNumber*)parameterBag[@"ROOM_RATE"] doubleValue];
	self.roomTax = [(NSNumber*)parameterBag[@"ROOM_TAX"] doubleValue];
    self.otherRoomTax1 = [(NSNumber*)parameterBag[@"OTHER_ROOM_TAX1"] doubleValue];
    self.otherRoomTax2 = [(NSNumber*)parameterBag[@"OTHER_ROOM_TAX2"] doubleValue];
	
	if (![roleCode length])
		self.roleCode = ROLE_EXPENSE_TRAVELER;
	
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ItemizeHotel/%@/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.roleCode, entry.rpeKey];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	
	return msg;
}


@end
