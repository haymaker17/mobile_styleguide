//
//  CarDateTimeViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CarDateTimeViewController.h"
#import "ExSystem.h" 

#import "CarViewController.h"
#import "CarSearchCriteria.h"
#import "BookingLabelValueCell.h"


@implementation CarDateTimeViewController

@synthesize parentMVC;
@synthesize tBar;
@synthesize titleButton;
@synthesize datePicker;
@synthesize hourPicker;
@synthesize date;
@synthesize isPickup;
@synthesize isEditingDate;
@synthesize cancelBtn;

#define DATE_ROW 0
#define HOUR_ROW 1

#pragma mark -
#pragma mark IBOutlet methods

-(IBAction) datePickerValueChanged:(id)sender
{
	self.date = datePicker.date;
	[self reloadData];
}

-(IBAction) btnDone:(id)sender
{
//	if (isPickup)
//		[parentMVC changePickupDate:date];
//	else
//		[parentMVC changeDropoffDate:date];
//
//	[self closeView];
}

-(IBAction) btnCancel:(id)sender
{
	[self closeView];	
}

-(void)closeView
{
	[self dismissViewControllerAnimated:YES completion:nil];	
}


#pragma mark -
#pragma mark Modes
-(void)enterDateMode
{
	self.isEditingDate = YES;
	[datePicker setHidden:NO];
	[hourPicker setHidden:YES];
}

-(void)enterHourMode
{
	self.isEditingDate = NO;
	[datePicker setHidden:YES];
	[hourPicker setHidden:NO];
}


#pragma mark -
#pragma mark Utilities

-(void)reloadData
{
	// Update table
	[tblView reloadData];
	
	// Update date picker
	[datePicker setDate:date];
	
	// Update hour picker
	NSCalendar *calendar = [NSCalendar currentCalendar];
	calendar.timeZone = [NSTimeZone localTimeZone];
	NSDateComponents *components = [calendar components:(NSHourCalendarUnit) fromDate:date];
	[hourPicker selectRow:components.hour inComponent:0 animated:NO];
}


#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return CAR_DATES;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}


-(void)respondToFoundData:(Msg *)msg
{
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
	self.cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	
	titleButton.title = (isPickup ? [Localizer getLocalizedText:@"Pick-up"] : [Localizer getLocalizedText:@"Drop-off"]);
	
	self.datePicker.timeZone = [NSTimeZone localTimeZone];
	
	self.datePicker.minimumDate = [NSDate date];
	
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

	[self reloadData];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
	[self enterDateMode];

	// Select the date cell
	NSUInteger path[2] = {0, 0};
	NSIndexPath *indexPath = [[NSIndexPath alloc] initWithIndexes:path length:2];
	[tblView selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionNone];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

#pragma mark -
#pragma mark UIPickerViewDelegate methods

-(NSString*)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component
{
	return [CarSearchCriteria hourStringFromInteger:row];
}

-(void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
	NSCalendar *calendar = [NSCalendar currentCalendar];
	calendar.timeZone = [NSTimeZone localTimeZone];
	NSDateComponents *components = [calendar components:(NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit | NSHourCalendarUnit) fromDate:date];
	components.hour = row;
	self.date = [calendar dateFromComponents:components];
	[self reloadData];
}

#pragma mark -
#pragma mark UIPickerViewDataSource methods

-(NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView
{
	return 1;
}

-(NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component
{
	return 24;	// Hours in a day
}

#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 2;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    NSUInteger row = [indexPath row];
    
	NSString *cellText;
	NSString *cellDetailText;
	
	if (row == 0)
	{
		cellText = [Localizer getLocalizedText:@"Date"];
		cellDetailText = [DateTimeFormatter formatHotelOrCarDateForBooking:date];
	}
	else
	{
		NSCalendar *calendar = [NSCalendar currentCalendar];
		calendar.timeZone = [NSTimeZone localTimeZone];
		NSDateComponents *components = [calendar components:(NSHourCalendarUnit) fromDate:date];
		cellText = [Localizer getLocalizedText:@"Time"];
		cellDetailText = [CarSearchCriteria hourStringFromInteger:components.hour];
	}
	
	BookingLabelValueCell* cell =  [BookingLabelValueCell makeCell:tableView owner:self label:cellText value:cellDetailText];
	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
	if (DATE_ROW == row)
		[self enterDateMode];
	else
		[self enterHourMode];
}


#pragma mark -
#pragma mark Memory management

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/

/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}
*/

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}




@end
