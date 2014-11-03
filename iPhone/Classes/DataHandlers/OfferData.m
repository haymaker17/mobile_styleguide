////
////  OfferData.m
////  ConcurMobile
////
////  Created by Paul Kramer on 8/25/11.
////  Copyright (c) 2011 Concur. All rights reserved.
////
//
//#import "OfferData.h"
//#import "ImageUtil.h"
//
//@implementation OfferData
//@synthesize Id;
//@synthesize bookingSource;
//@synthesize recordLocator;
//@synthesize segmentKey;
//@synthesize title;
//@synthesize offerVendor;
//@synthesize offerAction;
//@synthesize offerApplication;
//@synthesize links;
//@synthesize imageName;
//@synthesize actionURL;
//@synthesize imageURL;
//@synthesize htmlContent;
//@synthesize segmentSide;
//@synthesize validityDetails;
//@synthesize geoLink;
//
//-(void) processImageDataWithBlock:(void (^)(NSData *imageData))processImage
//{
//    NSString *url = nil;
//    
//    if (self.imageURL != nil) {
//        url = self.imageURL;
//    }
//    else
//    {
//        NSString * name = [NSString stringWithFormat:@"%@.png",self.imageName];
//        
//        if ([ExSystem is4]) {
//            name = [NSString stringWithFormat:@"%@@2x.png",self.imageName];
//        }
//        url = [NSString stringWithFormat:@"%@/images/mobile/intouch/iOS/%@", [ExSystem sharedInstance].entitySettings.uriNonSSL,name];
//    }
//
//	dispatch_queue_t callerQueue = dispatch_get_current_queue();
//	dispatch_queue_t downloadQueue = dispatch_queue_create("Offer icon downloader", NULL);
//	dispatch_async(downloadQueue, ^{
//		NSData *imageData = [ImageUtil imageDataForImageWithURLString:url];
//		dispatch_async(callerQueue, ^{
//		    processImage(imageData);
//		});
//	});
//	dispatch_release(downloadQueue);
//}
//
//-(void) printValidTimeRange
//{
//    NSArray *timeRanges = self.validityDetails.offerTimeRanges;
//    NSLog(@"\nOffer valid between these dates:\n");
//    
//    for (OfferTimeRange *validTimeRange in timeRanges) 
//    {
//        NSDate *startDate = [DateTimeFormatter getNSDate:validTimeRange.startDateTimeUTC Format:@"yyyy-MM-dd'T'HH:mm:ss"];
//        NSDate *endDate = [DateTimeFormatter getNSDate:validTimeRange.endDateTimeUTC Format:@"yyyy-MM-dd'T'HH:mm:ss"];
//
//        NSLog(@"\nTime range:\n-->Offer valid from:%@\n-->Offer expires at:%@\n",startDate,endDate);
//    }
//}
//
//-(void) printValidLocationDetails
//{
//    NSArray *locations= self.validityDetails.offerLocations;
//    NSLog(@"\nOffer valid for these proximate locations:\n");
//    for (OfferLocation *validLocation in locations)
//    {
//        NSLog(@"\nLocation:\n-->Latitude:%f\n-->Longitude:%f\n-->Proximity in km:%f\n",[validLocation.latitude doubleValue],[validLocation.longitude doubleValue],[validLocation.proximity doubleValue]);
//    } 
//}
//
//-(void) printDetails
//{
//    NSLog(@"\nOffer Id:%@\nOffer Vendor:%@\nOffer type:%@\nDisplayed in segment side:%@\n",self.Id, self.offerVendor,self.offerAction,self.segmentSide);
//    [self printValidTimeRange];
//    [self printValidLocationDetails];
//    NSLog(@"\n===========================================\n");
//}
//
//-(BOOL) hasValidProximity
//{
//    NSArray *locations= self.validityDetails.offerLocations;
//    
//    if (locations != nil && [locations count] > 0) 
//    {
//        for (OfferLocation *validLocation in locations)
//        {
//            CLLocation *location = [[CLLocation alloc] initWithLatitude:[validLocation.latitude doubleValue] longitude:[validLocation.longitude doubleValue]];
//            double distance = [location distanceFromLocation:[GlobalLocationManager currentLocation]];
//            if(distance/1000 <= [validLocation.proximity doubleValue])
//            {
//                return TRUE;
//            }
//        }
//        return FALSE; 
//    }
//    else
//    {
//        //If there are no location constraints then offer should not be invalidated
//        return TRUE;
//    }
//}
//
//-(BOOL) hasValidTimeRange
//{
//    NSDate *now = [NSDate date];
//    NSArray *timeRanges = self.validityDetails.offerTimeRanges;
//    
//    if (timeRanges != nil && [timeRanges count] > 0) 
//    {
//        for (OfferTimeRange *validTimeRange in timeRanges) 
//        {
//            NSDate *startDate = [DateTimeFormatter getNSDate:validTimeRange.startDateTimeUTC Format:@"yyyy-MM-dd'T'HH:mm:ss"];
//            NSDate *endDate = [DateTimeFormatter getNSDate:validTimeRange.endDateTimeUTC Format:@"yyyy-MM-dd'T'HH:mm:ss"];
//            
//            if ([startDate timeIntervalSinceDate:now] <= 0 && [endDate timeIntervalSinceDate:now] > 0) {
//                return YES;
//            }
//        }
//        
//        return FALSE;    
//    }
//    else
//    {
//        //If there are no time constraints then offer should not be invalidated
//        return TRUE;
//    }
//}
//
//@end
