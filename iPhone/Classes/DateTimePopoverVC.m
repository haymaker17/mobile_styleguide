//
//  DateTimePopoverVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 10/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DateTimePopoverVC.h"
#import "DateTimeFormatter.h"
#import "ExSystem.h" 


@implementation DateTimePopoverVC
@synthesize		datePicker;
@synthesize		aTimes, selectedExtendedHour, currTime;
@synthesize		dateSelected;
@synthesize		key, isDate, indexPath, initialDate, initialExtendedHour;
@synthesize delegate = _delegate;


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	
    self.datePicker.timeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
    
	datePicker.date = (initialDate != nil ? initialDate : now);
	datePicker.autoresizingMask = UIViewAutoresizingNone;
    if (self.isDate)
    {
        self.datePicker.timeZone = [NSTimeZone localTimeZone];
        datePicker.date = (initialDate != nil ? initialDate : [NSDate date]);

        datePicker.datePickerMode = UIDatePickerModeDate;
    }
    else
    {
        datePicker.datePickerMode = UIDatePickerModeDateAndTime;
        datePicker.minuteInterval = 30;
    }
	self.contentSizeForViewInPopover = CGSizeMake(320.0, 215.0);

}



- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    
    self.datePicker = nil;
}



-(IBAction) datePickerValueChanged:(id)sender
{
	// AJC - if this code is still here after 12/12/2013, please delete
    //lblDate.text = [self textFromDate:datePicker.date];
    if (_delegate != nil && [_delegate respondsToSelector:@selector(pickedDate:)])
        [_delegate pickedDate:datePicker.date];
}

-(IBAction) closeDown:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

-(IBAction) closeDone:(id)sender
{
	[self onDone];
	[self dismissViewControllerAnimated:YES completion:nil];
}

-(void) onDone
{
	// overriden
}

-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd
{
	// set up date components
	NSDateComponents *components = [[NSDateComponents alloc] init];
	[components setDay:daysToAdd];
	
	// create a calendar
	NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
	
	NSDate *newDate2 = [gregorian dateByAddingComponents:components toDate:dateDepart options:0];
	
	return newDate2;
}

-(NSString*) textFromDate:(NSDate*)date
{
	return [DateTimeFormatter formatDateForBooking:date];
}

-(NSDate*) dateFromText:(NSString*)text
{
	return [DateTimeFormatter getNSDate:text Format:@"EEE MMM dd, yyyy"];
}

#pragma mark -
#pragma mark Popover methods
-(void) initDate:(NSDate *) dateCurrent
{
	[datePicker setHidden:NO];
	
	if (dateCurrent == nil)
		dateCurrent = [NSDate date];
	datePicker.date = dateCurrent;
}


-(IBAction)dateDone:(id)sender
{
    if (_delegate != nil && [_delegate respondsToSelector:@selector(donePicker:)])
        [_delegate donePicker:datePicker.date];
}


-(IBAction)dateCancel:(id)sender
{
    if (_delegate != nil && [_delegate respondsToSelector:@selector(cancelPicker)])    
        [_delegate cancelPicker];
}
@end
