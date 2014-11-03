//
//  AirFilterSummaryManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class EntityAirViolation;
@class EntityAirFilterSummary;

@interface AirFilterSummaryManager : NSObject {
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

+(AirFilterSummaryManager*)sharedInstance;
-(AirFilterSummaryManager*)init;
-(instancetype)initWithContext:(NSManagedObjectContext*)inContext;
-(EntityAirViolation*) makeNewViolation;
-(EntityAirViolation*) fetchHighestEnforcement:(EntityAirFilterSummary*) air;

@end
