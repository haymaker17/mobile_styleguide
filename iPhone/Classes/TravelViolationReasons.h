//
//  TravelViolationReasons.h
//  ConcurMobile
//
//  Created by ernest cho on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TravelViolationReasons : NSObject
{
    NSMutableDictionary *violationReasons;
}

@property (nonatomic, strong) NSMutableDictionary *violationReasons;

-(NSMutableArray*)getReasonsFor:(NSArray*)violations;

+(TravelViolationReasons*)getSingleton;
+(void)setSingleton:(TravelViolationReasons*)violationReasons;

@end
