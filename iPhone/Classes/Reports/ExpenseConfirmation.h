//
//  ExpenseConfirmation.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ExpenseConfirmation : NSObject
{
    NSString            *confirmationKey;
    NSString            *text;
    NSString            *title;
}

@property (nonatomic, strong) NSString      *confirmationKey;
@property (nonatomic, strong) NSString      *text;
@property (nonatomic, strong) NSString      *title;

+ (NSMutableDictionary*) getXmlToPropertyMap;

@end
