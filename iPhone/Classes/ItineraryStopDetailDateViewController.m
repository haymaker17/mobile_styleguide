//
//  ItineraryStopDetailDateViewController.m
//  ConcurMobile
//
//  Created by Wes Barton on 3/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryStopDetailDateViewController.h"
#import "CKCalendarViewController.h"

@interface ItineraryStopDetailDateViewController ()

@end

@implementation ItineraryStopDetailDateViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.

    // Did the date get set?  Update the property
    NSLog(@"self.workingDate = %@", self.workingDate);


//    NSTimeZone *tz = [NSTimeZone systemTimeZone];
    NSTimeZone *tz = [NSTimeZone timeZoneForSecondsFromGMT:0];

//    [self.workingDatePicker setTimeZone:tz];
    [self.workingTimePicker setTimeZone:tz];

//    NSLog(@"self.workingDatePicker.timeZone = %@", self.workingDatePicker.timeZone);

//    [self.workingDatePicker setDate:self.workingDate animated:YES];
    [self.workingTimePicker setDate:self.workingDate animated:YES];

//    NSLog(@"self.workingDatePicker.date  = %@", self.workingDatePicker.date);


}

- (void)viewWillDisappear:(BOOL)animated {
    // Need to do this because the default back button cant be attached to exit from IB
    // You connect the viewcontroller to the exit to create the segue, then call it from code.
    [self performSegueWithIdentifier:@"UnwindFromCalendarPicker" sender:self];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



- (IBAction)unwindFromCalendarPicker:(UIStoryboardSegue *)segue
{
    NSLog(@"unwindFromCalendarPicker-segue.identifier = %@", segue.identifier);
    CKCalendarViewController *c = (CKCalendarViewController *)segue.sourceViewController;
    NSMutableArray *dates = c.calendarView.selectedDates;
    for (NSDate *date in dates)
    {
        NSLog(@"date = %@", date);
    }
    
}

- (IBAction)timePickerChangedAction:(id)sender
{
    NSLog(@"time picker changed sender = %@", sender);
    UIDatePicker *targetedDatePicker = sender;
    NSDate *time = targetedDatePicker.date;
    NSLog(@"time = %@", time);

    NSCalendar *calendar = [NSCalendar currentCalendar];

    unsigned timeFlags = NSHourCalendarUnit | NSMinuteCalendarUnit;
    unsigned dateFlags = NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit;

    NSDateComponents *dateComponents = [calendar components:dateFlags fromDate:self.workingDate];

    NSDateComponents *timeComponents = [calendar components:timeFlags fromDate:time];

    dateComponents.hour = timeComponents.hour;
    dateComponents.minute = timeComponents.minute;
    dateComponents.second = 0;

    NSDate *newDate = [calendar dateFromComponents:dateComponents];
    NSLog(@"newDate = %@", newDate);

    self.workingDate = newDate;

}

- (IBAction)datePickerChangedAction:(id)sender
{
    NSLog(@"date picker changed sender = %@", sender);
    UIDatePicker *targetedDatePicker = sender;
    NSDate *date = targetedDatePicker.date;
    NSLog(@"date = %@", date);

    NSCalendar *calendar = [NSCalendar currentCalendar];

    unsigned timeFlags = NSHourCalendarUnit | NSMinuteCalendarUnit;
    unsigned dateFlags = NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit;

    NSDateComponents *dateComponents = [calendar components:dateFlags fromDate:date];

    NSDateComponents *timeComponents = [calendar components:timeFlags fromDate:self.workingDate];

    timeComponents.year = dateComponents.year;
    timeComponents.month = dateComponents.month;
    timeComponents.day = dateComponents.day;

    NSDate *newDate = [calendar dateFromComponents:timeComponents];
    NSLog(@"newDate = %@", newDate);

    self.workingDate = newDate;

}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if([segue.identifier isEqualToString:@"CalendarPicker"])
    {
        NSLog(@"segue.identifier = %@", segue.identifier);

        void (^onDateSelected)(NSDate *)= [self getDateChangedFunction];

        CKCalendarViewController *c = (CKCalendarViewController *)segue.destinationViewController;
//        [c.calendarView selectDate:<#(NSDate *)date#> makeVisible:<#(BOOL)visible#>];
        c.onDateSelected = onDateSelected;

        NSLog(@"self.workingDate = %@", self.workingDate);
        c.initialDate = self.workingDate;

    }

}

- (void (^)(NSDate *))getDateChangedFunction {
    void (^onDateSelected)(NSDate *) = ^(NSDate *selectedDate)
        {
            NSLog(@"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            NSLog(@"selectedDate = %@", selectedDate);
            NSLog(@"self.workingDate = %@", self.workingDate);

            NSCalendar *calendar = [NSCalendar currentCalendar];

            unsigned timeFlags = NSHourCalendarUnit | NSMinuteCalendarUnit;
            unsigned dateFlags = NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit;

            NSDateComponents *dateComponents = [calendar components:dateFlags fromDate:selectedDate];

            NSDateComponents *timeComponents = [calendar components:timeFlags fromDate:self.workingDate];

            timeComponents.year = dateComponents.year;
            timeComponents.month = dateComponents.month;
            timeComponents.day = dateComponents.day;

            NSDate *newDate = [calendar dateFromComponents:timeComponents];
            NSLog(@"newDate = %@", newDate);

            self.workingDate = newDate;
        };
    return onDateSelected;
}


@end
