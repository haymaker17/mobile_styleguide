//
//  TravelRequestVC.h
//  ConcurMobile
//
//  Created by laurent mery on 20/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TravelRequest.h"

#import "UIColor+ConcurColor.h"
#import "WaitViewController.h"
#import "UIView+Styles.h"


@interface TravelRequestVC : UIViewController

@property (copy, nonatomic) NSString *callerViewName;

/*
 *  - static is important because we need to get this value, sometimes, before instantiation
 * with flurry to get the name of the next view
 */
+(NSString*)viewName;

-(void)flurryLogEventActionFrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters;
-(void)flurryLogEventOpenViewFrom:(NSString*)sourceViewName to:(NSString*)DestinationViewName parameters:(NSMutableDictionary*)parameters;
-(void)flurryLogEventReturnFromView:(NSString*)sourceViewName toOrNil:(NSString*)destinationViewName parameters:(NSMutableDictionary*)parameters;

-(void)flurryLogSpinnerStartTimefrom:(NSString*)sourceViewName action:(NSString*)action;
-(void)flurryLogSpinnerStopTimefrom:(NSString*)sourceViewName action:(NSString*)action parameters:(NSDictionary*)parameters;

-(void)waitIn;
-(void)waitOut;
@end
