//
//  UserProfileManager.m
//  ConcurMobile
//
//  Created by Ray Chi on 12/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UserProfileManager.h"

__strong static UserProfileManager *sharedInstance = nil;

@implementation UserProfileManager

+(UserProfileManager *)sharedInstance{
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        sharedInstance = [[self alloc] init];
    });
    
    return sharedInstance;
}

-(id)init
{
    self = [super init];
    if(self){
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
    }
    return self;
}

/**
 *  Initiate the manager with a private given context. To seperate read/write the coredata.
 *
 *  @param inContext --- private context if initiated from xml/json parser
 *  @return instance type of the user profile manager
 */
-(instancetype)initWithContext:(NSManagedObjectContext *)inContext
{
    self = [super init];
    if(self){
        self.context = inContext;
    }
    return self;
}

-(EntityProfile *)makeNew;
{
    return ((EntityProfile *)[super makeNew:@"EntityProfile"]);
}


-(EntityProfile *)fetchOrMake
{
    
}

@end
