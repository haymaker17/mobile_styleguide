//
//  HotelAnnotation.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>


@interface HotelAnnotation : NSObject <MKAnnotation>
{
	CLLocationCoordinate2D coordinate;
	NSString *title;
	NSString *subtitle;
	NSUInteger hotelIndex;
}

@property (nonatomic) CLLocationCoordinate2D	coordinate;
@property (nonatomic, copy) NSString			*title;
@property (nonatomic, copy) NSString			*subtitle;
@property (nonatomic) NSUInteger				hotelIndex;

@end