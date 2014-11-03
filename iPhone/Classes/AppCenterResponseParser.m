//
//  AppCenterResponseParser.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AppCenterResponseParser.h"
#import "AppCenterListing.h"

@implementation AppCenterResponseParser

-(id) initWithJsonResponse:(NSDictionary*)json
{
    self = [self init];
    if (self) {
        NSArray *listOfApps = [json valueForKeyPath:@"data.appsListing"];
        if (listOfApps != nil)
        {
            NSMutableArray *apps = [[NSMutableArray alloc] init];
            for (int i=0; i<listOfApps.count; i++) {
                AppCenterListing *newListing = [[AppCenterListing alloc] initWithJSON:[listOfApps objectAtIndex:i]];
                [apps addObject:newListing];
            }
            [self setAppListings:apps];
        }
        
        NSArray *infoValue = [json objectForKey:@"info"];
        if (infoValue != nil)
        {
            [self setInfo:[infoValue objectAtIndex:0]];
        }
    }
    return self;
}
@end
