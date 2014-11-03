//
//  OpportunitesData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "EntitySalesOpportunity.h"

@interface OpportunitesData : MsgResponderCommon
{
    // Input is destination location,e.g. "San Francisco", or geo coordinates
    NSString                    *city;
    EntitySalesOpportunity      *opp;
}

@property (strong, nonatomic) NSString                          *city;
@property (strong, nonatomic) EntitySalesOpportunity            *opp;
@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;

@end
