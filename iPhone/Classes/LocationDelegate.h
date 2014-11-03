//
//  LocationDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@class LocationResult;


@protocol LocationDelegate

-(void)locationSelected:(LocationResult*)locationResult tag:(NSString*)tag;

@end
