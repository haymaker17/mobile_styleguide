//
//  SegmentStuff.h
//  ConcurMobile
//
//  Created by Paul Kramer on 10/4/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ExSystem.h" 

#import "SegmentData.h"

@interface SegmentStuff : NSObject {
	RootViewController *rootViewController;
}

@property (strong, nonatomic) RootViewController *rootViewController;


-(NSMutableArray *) fillAirSections:(EntitySegment *)segment;
-(NSMutableArray *)fillRailSections:(EntitySegment *)segment;
-(NSMutableArray *)fillDiningSections:(EntitySegment *)segment;
-(NSMutableArray *)fillRideSections:(EntitySegment *)segment;
-(NSMutableArray *) fillHotelSections:(EntitySegment *)segment;
-(NSMutableArray *) fillCarSections:(EntitySegment *)segment;
//-(NSMutableArray *) fillOfferRows:(EntitySegment *)segment;

-(NSString *)getAircraftURL:(NSString *)vendorCode AircraftCode:(NSString *)aircraftCode;
@end
