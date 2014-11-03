//
//  OAuthUserManager.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 2/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntityOAuthUser.h"

// Constants for Providers
extern NSString * const PROVIDER_FACEBOOK;
extern NSString * const PROVIDER_GOOGLE;

@interface OAuthUserManager : BaseManager

+(OAuthUserManager*)sharedInstance;
-(id)init;

-(EntityOAuthUser *) makeNew;

-(void) clearAll;
-(EntityOAuthUser *) userByProvider:(NSString *)provider;
-(EntityOAuthUser*) saveUserInfo:(NSString*)provider email:(NSString*)email first:(NSString*) firstName last:(NSString*) lastName externalId:(NSString*) extId;

@end
