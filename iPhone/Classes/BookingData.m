//
//  BookingData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "BookingData.h"


@implementation BookingData

@synthesize recordLocator;
@synthesize type;
@synthesize label;
@synthesize isCliqbookSystemOfRecord;
@synthesize segments;
@synthesize segmentKeys;
@synthesize segment, gds, bookSource, agencyPCC, companyAccountingCode, travelConfigId;


-(void)initWithSegments
{
	segments = [[NSMutableDictionary alloc] init];
	segmentKeys = [[NSMutableArray alloc] init];
}

-(void)setSegmentsBookingKey:(NSString *)bookingKey
{
	for(NSString *segKey in segments)
	{
		SegmentData *seg = segments[segKey];
		[seg setBookingKey:bookingKey];
	}
}

-(void)finishSegment
{

//	if (segments == nil)
//	{
//		segments = [[NSMutableDictionary alloc] init];
//	}
//	
//	if (segmentKeys == nil)
//	{
//		segmentKeys = [[NSMutableArray alloc] init];
//	}
	
	if (self.segment.idKey == nil)
	{
		//NSLog(@"finishSegment:self.segment.idKey DOES NOT EXIST self.type=%@", type);
		////NSLog(@"finishSegment:self.segment.cliqbookId=%@", segment.cliqbookId);
	}
	else
	{
		////NSLog(@"BookingData::finishSegment:self.segment.cliqbookId=%@", segment.cliqbookId);
		////NSLog(@"BookingData::finishSegment: self.segment retain count=%d", [segment retainCount]);
		segments[self.segment.idKey] = self.segment;
		[segmentKeys addObject:self.segment.idKey];
		//NSLog(@"TYPE=%@; idKEY=%@", segment.type, segment.idKey);
	}
	//[segment release];  I should never release here, the dealloc method takes care of it... need to do the realloc check for count and release as needed.

}


+(int)getGDSId:(NSString *)bookingSource
{
	if ([bookingSource isEqualToString:@"Apollo"]) {
		return 1;
	} else if ([bookingSource isEqualToString:@"Galileo"]) {
		return 2;
	} else if ([bookingSource isEqualToString:@"Sabre"]) {
		return 4;
	} else if ([bookingSource isEqualToString:@"Farechase"]) {
		return 8;
	} else if ([bookingSource isEqualToString:@"Worldspan"]) {
		return 16;
	} else if ([bookingSource isEqualToString:@"Amadeus"]) {
		return 32;
	} else if ([bookingSource isEqualToString:@"Alternate"]) {
		return 64;
	} else if ([bookingSource isEqualToString:@"Ita"]) {
		return 128;
	} else if ([bookingSource isEqualToString:@"G2"]) {
		return 256;
	} else if ([bookingSource isEqualToString:@"Navitaire"]) {
		return 512;
	} else if ([bookingSource isEqualToString:@"NewSkies"]) {
		return 1024;
	} else if ([bookingSource isEqualToString:@"VirginBlue"]) {
		return 2048;
	} else if ([bookingSource isEqualToString:@"AirCanada"]) {
		return 4096;
	} else if ([bookingSource isEqualToString:@"DeutscheBahn"]) {
		return 8192;
	} else if ([bookingSource isEqualToString:@"SNCF"]) {
		return 16384;
	} else if ([bookingSource isEqualToString:@"Rail1"]) {
		return 32768;
	} else if ([bookingSource isEqualToString:@"Pegasus"]) {
		return 131072;
	} else {
		return 268435456;
	}
}

@end
