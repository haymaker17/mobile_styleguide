//
//  HotelRoomReserveView.h
//  ConcurMobile
//
//  Created by ernest cho on 8/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEHotel.h"
#import "CTEHotelRate.h"
#import "HotelRoomReserveViewController.h"

@interface HotelRoomReserveView : UIView

- (void)setSelectedRate:(CTEHotelRate *)rate nextViewControllerBlock:(void (^)(UIViewController *nextViewController))nextViewControllerBlock updateActiveField:(void (^)(UIView *activeField))updateActiveField;

- (void)reserve;

@end
