//
//  ItineraryStopCalendarTableViewCell.h
//  ConcurMobile
//
//  Created by Wes Barton on 4/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CKCalendarView.h"

@class CKCalendarView;

@interface ItineraryStopCalendarTableViewCell : UITableViewCell<CKCalendarDelegate>

@property (strong,nonatomic) CKCalendarView *calendarView;

@property (weak, nonatomic) NSDate *initialDate;

@property (copy,nonatomic) void(^onDateSelected)(NSDate *date);


@end
