//
//  UserProfileManager.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "BaseManager.h"
#import "EntityProfile.h"

@interface UserProfileManager : BaseManager
{
    NSString *entityName;
}

+(UserProfileManager *)sharedInstance;
-(id)init;
-(instancetype)initWithContext:(NSManagedObjectContext*)inContext;

-(EntityProfile *)makeNew;
-(EntityProfile *)fetchOrMake;


@end
