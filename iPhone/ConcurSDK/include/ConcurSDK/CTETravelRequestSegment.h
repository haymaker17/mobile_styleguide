//
//  CTETravelRequestSegment.h
//  ConcurSDK
//
//  Created by laurent mery on 02/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//

#import <Foundation/Foundation.h>
#import "CTEDataTypes.h"

@class CTEComment;

@interface CTETravelRequestSegment : NSObject

@property (copy, nonatomic) CTEDataTypes *SegmentTypeName;
@property (copy, nonatomic) CTEDataTypes *DepartureDate;
@property (copy, nonatomic) CTEDataTypes *DepartureTime;
@property (copy, nonatomic) CTEDataTypes *ArrivalDate;
@property (copy, nonatomic) CTEDataTypes *ArrivalTime;
@property (copy, nonatomic) CTEDataTypes *FromLocationName;
@property (copy, nonatomic) CTEDataTypes *ToLocationName;
@property (copy, nonatomic) CTEDataTypes *FromLocationDetail;
@property (copy, nonatomic) CTEDataTypes *ToLocationDetail;
@property (copy, nonatomic) CTEDataTypes *ExchangeRate;
@property (copy, nonatomic) CTEDataTypes *ForeignAmount;
@property (copy, nonatomic) CTEDataTypes *ForeignCurrencyName;
@property (copy, nonatomic) CTEDataTypes *ForeignCurrencyCode;
@property (copy, nonatomic) CTEDataTypes *PostedAmount;
@property (copy, nonatomic) CTEDataTypes *RemainingAmount;
@property (copy, nonatomic) CTEDataTypes *FormTypeCode;
@property (copy, nonatomic) CTEDataTypes *SegmentTypeCode;
@property (copy, nonatomic) CTEDataTypes *ApprovedAmount;
@property (copy, nonatomic) CTEDataTypes *IsAgencyBooked;
@property (copy, nonatomic) CTEDataTypes *IsSelfBooked;
@property (copy, nonatomic) CTEDataTypes *LastModifiedDate;
@property (copy, nonatomic) CTEDataTypes *OutboundSegmentID;
@property (copy, nonatomic) CTEDataTypes *SegmentFormID;
@property (copy, nonatomic) NSArray *Comments;
@property (copy, nonatomic) NSArray *Exceptions;
@property (copy, nonatomic) CTEDataTypes *Custom1;
@property (copy, nonatomic) CTEDataTypes *Custom2;
@property (copy, nonatomic) CTEDataTypes *Custom3;
@property (copy, nonatomic) CTEDataTypes *Custom4;
@property (copy, nonatomic) CTEDataTypes *Custom5;
@property (copy, nonatomic) CTEDataTypes *Custom6;
@property (copy, nonatomic) CTEDataTypes *Custom7;
@property (copy, nonatomic) CTEDataTypes *Custom8;
@property (copy, nonatomic) CTEDataTypes *Custom9;
@property (copy, nonatomic) CTEDataTypes *Custom10;
@property (copy, nonatomic) CTEDataTypes *Custom11;
@property (copy, nonatomic) CTEDataTypes *Custom12;
@property (copy, nonatomic) CTEDataTypes *Custom13;
@property (copy, nonatomic) CTEDataTypes *Custom14;
@property (copy, nonatomic) CTEDataTypes *Custom15;
@property (copy, nonatomic) CTEDataTypes *Custom16;
@property (copy, nonatomic) CTEDataTypes *Custom17;
@property (copy, nonatomic) CTEDataTypes *Custom18;
@property (copy, nonatomic) CTEDataTypes *Custom19;
@property (copy, nonatomic) CTEDataTypes *Custom20;
@property (copy, nonatomic) CTEDataTypes *Custom21;
@property (copy, nonatomic) CTEDataTypes *Custom22;
@property (copy, nonatomic) CTEDataTypes *Custom23;
@property (copy, nonatomic) CTEDataTypes *Custom24;
@property (copy, nonatomic) CTEDataTypes *Custom25;
@property (copy, nonatomic) CTEDataTypes *Custom26;
@property (copy, nonatomic) CTEDataTypes *Custom27;
@property (copy, nonatomic) CTEDataTypes *Custom28;
@property (copy, nonatomic) CTEDataTypes *Custom29;
@property (copy, nonatomic) CTEDataTypes *Custom30;
@property (copy, nonatomic) CTEDataTypes *Custom31;
@property (copy, nonatomic) CTEDataTypes *Custom32;
@property (copy, nonatomic) CTEDataTypes *Custom33;
@property (copy, nonatomic) CTEDataTypes *Custom34;
@property (copy, nonatomic) CTEDataTypes *Custom35;
@property (copy, nonatomic) CTEDataTypes *Custom36;
@property (copy, nonatomic) CTEDataTypes *Custom37;
@property (copy, nonatomic) CTEDataTypes *Custom38;
@property (copy, nonatomic) CTEDataTypes *Custom39;
@property (copy, nonatomic) CTEDataTypes *Custom40;

//airfr
@property (copy, nonatomic) CTEDataTypes *RecordLocator;
@property (copy, nonatomic) CTEDataTypes *FlightNumber;
@property (copy, nonatomic) CTEDataTypes *ClassOfServiceCode;
@property (copy, nonatomic) CTEDataTypes *TripLocator;
@property (copy, nonatomic) CTEDataTypes *SegmentLocator;

- (NSString*)getIconKey;
- (CTEComment*)getLastComment;
- (id)valueForUndefinedKey:(NSString*)key;

@end
