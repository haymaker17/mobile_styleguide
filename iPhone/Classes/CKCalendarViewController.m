//
//  CKCalendarViewController.m
//  Jarvis
//
//  Created by Wanny Morellato on 9/4/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

#import "CKCalendarViewController.h"

@interface CKCalendarViewController ()

@end

@implementation CKCalendarViewController

- (id)init
{
    self = [super init];
    if (self) {
        self.calendarView = [[CKCalendarView alloc] initWithStartDay:startSunday];
        self.calendarView.delegate = self;
        self.view = self.calendarView;
        [self setPreferredContentSize:self.calendarView.bounds.size];
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    self.calendarView = [[CKCalendarView alloc] initWithStartDay:startSunday];
    self.calendarView.allowMultipleSelection = NO;
    self.calendarView.adaptHeightToNumberOfWeeksInMonth = NO;
    self.calendarView.delegate = self;
    self.view = self.calendarView;
    [self setPreferredContentSize:self.calendarView.bounds.size];

    if(self.initialDate != nil)
    {
        [self.calendarView selectDate:self.initialDate makeVisible:YES];
    }
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)calendar:(CKCalendarView *)calendar configureDateItem:(CKDateItem *)dateItem forDate:(NSDate *)date{
    
}
- (BOOL)calendar:(CKCalendarView *)calendar willSelectDate:(NSDate *)date{
/*
    if ([date compare:[NSDate date]] == NSOrderedAscending) {
        return NO;
    }
*/
    return YES;
}
- (void)calendar:(CKCalendarView *)calendar didSelectDate:(NSDate *)date{
    if (self.onDateSelected) {
        self.onDateSelected(date);
    }
}
- (BOOL)calendar:(CKCalendarView *)calendar willDeselectDate:(NSDate *)date{
    return NO;
}
- (void)calendar:(CKCalendarView *)calendar didDeselectDate:(NSDate *)date{
    
}

- (BOOL)calendar:(CKCalendarView *)calendar willChangeToMonth:(NSDate *)date{
    return YES;
}
- (void)calendar:(CKCalendarView *)calendar didChangeToMonth:(NSDate *)date{
    
}

- (void)calendar:(CKCalendarView *)calendar didLayoutInRect:(CGRect)frame{
    
}

@end
