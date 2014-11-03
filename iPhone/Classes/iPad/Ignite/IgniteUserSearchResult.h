//
//  IgniteUserSearchResult.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface IgniteUserSearchResult : NSObject
{
    NSString *identifier;
    NSString *name; // User friendly name, like "Charlotte Fallarme"
    NSString *userName; // Less friendly name, like "charlotte.fallarme@concur.com", although not necessarily an email address.
}

@property (nonatomic, strong) NSString *identifier;
@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSString *userName;
@end
