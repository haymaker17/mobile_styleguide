//
//  DateTimeOneVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "DateTimeOneVC.h"
#import "DateTimeFormatter.h"

@implementation DateTimeOneVC
@synthesize delegate = _delegate;

@synthesize datePicker;
@synthesize lblDate, btnBack, date, timeInMinutes, lblLabel, context, label, viewTitle;

// Travel time is always local to the selected location.
// However, the traveller's local timezone can change, therefore, we need to 
// use date of a fixed time zone to represent departure/arrival time
// The date passed in is in GMT and out is also GMT
-(void)setSeedData:(id<DateTimeOneDelegate>)del withFullDate:(NSDate*) dt withLabel:(NSString*) lbl withContext:(NSObject*) con;
{
    self.delegate = del;
    self.context = con;
    self.date = [DateTimeFormatter getDateWithoutTime:dt withTimeZoneAbbrev:@"GMT"];
    self.timeInMinutes = [DateTimeFormatter getTimeInSeconds:dt withTimeZoneAbbrev:@"GMT"]/60;
    self.label = lbl;
    self.viewTitle = lbl;
}


#pragma mark - Picker rotating
// Use frame of containing view to work out the correct origin and size
// of the UIDatePicker.
-(void) layoutPicker:(UIInterfaceOrientation)orientation {
	
	if (UIInterfaceOrientationIsPortrait(orientation)) 
	{
        datePicker.frame = CGRectMake(0, self.view.frame.size.height - 216, 320, 216);
	}
    else {
		// datePicker.frame = CGRectMake(0, self.view.frame.size.height-162, 480, 162);
		datePicker.frame = CGRectMake(0, 140, 480, 162);
    }
	datePicker.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
	datePicker.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
	
}

+(NSString*) getDateString:(NSDate*) date withTimeInMinutes:(NSInteger) tmInMinutes
{
    NSDate* fullDate = [date dateByAddingTimeInterval:tmInMinutes * 60];
    return [DateTimeFormatter formatBookingDateTime:fullDate];
}

-(void) updateDateLabel
{
    NSTimeZone* gmtTz = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
    NSString *formatString = [NSDateFormatter dateFormatFromTemplate:@"EEE. MMM dd, h:mm aa"  options:0 locale:[NSLocale currentLocale]];
    
    lblDate.text = [DateTimeFormatter formatDate:self.datePicker.date Format:formatString TimeZone:gmtTz];
}

-(void) updateDateTime
{
    NSDate* dawnDate = [DateTimeFormatter getDateWithoutTime:self.datePicker.date withTimeZoneAbbrev:@"GMT"];    
    self.date = dawnDate;
    self.timeInMinutes = [self.datePicker.date timeIntervalSinceDate:dawnDate]/60;
    
}
-(void) setPickerDateTime
{
    NSDate* dawnDate = [DateTimeFormatter getDateWithoutTime:self.date withTimeZoneAbbrev:@"GMT"];
    NSDate* fullDate = [NSDate dateWithTimeInterval:self.timeInMinutes*60 sinceDate:dawnDate];
    
    self.datePicker.date = fullDate;
    [self updateDateLabel];

}

-(void) viewWillAppear:(BOOL)animated 
{
	[super viewWillAppear:animated];
	// Layout once here to ensure the current orientation is respected.
	[self layoutPicker:[UIApplication sharedApplication].statusBarOrientation];
//    [self setPickerDateTime];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


- (void)willAnimateRotationToInterfaceOrientation:
(UIInterfaceOrientation)orientation	 duration:(NSTimeInterval)duration 
{
	[self layoutPicker:orientation];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	self.datePicker.timeZone = [NSTimeZone timeZoneWithAbbreviation:@"GMT"];
	// limit the range to one year forward
    self.datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0 * 1)];
    
    NSDate* now = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
	self.datePicker.minimumDate = now;
    
    lblLabel.text = label;
    self.title = viewTitle;
	if (self.date == nil)
        self.date = now;
    
    [self setPickerDateTime];
    //    NSLog(@"Picker date %@", datePicker.date);
	datePicker.autoresizingMask = UIViewAutoresizingNone;
	
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor colorWithRed:0.882871 green:0.887548 blue:0.892861 alpha:1]];
    
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

-(IBAction) datePickerValueChanged:(id)sender
{
    [self updateDateTime];
    [self updateDateLabel];
}

-(IBAction) closeDone:(id)sender
{
	if (self.delegate!= nil)
    {
        NSDate* fullDate = [self.date dateByAddingTimeInterval:self.timeInMinutes*60];
		[self.delegate dateSelected:context withDate:fullDate];
    }
    self.delegate = nil;
}

-(void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self closeDone:nil];
}


@end
