//
//  AppCenterResponseParser.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AppCenterResponseParser : NSObject

@property (nonatomic, strong)   NSArray *appListings;
@property (nonatomic, strong)   NSString *info;

-(id) initWithJsonResponse:(NSDictionary*)json;

@end
