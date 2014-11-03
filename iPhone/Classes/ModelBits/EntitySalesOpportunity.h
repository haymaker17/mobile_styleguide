//
//  EntitySalesOpportunity.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/22/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySalesOpportunity : NSManagedObject

@property (nonatomic, strong) NSString * contactName;
@property (nonatomic, strong) NSDecimalNumber * opportunityAmount;
@property (nonatomic, strong) NSString * accountCity;
@property (nonatomic, strong) NSString * opportunityName;
@property (nonatomic, strong) NSString * contactId;
@property (nonatomic, strong) NSString * accountAddress;
@property (nonatomic, strong) NSString * accountName;
@property (nonatomic, strong) NSString * opportunityId;

@end
