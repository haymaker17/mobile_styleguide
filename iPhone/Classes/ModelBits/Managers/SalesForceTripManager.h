//
//  SalesForceTripManager.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntitySalesForceTrip.h"

@interface SalesForceTripManager : NSObject
{
}

+(EntitySalesForceTrip*) lookupTripBySalesforceId:(NSString*)salesforceId inContext:(NSManagedObjectContext*)context;
+(EntitySalesForceTrip*) lookupTripByLocator:(NSString*)locator inContext:(NSManagedObjectContext*)context;

@end
