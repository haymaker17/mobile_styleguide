//
//  EntityGovExpenseExtension.h
//  ConcurMobile
//
//  Created by charlottef on 1/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EntityGovExpense.h"

@interface EntityGovExpense (EntityGovExpenseExtension)
{
}

#pragma mark - Fetch methods
+(EntityGovExpense *) fetchById:(NSString*)ccExpId inContext:(NSManagedObjectContext*)context;

#pragma mark - Deletion
-(void) deleteEntityObject;

@end

