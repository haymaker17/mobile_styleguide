//
//  AirFilterManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/8/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AirFilterManager : NSObject {
    NSManagedObjectContext      *_context;
    NSString *entityName;
}

@property (nonatomic, strong) NSManagedObjectContext *context;
@property (nonatomic, strong) NSString *entityName;

-(void) saveIt:(NSManagedObject *) obj;
-(BOOL) hasAny;
-(NSManagedObject *) makeNew;
-(NSManagedObject *) fetchFirst;
-(NSArray *) fetchAll;
-(NSManagedObject *) fetchOrMake:(NSString *)key;
-(void) deleteObj:(NSManagedObject *)obj;
-(void) deleteAll;

+(AirFilterManager*)sharedInstance;
-(AirFilterManager*)init;
-(instancetype)initWithContext:(NSManagedObjectContext*)inContext;

-(NSManagedObject *) fetchByFareIdSegmentPosFlightPos:(NSString *)fareId segPos:(int)segPos flightPos:(int)flightPos;
-(NSArray*) fetchByFareId:(NSString *)fareId;
-(NSArray *) fetchByFareIdSegmentPos:(NSString *)fareId segPos:(int)segPos;

-(NSArray *) fetchAirFilters: (NSString*)fareId;
@end
