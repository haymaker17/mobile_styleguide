//
//  HotelRoomReserveViewController.h
//  ConcurMobile
//
//  Created by ernest cho on 8/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEHotelRate.h"
#import "HotelSearchCriteriaV2.h"

@interface HotelRoomReserveViewController : UIViewController

- (id)initWithSelectedRate:(CTEHotelRate *)selectedRate searchCriteria:(HotelSearchCriteriaV2 *)searchCriteria;

@end
