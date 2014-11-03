//
//  FindAddress.h
//  ConcurMobile
//
//  Created by Paul Kramer on 2/4/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "MapViewController.h"



@interface FindAddress : MsgResponder {

	NSString				*latitude, *longitude, *address;
	RootViewController		*rootVC;
	MapViewController		*mapView;
}

@property (strong, nonatomic) NSString *latitude;
@property (strong, nonatomic) NSString *longitude;
@property (strong, nonatomic) NSString *address;

@property (strong, nonatomic) MapViewController		*mapView;

@property (strong, nonatomic) RootViewController *rootVC;

//-(void) getLocation:(NSString *)longi latString:(NSString *)lati;
//-(void) init:(RootViewController *)rootViewController;


@end
