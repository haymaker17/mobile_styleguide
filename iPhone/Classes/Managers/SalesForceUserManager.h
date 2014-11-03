//
//  SalesForceUserManager.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "BaseManager.h"
#import "EntitySalesForceUser.h"

@interface SalesForceUserManager : BaseManager
{
}

+(SalesForceUserManager*)sharedInstance;

-(EntitySalesForceUser*) fetchUser;
-(NSString*) getAccessToken;
-(NSString*) getInstanceUrl;

-(void) clearSalesForceCredentials;
- (void)getPortraitForImageView:(UIImageView *)view;

@end
