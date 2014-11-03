//
//  DateTimePopoverDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/16/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol DateTimePopoverDelegate <NSObject>
- (void)cancelPicker;
- (void)donePicker:(NSDate *)dateSelected;
- (void)pickedDate:(NSDate *)dateSelected;
@end

