//
//  CarMileageDataLoader.h
//  ConcurMobile
//
//  Created by ernest cho on 3/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RootViewController.h"

@interface CarMileageDataLoader : NSObject<ExMsgRespondDelegate>

@property (strong, nonatomic) CarRatesData *carRatesData;

- (BOOL)isCarMileageDataReady;
- (void)openReportSelectView:(UIViewController *)parentView;

@end
