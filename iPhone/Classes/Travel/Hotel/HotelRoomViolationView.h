//
//  HotelRoomViolationView.h
//  ConcurMobile
//
//  Created by ernest cho on 10/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CTEHotelViolation.h"
#import "CTEHotelViolationReason.h"

@interface HotelRoomViolationView : UIView <UITextViewDelegate>

- (void)setHotelViolation:(CTEHotelViolation *)violation nextViewControllerBlock:(void (^)(UIViewController *nextViewController))nextViewControllerBlock updateActiveField:(void (^)(UIView *activeField))updateActiveField;

- (CTEHotelViolationReason *)violationReason;

@end
