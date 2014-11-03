//
//  IpmMessage.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface IpmMessage : NSObject

@property (nonatomic, strong)   NSString    *target;
@property (nonatomic, strong)   NSString    *adUnitId;
@property (nonatomic, strong)   NSString    *msgKey;
@property (nonatomic, strong)   NSDictionary *additionalParameters;

@end
