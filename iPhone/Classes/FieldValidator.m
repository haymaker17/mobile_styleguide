//
//  FieldValidator.m
//  ConcurMobile
//
//  Created by Richard Puckett on 10/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "EntityTravelCustomFields.h"
#import "FieldValidator.h"

@implementation FieldValidator

+ (BOOL)isOptionalStringValid:(NSString *)str forCustomField:(EntityTravelCustomFields *)field {
    BOOL isValid = NO;
    
    NSInteger length = str.length;
    NSInteger min = [field.minLength integerValue];
    NSInteger max = [field.maxLength integerValue];
    
    if (min <= max && length >= min && length <= max) {
        isValid = YES;
    } else if (min > max) {
        isValid = YES;
    } else if ([field.dataType isEqualToString:@"number"]) {
        isValid = YES;
    } else if (min < 0 && max < 0) {
        isValid = YES;
    }
    
    return isValid;
}

+ (BOOL)isRequiredStringValid:(NSString *)str forCustomField:(EntityTravelCustomFields *)field {
    BOOL isValid = NO;
    
    isValid = [str lengthIgnoreWhitespace] > 0;
    
    return isValid;
}

+ (BOOL)isStringValid:(NSString *)str forCustomField:(EntityTravelCustomFields *)field {
    BOOL isValid = NO;
    
    if ([field.required boolValue] == YES) {
        isValid = [self isRequiredStringValid:str forCustomField:field];
    } else {
        isValid = [self isOptionalStringValid:str forCustomField:field];
    }
    
    return isValid;
}

@end
