//
//  OfferData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/25/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

//#import <Foundation/Foundation.h>
//#import "SegmentData.h"
//#import "OfferValidity.h"
//#import "MultiWebLinkData.h"
//#import "OfferLocation.h"
//#import "DateTimeFormatter.h"
//
//@interface OfferData : SegmentData
//{
//    NSString *Id;
//    NSString *bookingSource;
//    NSString *recordLocator;
//    NSString *segmentKey;
//    NSString *segmentSide;
//    NSString *title;
//    NSString *offerVendor;
//    NSString *offerAction;
//    
//    // APP_LINK offer type
//    NSString *offerApplication;
//    
//    // WEB_LINK offer type
//    NSString *actionURL;
//    NSString *imageURL;
//    
//    // MULTI_LINK offer type
//    NSMutableArray *links;
//    NSString *imageName;
//        
//    // NULL_LINK offer type
//    OfferLocation *geoLink;
//    
//    NSString *htmlContent;
//    OfferValidity *validityDetails;
//}
//
//@property (strong, nonatomic) NSString *Id;
//@property (strong, nonatomic) NSString *bookingSource;
//@property (strong, nonatomic) NSString *recordLocator;
//@property (strong, nonatomic) NSString *segmentKey;
//@property (strong, nonatomic) NSString *title;
//@property (strong, nonatomic) NSString *offerVendor;
//@property (strong, nonatomic) NSString *offerAction;
//@property (strong, nonatomic) NSString *offerApplication;
//@property (strong, nonatomic) NSMutableArray *links;
//@property (strong, nonatomic) NSString *imageName;
//@property (strong, nonatomic) NSString *actionURL;
//@property (strong, nonatomic) NSString *imageURL;
//@property (strong, nonatomic) NSString *htmlContent;
//@property (strong, nonatomic) NSString *segmentSide;
//@property (strong, nonatomic) OfferValidity *validityDetails;
//@property (strong, nonatomic) OfferLocation *geoLink;
//
//-(void) processImageDataWithBlock:(void (^)(NSData *imageData))processImage;
//-(BOOL) hasValidProximity;
//-(BOOL) hasValidTimeRange;
//-(void) printDetails;
//@end
