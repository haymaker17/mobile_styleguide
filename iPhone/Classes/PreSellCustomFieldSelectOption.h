//
//  PreSellCustomFieldSelectOption.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 10/10/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface PreSellCustomFieldSelectOption : NSObject

@property (nonatomic, strong) NSString *realValue;
@property (nonatomic, strong) NSString *displayValue;
@property (nonatomic, getter = isSelected) BOOL *selected;

@end