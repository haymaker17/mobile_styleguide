//
//  CCFlurryLogs.h
//  ConcurMobile
//
//  Created by laurent mery on 23/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CCFlurryLogs : NSObject

+(void)flurryLogEventActionFrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters;

+(void)flurryLogEventOpenViewFrom:(NSString*)sourceViewName to:(NSString*)DestinationViewName parameters:(NSMutableDictionary*)parameters;

+(void)flurryLogEventReturnFromView:(NSString*)sourceViewName to:(NSString*)destinationViewName parameters:(NSMutableDictionary*)parameters;




+(void)flurryLogSpinnerStartTimefrom:(NSString*)sourceViewName action:(NSString*)action;

+(void)flurryLogSpinnerStopTimefrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters;



@end
