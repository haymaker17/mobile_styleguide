//
//  CTECreditCard.h
//  ConcurSDK
//
//  Created by Sally Yan on 7/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTECreditCard : NSObject

@property (nonatomic, strong) NSString *cardId;
@property (nonatomic, strong) NSString *lastFourDigits;
@property (nonatomic, strong) NSString *name;
@property BOOL isDefault;

-(id)initWithJSon:(NSDictionary *)card;
+(NSArray *)parseListOfCreditCards:(NSDictionary *)responseObject;

@end
