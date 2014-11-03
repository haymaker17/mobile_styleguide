//
//  EntitySalesForceTrip.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySalesForceTrip : NSManagedObject

@property (nonatomic, strong) NSDate * startDate;
@property (nonatomic, strong) NSDate * endDate;
@property (nonatomic, strong) NSString * locator;
@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSString * identifier;

@end
