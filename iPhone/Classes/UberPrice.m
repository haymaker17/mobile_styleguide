//
//  UberPrice.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UberPrice.h"
#import "JsonParser.h"

@implementation UberPrice

-(id)initWithJSON:(NSDictionary *)json
{
    self = [super init];
    if (self) {
        self.currencyCode = [JsonParser getNodeAsString:@"currency_code" json:json];
        self.displayName = [JsonParser getNodeAsString:@"display_name" json:json];
        
        self.estimate = [JsonParser getNodeAsString:@"estimate" json:json];
        self.highEstimate = [JsonParser getNodeAsInt:@"high_estimate" json:json];
        self.lowEstimate = [JsonParser getNodeAsInt:@"low_estimate" json:json];
        self.productId = [JsonParser getNodeAsString:@"product_id" json:json];
        self.surgeMultiplier = [JsonParser getNodeAsFloat:@"surge_multiplier" json:json];
    }
    return self;
}

@end
