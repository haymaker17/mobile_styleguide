//
//  EntitySalesForceUser.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySalesForceUser : NSManagedObject

@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSString * identifier;
@property (nonatomic, strong) NSString * smallPhotoUrl;
@property (nonatomic, strong) NSString * accessToken;
@property (nonatomic, strong) NSString * instanceUrl;

@end
