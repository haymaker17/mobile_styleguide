//
//  DateEditVC.m
//  ConcurMobile
//
//  Created by yiwen on 5/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "DateEditVC.h"
#import "DateTimeFormatter.h"

@implementation DateEditVC

@synthesize delegate = _delegate;

@synthesize datePicker;
@synthesize lblDate, btnBack, date, lblTip, context, tipText, viewTitle;


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

-(void) viewWillAppear:(BOOL)animated 
{
	[super viewWillAppear:animated];
	// Layout once here to ensure the current orientation is respected.
	[self layoutPicker:[UIApplication sharedApplication].statusBarOrientation];
    // MOB-6312 Need to set the date on picker twice to get it right on device.
	[datePicker setDate:date animated:YES];
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
	// MOB-4178 Per Justin's suggestion, we display date ranges from 3 years back and 2 years forward
	self.datePicker.timeZone = [NSTimeZone localTimeZone];
	self.datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0 * 2)];
	self.datePicker.minimumDate = [NSDate dateWithTimeIntervalSinceNow:-(60.0 * 60.0 * 24.0 * 365.0 * 3)];

    lblTip.text = tipText;
    self.title = viewTitle;
	if (self.date == nil)
        self.date = [NSDate date];
    lblDate.text = [DateTimeFormatter formatExpenseDateEEEMMMDDYYYY:self.date];
    	
	datePicker.date = date;
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
	self.date = datePicker.date;
	lblDate.text = [DateTimeFormatter formatExpenseDateEEEMMMDDYYYY:self.date];
}

-(void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self closeDone:nil];
}

-(IBAction) closeDone:(id)sender
{
	if (self.delegate!= nil)
		[self.delegate dateSelected:context withValue:self.date];
    
    self.delegate = nil;
}

@end
