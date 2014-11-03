//
//  UberPrice.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UberPrice : NSObject

@property (nonatomic, strong)   NSString    *currencyCode;
@property (nonatomic, strong)   NSString    *displayName;
@property (nonatomic, strong)   NSString    *estimate;
@property (nonatomic)           int         highEstimate;
@property (nonatomic)           int         lowEstimate;
@property (nonatomic, strong)   NSString    *productId;
@property (nonatomic )          float       surgeMultiplier;

-(id)initWithJSON:(NSDictionary *)json;

@end
