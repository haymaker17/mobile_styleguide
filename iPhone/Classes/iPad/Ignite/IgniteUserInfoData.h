//
//  IgniteUserInfoData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MsgResponder.h"
#import "EntitySalesForceUser.h"

@interface IgniteUserInfoData : MsgResponderCommon
{
    NSString *accessToken;
    NSString *instanceUrl;
    NSString *profileUserId;
    NSString *profileName;
    NSString *profileSmallPhotoUrl;
    
    EntitySalesForceUser *user;
}

@property (nonatomic, strong) NSString *accessToken;
@property (nonatomic, strong) NSString *instanceUrl;
@property (nonatomic, strong) NSString *profileUserId;
@property (nonatomic, strong) NSString *profileName;
@property (nonatomic, strong) NSString *profileSmallPhotoUrl;

@property (nonatomic, strong) EntitySalesForceUser *user;
@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;

@end
