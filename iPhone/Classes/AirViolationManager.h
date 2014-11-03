//
//  AirViolationManager.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityAirViolation.h"
#import "PolicyViolationConstants.h"


@interface AirViolationManager : NSObject {
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

+(AirViolationManager*)sharedInstance;
//-(AirViolationManager*)init;
-(instancetype)initWithContext:(NSManagedObjectContext *)inContext;
-(NSArray *) fetchByFareId:(NSString *)key;
@end
