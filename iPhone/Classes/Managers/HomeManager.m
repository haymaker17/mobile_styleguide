//
//  HomeManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/16/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "HomeManager.h"
static HomeManager *sharedInstance;

@implementation HomeManager

+(HomeManager*)sharedInstance
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
				sharedInstance = [[HomeManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(id)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
	}
    
	return self;
}


-(EntityHome *) makeNew
{
    return ((EntityHome *)[super makeNew:@"EntityHome"]);
}


-(void) clearAll
{
    NSArray *aHomeData = [self fetchAll:@"EntityHome"];
    
    for(EntityHome *entity in aHomeData)
    {
        [self deleteObj:entity];
    }
}

-(NSManagedObject *) fetchHome:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHome" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(key = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}
@end
