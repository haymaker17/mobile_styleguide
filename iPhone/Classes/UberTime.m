//
//  UberTime.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UberTime.h"
#import "JsonParser.h"

@implementation UberTime

-(id)initWithJSON:(NSDictionary *)json
{
    self = [super init];
    if (self) {
        self.productId = [JsonParser getNodeAsString:@"product_id" json:json];
        self.displayName = [JsonParser getNodeAsString:@"display_name" json:json];
        self.estimate = [JsonParser getNodeAsInt:@"estimate" json:json];
    }
    return self;
}

@end
