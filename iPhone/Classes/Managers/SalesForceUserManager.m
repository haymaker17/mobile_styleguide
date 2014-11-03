//
//  SalesForceUserManager.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "SalesForceUserManager.h"
#import "EntitySalesForceUser.h"
#import "EntityChatterFeedEntry.h"


static SalesForceUserManager *sharedInstance;

@implementation SalesForceUserManager


+(SalesForceUserManager*)sharedInstance
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
				sharedInstance = [[SalesForceUserManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(EntitySalesForceUser*) fetchUser
{
    return (EntitySalesForceUser*)[self fetchFirst:@"EntitySalesForceUser"];
}

-(NSString*) getAccessToken
{
    EntitySalesForceUser *user = [self fetchUser];
    return user.accessToken;
}

-(NSString*) getInstanceUrl
{
    EntitySalesForceUser *user = [self fetchUser];
    return user.instanceUrl;
}

- (void)getPortraitForImageView:(UIImageView *)view
{
    EntitySalesForceUser *user = [self fetchUser];

    NSString *imageCacheName = [NSString stringWithFormat:@"Photo_Small_%@", user.identifier];
    [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:user.smallPhotoUrl RespondToImage:nil IV:view MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:user.accessToken];
}


// need to wipe the salesforce credentials on logout
-(void) clearSalesForceCredentials
{
    EntitySalesForceUser *tmp = [self fetchUser];
    if (tmp != nil) {
        [self deleteObj:tmp];
    }

    [self clearChatterPosts];
}

// need to wipe chatter posts for the next user
-(void)clearChatterPosts
{
    NSError *error;
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityChatterFeedEntry" inManagedObjectContext:self.context];
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    [fetchRequest setEntity:entity];

    NSArray* posts = [self.context executeFetchRequest:fetchRequest error:&error];
    for (int i=0; i<posts.count; i++) {
        EntityChatterFeedEntry *postEntity = posts[i];
        if (postEntity != nil) {
            [self deleteObj:postEntity];
        }
    }
}


@end
