//
//  UberParser.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UberParser : NSObject

@property (nonatomic, strong)   NSArray     *prices;
@property (nonatomic, strong)   NSArray     *times;

-(id) initWithPricesJSON:(NSDictionary *)response;
-(id) initWithTimesJSON:(NSDictionary *)response;

@end
