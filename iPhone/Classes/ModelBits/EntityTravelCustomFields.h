//
//  EntityTravelCustomFields.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import "EntityTravelCustomFieldsInterface.h"

@class EntityTravelCustomFieldAttribute;

@interface EntityTravelCustomFields : NSManagedObject <EntityTravelCustomFieldsInterface>

@property (nonatomic, strong) NSString * attributeValue;
@property (nonatomic, strong) NSString * selectedAttributeOptionText;
@property (nonatomic, strong) NSString * dataType;
@property (nonatomic, strong) NSNumber * maxLength;
@property (nonatomic, strong) NSNumber * minLength;
@property (nonatomic, strong) NSNumber * dependencyAttributeId;
@property (nonatomic, strong) NSString * attributeTitle;
@property (nonatomic, strong) NSString * attributeId;
@property (nonatomic, strong) NSNumber * required;
@property (nonatomic, strong) NSNumber * hasDependency;
@property (nonatomic, strong) NSNumber * displayAtStart;
@property (nonatomic, strong) NSSet *relAttribute;
@property (nonatomic, strong) NSNumber * largeValueCount;
@end

@interface EntityTravelCustomFields (CoreDataGeneratedAccessors)

- (void)addRelAttributeObject:(EntityTravelCustomFieldAttribute *)value;
- (void)removeRelAttributeObject:(EntityTravelCustomFieldAttribute *)value;
- (void)addRelAttribute:(NSSet *)values;
- (void)removeRelAttribute:(NSSet *)values;
@end
