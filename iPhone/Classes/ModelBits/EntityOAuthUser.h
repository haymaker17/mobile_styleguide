//
//  EntityOAuthUser.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 2/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityOAuthUser : NSManagedObject

@property (nonatomic, strong) NSString * externalId;
@property (nonatomic, strong) NSString * lastName;
@property (nonatomic, strong) NSString * provider;
@property (nonatomic, strong) NSString * firstName;
@property (nonatomic, strong) NSString * extra_3;
@property (nonatomic, strong) NSString * extra_5;
@property (nonatomic, strong) NSString * company;
@property (nonatomic, strong) NSString * extra_2;
@property (nonatomic, strong) NSString * extra_4;
@property (nonatomic, strong) NSString * email;
@property (nonatomic, strong) NSString * extra_1;
@property (nonatomic, strong) NSString * extra_6;

@end
