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

@class CTETravelRequestComment;

@interface CTETravelRequestSegment : NSObject

@property (copy, nonatomic) NSString *SegmentTypeName;
@property (copy, nonatomic) NSString *DepartureDate;
@property (copy, nonatomic) NSString *DepartureTime;
@property (copy, nonatomic) NSString *ArrivalDate;
@property (copy, nonatomic) NSString *ArrivalTime;
@property (copy, nonatomic) NSString *FromLocationName;
@property (copy, nonatomic) NSString *ToLocationName;
@property (copy, nonatomic) NSString *FromLocationDetail;
@property (copy, nonatomic) NSString *ToLocationDetail;
@property (copy, nonatomic) NSString *ExchangeRate;
@property (copy, nonatomic) NSString *ForeignAmount;
@property (copy, nonatomic) NSString *ForeignCurrencyName;
@property (copy, nonatomic) NSString *ForeignCurrencyCode;
@property (copy, nonatomic) NSString *PostedAmount;
@property (copy, nonatomic) NSString *RemainingAmount;
@property (copy, nonatomic) NSString *FormTypeCode;
@property (copy, nonatomic) NSString *SegmentTypeCode;
@property (copy, nonatomic) NSString *ApprovedAmount;
@property (copy, nonatomic) NSString *IsAgencyBooked;
@property (copy, nonatomic) NSString *IsSelfBooked;
@property (copy, nonatomic) NSString *LastModifiedDate;
@property (copy, nonatomic) NSString *OutboundSegmentKey;
@property (copy, nonatomic) NSString *CommentCount;
@property (copy, nonatomic) NSArray *CommentsList;
@property (copy, nonatomic) NSString *ExceptionCount;
@property (copy, nonatomic) NSArray *ExceptionsList;
@property (copy, nonatomic) NSString *Custom1;
@property (copy, nonatomic) NSString *Custom2;
@property (copy, nonatomic) NSString *Custom3;
@property (copy, nonatomic) NSString *Custom4;
@property (copy, nonatomic) NSString *Custom5;
@property (copy, nonatomic) NSString *Custom6;
@property (copy, nonatomic) NSString *Custom7;
@property (copy, nonatomic) NSString *Custom8;
@property (copy, nonatomic) NSString *Custom9;
@property (copy, nonatomic) NSString *Custom10;
@property (copy, nonatomic) NSString *Custom11;
@property (copy, nonatomic) NSString *Custom12;
@property (copy, nonatomic) NSString *Custom13;
@property (copy, nonatomic) NSString *Custom14;
@property (copy, nonatomic) NSString *Custom15;
@property (copy, nonatomic) NSString *Custom16;
@property (copy, nonatomic) NSString *Custom17;
@property (copy, nonatomic) NSString *Custom18;
@property (copy, nonatomic) NSString *Custom19;
@property (copy, nonatomic) NSString *Custom20;
@property (copy, nonatomic) NSString *Custom21;
@property (copy, nonatomic) NSString *Custom22;
@property (copy, nonatomic) NSString *Custom23;
@property (copy, nonatomic) NSString *Custom24;
@property (copy, nonatomic) NSString *Custom25;
@property (copy, nonatomic) NSString *Custom26;
@property (copy, nonatomic) NSString *Custom27;
@property (copy, nonatomic) NSString *Custom28;
@property (copy, nonatomic) NSString *Custom29;
@property (copy, nonatomic) NSString *Custom30;
@property (copy, nonatomic) NSString *Custom31;
@property (copy, nonatomic) NSString *Custom32;
@property (copy, nonatomic) NSString *Custom33;
@property (copy, nonatomic) NSString *Custom34;
@property (copy, nonatomic) NSString *Custom35;
@property (copy, nonatomic) NSString *Custom36;
@property (copy, nonatomic) NSString *Custom37;
@property (copy, nonatomic) NSString *Custom38;
@property (copy, nonatomic) NSString *Custom39;
@property (copy, nonatomic) NSString *Custom40;

//airfr
@property (copy, nonatomic) NSString *RecordLocator;
@property (copy, nonatomic) NSString *FlightNumber;
@property (copy, nonatomic) NSString *ClassOfServiceCode;
@property (copy, nonatomic) NSString *TripLocator;
@property (copy, nonatomic) NSString *SegmentLocator;

- (NSString *)getIconKey;
-(CTETravelRequestComment *)getLastComment;
- (id)valueForUndefinedKey:(NSString *)key;

@end
