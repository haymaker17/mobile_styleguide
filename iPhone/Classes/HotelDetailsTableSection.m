//
//  HotelDetailsTableSection.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelDetailsTableSection.h"


@implementation HotelDetailsTableSection

@synthesize type;
@synthesize index;

const NSUInteger HOTEL_DETAILS_TABLE_SECTION_TYPE_SUMMARY		= 1;
const NSUInteger HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES			= 2;
const NSUInteger HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL		= 3;

-(id)initWithTableSectionType:(NSUInteger)tableSectionType
{
	self = [super init];
	if (self)
    {
        self.type = tableSectionType;
	}
	return self;
}

@end
