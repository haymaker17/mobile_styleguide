//
//  UberParser.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "UberParser.h"
#import "UberPrice.h"
#import "UberTime.h"

@implementation UberParser

-(id) initWithPricesJSON:(NSDictionary *)response
{
    self = [self init];
    if (self) {
        NSArray *pricesArray = [response objectForKey:@"prices"];
        
        if (pricesArray != nil)
        {
            NSMutableArray *prices = [[NSMutableArray alloc] init];
            for (int i=0; i<pricesArray.count; i++) {
                UberPrice *price = [[UberPrice alloc] initWithJSON:[pricesArray objectAtIndex:i]];
                [prices addObject:price];
            }
            [self setPrices:prices];
        }
    }
    return self;
}

-(id) initWithTimesJSON:(NSDictionary *)response
{
    self = [self init];
    if (self) {
        NSArray *timesArray = [response objectForKey:@"times"];
        
        if (timesArray != nil)
        {
            NSMutableArray *times = [[NSMutableArray alloc] init];
            for (int i=0; i<timesArray.count; i++) {
                UberTime *time = [[UberTime alloc] initWithJSON:[timesArray objectAtIndex:i]];
                [times addObject:time];
            }
            [self setTimes:times];
        }
    }
    return self;
}

@end
