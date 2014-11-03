//
//  MobileTourData.h
//  ConcurMobile
//
//  Created by Sally Yan on 2/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

// this are the keys used in the data source
extern NSString *const kTourImage;
extern NSString *const kTourTitle ;
extern NSString *const kTourSubtitle;

@interface MobileTourData : NSObject

@property (nonatomic,strong) NSArray *introScreens_iPhone;
@property (nonatomic,strong) NSArray *introScreens_iPad;

@end
