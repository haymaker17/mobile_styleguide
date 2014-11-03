//
//  SimpleCreditCard.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SimpleCreditCard : NSObject

@property (copy, nonatomic) NSString *cardId;
@property (copy, nonatomic) NSString *cardName;
@property (assign) BOOL isDefault;
@property (copy, nonatomic) NSString *lastFourDigits;

@end
