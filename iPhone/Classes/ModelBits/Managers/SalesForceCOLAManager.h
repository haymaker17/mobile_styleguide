//
//  SalesForceCOLAManager.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntitySalesOpportunity.h"

@interface SalesForceCOLAManager : BaseManager

+(SalesForceCOLAManager*)sharedInstance;

-(EntitySalesOpportunity*) fetchOpportunityByOppId:(NSString*)oppId;

-(EntitySalesOpportunity*) fetchOpportunityByOppName:(NSString*)oppName;
-(EntitySalesOpportunity*) fetchTopOpportunityByContactName:(NSString*)contactName;

-(NSArray*) fetchOpportunitiesByCity:(NSString*)city withContext:(NSManagedObjectContext*)context;
-(void) deleteOpportunitiesByCity:(NSString*)city withContext:(NSManagedObjectContext*)context;

@end
