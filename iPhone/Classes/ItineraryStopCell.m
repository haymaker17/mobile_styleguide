//
//  ItineraryStopCell.m
//  ConcurMobile
//
//  Created by Wes Barton on 1/22/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ItineraryStopCell.h"
#import "ItineraryStopDetailViewController.h"
#import "CTEBadge.h"
#import "DateTimeConverter.h"

@implementation ItineraryStopCell

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

+ (NSDate *)mergeDateTime:(NSDate *)mergeDate mergeTime:(NSDate *)mergeTime
{
//    NSLog(@"mergeDate = %@", mergeDate);
//    NSLog(@"mergeTime = %@", mergeTime);
    NSCalendar *calendar = [NSCalendar currentCalendar];

    //Need to match this or the dates will get screwed up
    NSTimeZone *gmtTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    calendar.timeZone = gmtTimeZone;

    unsigned timeFlags = NSHourCalendarUnit | NSMinuteCalendarUnit;
    unsigned dateFlags = NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit;

    NSDateComponents *dateComponents = [calendar components:dateFlags fromDate:mergeDate];

    NSDateComponents *timeComponents = [calendar components:timeFlags fromDate:mergeTime];

    timeComponents.year = dateComponents.year;
    timeComponents.month = dateComponents.month;
    timeComponents.day = dateComponents.day;

    NSDate *newDate = [calendar dateFromComponents:timeComponents];

    return newDate;
}

+ (NSDate *)getStartOfBusinessDay:(NSDate *)baseDate
{
    NSDate *xdate = [self quickTime:9 minute:0 second:0];

    NSDate *mergedDate = [ItineraryStopCell mergeDateTime:baseDate mergeTime:xdate];
    
    return mergedDate;
}

+ (NSDate *)getEndOfBusinessDay:(NSDate *)baseDate
{
    NSDate *xdate = [self quickTime:17 minute:0 second:0];

    NSDate *mergedDate = [ItineraryStopCell mergeDateTime:baseDate mergeTime:xdate];

    return mergedDate;
}

+ (NSDate *)quickTime:(int)hour minute:(int)minute second:(int)second
{
    NSCalendar *calendar = [NSCalendar currentCalendar];
    NSTimeZone *gmtTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    calendar.timeZone = gmtTimeZone;

    NSDateComponents *comps = [[NSDateComponents alloc]init];
    [comps setYear:2014];
    [comps setMonth:1];
    [comps setDay:1];
    [comps setHour:hour];
    [comps setMinute:minute];
    [comps setSecond:second];

    NSDate *xdate = [calendar dateFromComponents:comps];
    return xdate;
}

-(void) inputAccessoryViewDidFinishFromTimeText
{
    [self.stopTimeText resignFirstResponder];
    //Reload the row

}

-(void) inputAccessoryViewDidFinishFromDateText
{
    [self.stopDateText resignFirstResponder];
    //Reload the row

}

-(void) timeChangedDeparture:(id)sender
{
    UIDatePicker *picker = (UIDatePicker *)sender;
    if(picker.tag != nil)
    {
        ItineraryStop *stop = self.itineraryStop;

        NSDate *mergedDate = [ItineraryStopCell mergeDateTime:stop.departureDate mergeTime:picker.date];
        stop.departureDate = mergedDate;
        // Refresh the label, without resigning first responder
        [self setCellTimeLabels:mergedDate];
    }
}

-(void) timeChangedArrival:(id)sender
{
    UIDatePicker *picker = (UIDatePicker *)sender;
    if(picker.tag != nil)
    {
        NSDate *mergedDate = [ItineraryStopCell mergeDateTime:self.itineraryStop.arrivalDate mergeTime:picker.date];
        self.itineraryStop.arrivalDate = mergedDate;
        // Refresh the label, without resigning first responder
        [self setCellTimeLabels:mergedDate];
    }
}

-(void) timeChangedBorderCrossing:(id)sender
{
    UIDatePicker *picker = (UIDatePicker *)sender;
    if(picker.tag != nil)
    {
        NSDate *mergedDate = [ItineraryStopCell mergeDateTime:self.itineraryStop.borderCrossDate mergeTime:picker.date];
        self.itineraryStop.borderCrossDate = mergedDate;
        // Refresh the label, without resigning first responder
        [self setCellTimeLabels:mergedDate];
    }
}

- (void)setCellTimeLabels:(NSDate *)date
{
    NSDateFormatter *timeFormatter= [ItineraryStopDetailViewController getItineraryTimeFormatter];
    NSString *dateString = [timeFormatter stringFromDate:date];
    self.stopTime.text = dateString;
    self.stopTimeText.text = dateString;
}

- (void)setCellDateLabels:(NSDate *)date
{
    NSDateFormatter *timeFormatter= [ItineraryStopDetailViewController getItineraryDateFormatter];
    NSString *dateString = [timeFormatter stringFromDate:date];
    self.stopDate.text = dateString;
    self.stopDateText.text = dateString;
}

- (BOOL)calendar:(CKCalendarView *)calendar willSelectDate:(NSDate *)date {
    return YES;
}

- (void)calendar:(CKCalendarView *)calendar didSelectDate:(NSDate *)date {
    if (self.onDateSelected) {
        //date is in the local TZ, need to convert to GMT

        NSDate *gmtDate = [DateTimeConverter gmtDateWithSameComponentsAsLocalDate:date];

        self.onDateSelected(gmtDate);
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
