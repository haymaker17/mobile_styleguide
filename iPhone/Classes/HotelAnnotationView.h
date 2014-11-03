//
//  HotelAnnotationView.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MapKit.h>


@class HotelMapViewController;


@interface HotelAnnotationView : MKPinAnnotationView
{
	HotelMapViewController* __weak mapController;
	NSUInteger				hotelIndex;
}

@property (nonatomic, weak) HotelMapViewController*	mapController;
@property (nonatomic) NSUInteger						hotelIndex;

@end
