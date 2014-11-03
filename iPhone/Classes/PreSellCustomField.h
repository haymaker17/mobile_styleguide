//
//  PreSellCustomField.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 07/10/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PreSellCustomField : NSObject

@property (nonatomic, strong) NSString *dataType;
@property (nonatomic, strong) NSString *itemId;
@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) NSString *defaultValue;
@property (nonatomic, getter = isOptional) BOOL optional;
@property (nonatomic, strong) NSString *userInputValue;
@property (nonatomic, strong) NSString *userInputValueDisplayText;
@property (nonatomic, strong) NSMutableArray *attributeValues;

@end
