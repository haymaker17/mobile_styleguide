//
//  EntityTravelCustomFieldsInterface.h
//  ConcurMobile
//
//  Created by Richard Puckett on 10/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

@protocol EntityTravelCustomFieldsInterface <NSObject>

@property (nonatomic, strong) NSString *attributeValue;
@property (nonatomic, strong) NSString *selectedAttributeOptionText;
@property (nonatomic, strong) NSString *dataType;
@property (nonatomic, strong) NSNumber *maxLength;
@property (nonatomic, strong) NSNumber *minLength;
@property (nonatomic, strong) NSNumber *dependencyAttributeId;
@property (nonatomic, strong) NSString *attributeTitle;
@property (nonatomic, strong) NSString *attributeId;
@property (nonatomic, strong) NSNumber *required;
@property (nonatomic, strong) NSNumber *hasDependency;
@property (nonatomic, strong) NSNumber *displayAtStart;
@property (nonatomic, strong) NSSet *relAttribute;
@property (nonatomic, strong) NSNumber *largeValueCount;

@end
