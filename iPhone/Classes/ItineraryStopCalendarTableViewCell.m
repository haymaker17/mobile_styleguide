//
//  ItineraryStopCalendarTableViewCell.m
//  ConcurMobile
//
//  Created by Wes Barton on 4/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryStopCalendarTableViewCell.h"

@implementation ItineraryStopCalendarTableViewCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    NSLog(@"ItineraryStopCalendarTableViewCell-reuseIdentifier = %@", reuseIdentifier);

    if (self) {

    }
    return self;
}

- (void)willMoveToSuperview:(UIView *)newSuperview {
    [super willMoveToSuperview:newSuperview];

    //Create the calendar
    self.calendarView = [[CKCalendarView alloc] initWithStartDay:startSunday];
    self.calendarView.allowMultipleSelection = NO;
    self.calendarView.adaptHeightToNumberOfWeeksInMonth = NO;
    self.calendarView.delegate = self;

    [self addSubview:self.calendarView];

    NSLog(@"self.initialDate = %@", self.initialDate);
//    NSLog(@"self.calendarView.bounds.size = ", self.calendarView.bounds.size);

}

- (void)didMoveToSuperview {
    [super didMoveToSuperview];

    NSLog(@"self.initialDate = %@", self.initialDate);
    if(self.initialDate != nil)
    {

        [self.calendarView selectDate:self.initialDate makeVisible:YES];
    }
}


- (void)awakeFromNib
{
    // Initialization code
}



- (BOOL)calendar:(CKCalendarView *)calendar willSelectDate:(NSDate *)date {
    return YES;
}

- (void)calendar:(CKCalendarView *)calendar didSelectDate:(NSDate *)date {
    if (self.onDateSelected) {
        self.onDateSelected(date);
    }
}

- (BOOL)calendar:(CKCalendarView *)calendar willDeselectDate:(NSDate *)date {
    return NO;
}

- (void)calendar:(CKCalendarView *)calendar didDeselectDate:(NSDate *)date {

}

- (BOOL)calendar:(CKCalendarView *)calendar willChangeToMonth:(NSDate *)date {
    return YES;
}

- (void)calendar:(CKCalendarView *)calendar didChangeToMonth:(NSDate *)date {

}


@end
