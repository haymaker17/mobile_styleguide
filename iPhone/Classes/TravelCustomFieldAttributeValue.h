//
//  TravelCustomFieldAttributeValue.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TravelCustomFieldAttributeValue : NSObject
{
    NSString *attributeId;
    NSString *optionText;
    NSString *value;
    NSString *valueId;
    NSInteger sequence;
}

@property (nonatomic, strong) NSString *attributeId;
@property (nonatomic, strong) NSString *optionText;
@property (nonatomic, strong) NSString *value;
@property (nonatomic, strong) NSString *valueId;
@property NSInteger sequence; 

@end
