//
//  AirRuleManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AirRuleManager : NSObject {
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

+(AirRuleManager*)sharedInstance;
//-(AirRuleManager*)init;
-(instancetype)initWithContext:( NSManagedObjectContext *)inContext;
-(NSManagedObject *) fetchMostSevre:(NSString *)key;
-(NSArray *) fetchByFareId:(NSString *)key;
@end
