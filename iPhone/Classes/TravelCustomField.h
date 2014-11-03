//
//  TravelCustomField.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TravelCustomField : NSObject 
{
    NSString *attributeId;
    NSString *attributeTitle;
    NSMutableArray *attributeValues;
    NSString *attributeValue;
    NSString *dataType;
    NSString *dependencyAttributeId;
    NSInteger maxLength;
    NSInteger minLength;
    BOOL      required;
    BOOL      hasDependency;
    BOOL      displayAtStart;
}

@property (nonatomic, strong) NSString *attributeValue;
@property (nonatomic, strong) NSString *attributeId;
@property (nonatomic, strong) NSString *attributeTitle;
@property (nonatomic, strong) NSMutableArray *attributeValues;
@property (nonatomic, strong) NSString *dataType;
@property (nonatomic, strong) NSString *dependencyAttributeId;
@property NSInteger maxLength;
@property NSInteger minLength;
@property BOOL      required;
@property BOOL      hasDependency;
@property BOOL      displayAtStart;
@property BOOL      largeValueCount;
@end
