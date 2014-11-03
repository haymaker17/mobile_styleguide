//
//  FieldValidator.h
//  ConcurMobile
//
//  Created by Richard Puckett on 10/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface FieldValidator : NSObject

+ (BOOL)isStringValid:(NSString *)str forCustomField:(EntityTravelCustomFields *)field;

@end
