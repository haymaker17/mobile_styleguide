//
//  PostMessageManager.m
//  ConcurMobile
//
//  Created by yiwen on 10/27/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//
// TODO - 1. Add seq# or timestamp to PostMessage.  
  

#import "PostMessageManager.h"

static PostMessageManager *sharedInstance;

@implementation PostMessageManager

+(PostMessageManager*)sharedInstance
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
				sharedInstance = [[PostMessageManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(EntityPostMessage *) makeNew
{
    return ((EntityPostMessage *)[super makeNew:@"EntityPostMessage"]);
}

-(EntityPostMessageBody *) makeNewBody
{
    return ((EntityPostMessageBody *)[super makeNew:@"EntityPostMessageBody"]);
}

-(void) clearAll
{
    NSArray *aHomeData = [self fetchAll:@"EntityPostMessage"];
    
    for(EntityPostMessage *entity in aHomeData)
    {
        [self deleteObj:entity];
    }
}

-(EntityPostMessage *) getMessageByUUID:(NSString *)uuid
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityPostMessage" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(uuid = %@)", uuid];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    EntityPostMessage * result = aFetch == nil || [aFetch count]==0? nil : (EntityPostMessage *)aFetch[0];
    return result;
}

-(void) deleteMessage:(NSString*)uuid
{
    EntityPostMessage* msg = [self getMessageByUUID:uuid];
    [self deleteObj:msg];
}
@end
