//
//  TripManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityTrip.h"
#import "EntityBooking.h"
#import "EntitySegment.h"
#import "EntityFlightStats.h"
#import "EntityViolation.h"
#import "BaseManager.h"

@interface TripManager : BaseManager {
    NSString *entityName;
}

@property (nonatomic, strong) NSString *entityName;

-(void) saveIt:(NSManagedObject *) obj;
-(BOOL) hasAny;
+(EntityTrip *) makeNew:(NSManagedObjectContext*)manContext;
-(NSManagedObject *) fetchFirst;
-(NSArray *) fetchAll;
-(NSManagedObject *) fetchOrMake:(NSString *)key;
-(void) deleteObj:(NSManagedObject *)obj;
-(void) deleteAll;

+(TripManager*)sharedInstance;
-(TripManager*)init;
-(NSArray *) fetchByFareId:(NSString *)key;
-(EntityTrip*) fetchByTripKey:(NSString *)key;
-(EntityTrip*) fetchTripByBookingRecordLocator:(NSString *)recLoc;
-(EntityTrip*) fetchTripByClientLocator:(NSString *)clientLoc;
-(EntityTrip*) fetchByItinLocator:(NSString *)itinLocator;
-(EntitySegment*) fetchBySegmentKey:(NSString*) segmentKey inTrip:(EntityTrip*) trip;
- (NSArray*)fetchSegmentsByType:(NSString*)segType;
- (EntitySegment*)fetchSegmentByIdKey:(NSString*)idKey tripKey:(NSString*) tripKey;

+(EntitySegment *) makeNewSegment:(EntityTrip*)trip manContext:(NSManagedObjectContext*)manContext;
+(EntityBooking *) makeNewBooking:(EntityTrip*)trip manContext:(NSManagedObjectContext*)manContext;
+(EntityFlightStats *) makeNewFlightStat:(EntitySegment*)segment manContext:(NSManagedObjectContext*)manContext;
+(EntityViolation *) makeNewViolation:(EntityTrip *)trip manContext:(NSManagedObjectContext *)manContext;
+(void) saveItWithContext:(NSManagedObject *) obj manContext:(NSManagedObjectContext*)manContext;
+(NSArray *) fetchAllWithContext:(NSManagedObjectContext*) manContext;
+(void) deleteObjWithContext:(NSManagedObject *)obj manContext:(NSManagedObjectContext*)manContext;
+(void) deleteAllWithContext:(NSManagedObjectContext*) manContext;
-(void) resetContext;
@end
