//
//  BookingData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SegmentData.h"


@interface BookingData : NSObject 
{
	NSString			*recordLocator, *travelConfigId, *bookingData, *agencyPCC, *companyAccountingCode, *dateBookedLocal, *type, *label, *gds, *bookSource;
	BOOL				isCliqbookSystemOfRecord;
	NSMutableDictionary	*segments, *airTickets, *airQuotes, *passengers, *phoneNumbers, *remarks, *charges;
	NSMutableArray		*segmentKeys, *airTicketKeys, *airQuoteKeys, *passengerKeys, *phoneNumberKeys, *remarkKeys, *chargeKeys;
	SegmentData			*segment; //current segment
}

@property (nonatomic, strong) NSString *recordLocator;
@property (nonatomic, strong) NSString *type;
@property (nonatomic, strong) NSString *label;
@property (nonatomic, strong) NSString *gds;
@property (nonatomic, strong) NSString *bookSource;
@property (nonatomic, strong) NSString *agencyPCC;
@property (nonatomic, strong) NSString *companyAccountingCode;
@property (nonatomic, strong) NSString *travelConfigId;
@property (nonatomic) BOOL isCliqbookSystemOfRecord;
@property (nonatomic, strong) NSMutableDictionary *segments;
@property (nonatomic, strong) NSMutableArray *segmentKeys;
@property (nonatomic, strong) SegmentData *segment;


-(void)initWithSegments;
-(void)finishSegment;

-(void)setSegmentsBookingKey:(NSString *)bookingKey;
+(int)getGDSId:(NSString *)bookingSource;

@end
