//
//  OAuthUserManager.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 2/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "OAuthUserManager.h"


NSString * const PROVIDER_FACEBOOK = @"Facebook Mobile";
NSString * const PROVIDER_GOOGLE = @"Google Mobile";

static OAuthUserManager *sharedInstance;

@implementation OAuthUserManager

+(OAuthUserManager*)sharedInstance
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
				sharedInstance = [[OAuthUserManager alloc] init];
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

-(EntityOAuthUser *) makeNew
{
    return ((EntityOAuthUser *)[super makeNew:@"EntityOAuthUser"]);
}


-(void) clearAll
{
    NSArray *list = [self fetchAll:@"EntityOAuthUser"];
    
    for(EntityOAuthUser *entity in list)
    {
        [self deleteObj:entity];
    }
}

-(EntityOAuthUser *) userByProvider:(NSString *)provider
{
    
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityOAuthUser" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(provider = %@)", provider];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if (aFetch != nil && [aFetch count]>0)
        return aFetch[0];
    return nil;
}

-(EntityOAuthUser*) saveUserInfo:(NSString*)provider email:(NSString*)email first:(NSString*) firstName last:(NSString*) lastName externalId:(NSString*) extId
{
    EntityOAuthUser* user = [self userByProvider:provider];
    if (user == nil)
        user = [self makeNew];
    user.email = email;
    user.firstName = firstName;
    user.lastName = lastName;
    user.externalId = extId;
    user.provider = provider;
    [self saveIt:user];
    return user;
}

@end
