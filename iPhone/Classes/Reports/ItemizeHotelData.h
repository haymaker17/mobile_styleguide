//
//  ItemizeHotelData.h
//  ConcurMobile
//
//  Created by yiwen on 1/11/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SaveReportEntryData.h"

// The response is the same as SaveReportEntryData
@interface ItemizeHotelData : SaveReportEntryData 
{
	NSArray					*additionalCharges;
	NSString				*checkInDate;
	NSString				*checkOutDate;
	int						 numberOfNights;
	double					 roomRate;
	double					 roomTax;
}

@property(nonatomic, strong) NSArray		*additionalCharges;
@property(nonatomic, strong) NSString		*checkInDate;
@property(nonatomic, strong) NSString		*checkOutDate;
@property int								numberOfNights;
@property double							roomRate;
@property double							roomTax;

@property (nonatomic, readwrite, assign) double otherRoomTax1;
@property (nonatomic, readwrite, assign) double otherRoomTax2;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;


@end
