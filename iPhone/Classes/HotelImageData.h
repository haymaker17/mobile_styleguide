//
//  HotelImageData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HotelImageData : NSObject {

	NSString	*hotelImage, *hotelThumbnail;
}

@property (strong, nonatomic) NSString *hotelImage;
@property (strong, nonatomic) NSString *hotelThumbnail;

@end
