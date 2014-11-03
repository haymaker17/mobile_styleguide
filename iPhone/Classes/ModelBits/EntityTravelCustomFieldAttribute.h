//
//  EntityTravelCustomFieldAttribute.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 5/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityTravelCustomFields;

@interface EntityTravelCustomFieldAttribute : NSManagedObject

@property (nonatomic, strong) NSNumber * sequence;
@property (nonatomic, strong) NSString * value;
@property (nonatomic, strong) NSString * valueId;
@property (nonatomic, strong) NSString * optionText;
@property (nonatomic, strong) NSString * attributeId;
@property (nonatomic, strong) EntityTravelCustomFields *relCustomField;

@end
