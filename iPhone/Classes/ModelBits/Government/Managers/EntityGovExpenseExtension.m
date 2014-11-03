//
//  EntityGovExpenseExtension.m
//  ConcurMobile
//
//  Created by charlottef on 1/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EntityGovExpenseExtension.h"

@implementation EntityGovExpense (EntityGovExpenseExtension)

+(EntityGovExpense *) fetchById:(NSString*)ccExpId inContext:(NSManagedObjectContext*)context;
{
    if (ccExpId == nil)
        return nil;
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityGovExpense" inManagedObjectContext:context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(ccExpId = %@)", ccExpId];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
	{
        return [aFetch objectAtIndex:0];
	}
    
    return nil;
}

-(void) deleteEntityObject
{
    NSManagedObjectContext *context = self.managedObjectContext;
    [context deleteObject:self];
    NSError *error = nil;
    if (![context save:&error])
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
}

@end
