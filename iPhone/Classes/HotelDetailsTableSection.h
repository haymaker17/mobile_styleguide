//
//  HotelDetailsTableSection.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HotelDetailsTableSection : NSObject
{
	NSUInteger type;
	NSUInteger index;
}

extern const NSUInteger HOTEL_DETAILS_TABLE_SECTION_TYPE_SUMMARY;
extern const NSUInteger HOTEL_DETAILS_TABLE_SECTION_TYPE_FEES;
extern const NSUInteger HOTEL_DETAILS_TABLE_SECTION_TYPE_DETAIL;

@property (nonatomic) NSUInteger	type;
@property (nonatomic) NSUInteger	index;

-(id)initWithTableSectionType:(NSUInteger)tableSectionType;

@end
