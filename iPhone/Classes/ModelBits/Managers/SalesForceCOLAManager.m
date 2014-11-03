//
//  SalesForceCOLAManager.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "SalesForceCOLAManager.h"

static SalesForceCOLAManager *sharedInstance;

@implementation SalesForceCOLAManager

+(SalesForceCOLAManager*)sharedInstance
{
    if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[SalesForceCOLAManager alloc] init];
			}
		}
		return sharedInstance;
	}

}

-(EntitySalesOpportunity*) fetchOpportunityByOppId:(NSString*)oppId
{
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(opportunityId = %@)", oppId];
    return (EntitySalesOpportunity*)[self fetchFirst:@"EntitySalesOpportunity" withCondition: pred];
}


-(EntitySalesOpportunity*) fetchOpportunityByOppName:(NSString*)oppName
{
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(opportunityName = %@)", oppName];
    return (EntitySalesOpportunity*)[self fetchFirst:@"EntitySalesOpportunity" withCondition: pred];
}

-(EntitySalesOpportunity*) fetchTopOpportunityByContactName:(NSString*)contactName
{
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(contactName = %@)", contactName];
    return (EntitySalesOpportunity*)[self fetchFirst:@"EntitySalesOpportunity" withCondition: pred];
}

-(NSArray*) fetchOpportunitiesByCity:(NSString*)city withContext:(NSManagedObjectContext*) context
{
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(accountCity BEGINSWITH %@)", city];
    return (NSArray*)[self fetch:@"EntitySalesOpportunity" withCondition: pred withContext:context];
}

-(void) deleteOpportunitiesByCity:(NSString*)city withContext:(NSManagedObjectContext*)context
{
    NSArray *oppList = [self fetchOpportunitiesByCity:city withContext:context];
    for (EntitySalesOpportunity *opp in oppList)
    {
        [self deleteObj:opp withContext:context];
    }
}

@end
