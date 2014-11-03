//
//  OAuthConsumer.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface OAuthConsumer : NSObject

-(NSString*) oauthHeaderLegs:(int)legs httpMethod:(NSString*)httpMethod url:(NSString*)url;

@end
