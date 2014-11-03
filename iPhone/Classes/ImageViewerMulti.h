//
//  ImageViewerMulti.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileViewController.h"
#import "EntityHotelBooking.h"
#import "EntityHotelImage.h"

@interface ImageViewerMulti : NSObject {
	
	MobileViewController			*__weak parentVC;
	NSMutableArray					*aImageURLs;

}

@property (weak, nonatomic) MobileViewController			*parentVC;
@property (strong, nonatomic) NSMutableArray				*aImageURLs;


-(void) fillImagesFromURLs:(NSMutableArray*) imageURLs Owner:(MobileViewController*)owner ImageViewer:(UIImageView*)ivFirst;
-(IBAction) showHotelImages:(id)sender;
-(NSMutableArray*)getImageURLs:(NSArray *)propertyImagePairs;
-(void)configureWithImagePairs:(NSArray *)propertyImagePairs Owner:(MobileViewController*)owner  ImageViewer:(UIImageView*)ivFirst;
-(void)configureWithImagePairsForHotel:(EntityHotelBooking *)hotelBooking Owner:(MobileViewController*)owner ImageViewer:(UIImageView*)ivFirst;
-(NSMutableArray*)getImageURLsForHotel:(EntityHotelBooking *)hotelBooking;
@end
