//
//  UberTime.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UberTime : NSObject

@property (nonatomic, strong)   NSString    *productId;
@property (nonatomic, strong)   NSString    *displayName;
@property (nonatomic)           int         estimate;

-(id)initWithJSON:(NSDictionary *)json;

@end
