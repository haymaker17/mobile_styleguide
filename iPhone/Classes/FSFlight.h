//
//  FSFlight.h
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FSClassOfService.h"

@interface FSFlight : NSObject
{
    NSString *carrier;
    NSString *fltNum;
    NSString *operatingCarrier;
    NSString *depAirp;
    NSDate *depDateTime;
    NSString *arrAirp;
    NSDate *arrDateTime;
    int numStops;
    NSString *aircraftCode;
    NSMutableArray *classesOfService;
}

@property (nonatomic, strong) NSString *carrier;
@property (nonatomic, strong) NSString *fltNum;
@property (nonatomic, strong) NSString *operatingCarrier;
@property (nonatomic, strong) NSString *depAirp;
@property (nonatomic, strong) NSDate *depDateTime;
@property (nonatomic, strong) NSString *arrAirp;
@property (nonatomic, strong) NSDate *arrDateTime;
@property int numStops;
@property (nonatomic, strong) NSString *aircraftCode;
@property (nonatomic, strong) NSMutableArray *classesOfService;

-(FSClassOfService*) getCurrentClassOfService;
-(void)startTag:(NSString*)tag withAttributeData:(NSDictionary *) dict;
-(void)endTag:(NSString*)tag withText:(NSString*)text;

@end
