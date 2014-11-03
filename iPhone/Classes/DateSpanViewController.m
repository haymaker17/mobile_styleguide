//
//  DatePairViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DateSpanViewController.h"
#import "ExSystem.h" 

#import "HotelViewController.h"
#import "BookingLabelValueCell.h"


@implementation DateSpanViewController


@synthesize tBar;
@synthesize cancelBtn,doneBtn;
@synthesize titleButton;
@synthesize datePicker;
@synthesize startDateLabel;
@synthesize endDateLabel;
@synthesize startDateValueLabel;
@synthesize endDateValueLabel;
@synthesize startDateButton;
@synthesize endDateButton;
@synthesize title;
@synthesize startDate;
@synthesize endDate;
@synthesize timeInterval;
@synthesize isStartDateSelected;
@synthesize dateSpanDelegate;
@synthesize tableList;
@synthesize aValues, indexPathSelected;

#define CHECK_IN_ROW 0
#define CHECK_OUT_ROW 1

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return HOTEL_DATES;
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

	NSString* buttonTitle = [Localizer getLocalizedText:@"Hotel Dates"];
	titleButton.title = buttonTitle;
	
	self.tBar.topItem.title = buttonTitle;
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	doneBtn.title = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
	
	self.datePicker.timeZone = [NSTimeZone localTimeZone];
	
	//MOB-3129
	datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0)];
	datePicker.minimumDate = [NSDate date];
	
	startDateLabel.text = [Localizer getLocalizedText:@"Check-in"];
	endDateLabel.text = [Localizer getLocalizedText:@"Check-out"];
	
	startDateValueLabel.text = [DateTimeFormatter formatHotelOrCarDateForBooking:startDate];
	endDateValueLabel.text = [DateTimeFormatter formatHotelOrCarDateForBooking:endDate];

    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    tBar.tintColor = [UIColor darkBlueConcur_iOS6];
    tBar.alpha = 0.9f;
    
    self.aValues = [[NSMutableArray alloc] initWithObjects:[DateTimeFormatter formatHotelOrCarDateForBooking:startDate], [DateTimeFormatter formatHotelOrCarDateForBooking:endDate], nil];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self synchronizeDatePicker];
    
    [tableList selectRowAtIndexPath:indexPathSelected animated:YES scrollPosition:UITableViewScrollPositionTop];
}

- (void)viewDidLayoutSubviews
{
    if ([self respondsToSelector:@selector(topLayoutGuide)])
    {
        CGRect viewBounds = self.view.bounds;
        CGFloat topBarOffset = self.topLayoutGuide.length;
        
        [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height-topBarOffset)];
    }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

#pragma mark -
#pragma mark Button handlers

-(IBAction) startDateButtonPressed:(id)sender
{
	self.isStartDateSelected = YES;
	[self synchronizeDatePicker];

}

-(IBAction) endDateButtonPressed:(id)sender
{
	self.isStartDateSelected = NO;
	[self synchronizeDatePicker];
}


#pragma mark -
#pragma mark Utility Methods

// This is how the parent controller sets the start and end dates
-(void) initStartDate:(NSDate *)startDt endDate:(NSDate*)endDt selectStartDate:(BOOL)selStartDate title:(NSString*)titleText
{
	self.title = titleText;
	self.startDate = startDt;
	self.endDate = endDt;
	self.timeInterval = [endDate timeIntervalSinceDate:startDate];
	self.isStartDateSelected = selStartDate;
}

-(void)synchronizeDatePicker
{
	if (isStartDateSelected)
    {
		[datePicker setDate:startDate animated:YES];
        datePicker.minimumDate = [NSDate date];
    }
	else
    {
		[datePicker setDate:endDate animated:YES];
        datePicker.minimumDate = [startDate dateByAddingTimeInterval:(60.0 * 60.0 * 24.0)]; // startDate +1Day
    }
	
	if (isStartDateSelected)
	{
		[startDateButton setHighlighted:YES];
		[startDateLabel setHighlighted:YES];
		[startDateValueLabel setHighlighted:YES];
		
		[endDateButton setHighlighted:NO];
		[endDateLabel setHighlighted:NO];
		[endDateValueLabel setHighlighted:NO];
	}
	else
	{
		[startDateButton setHighlighted:NO];
		[startDateLabel setHighlighted:NO];
		[startDateValueLabel setHighlighted:NO];
		
		[endDateButton setHighlighted:YES];
		[endDateLabel setHighlighted:YES];
		[endDateValueLabel setHighlighted:YES];
	}
}

-(IBAction) datePickerValueChanged:(id)sender
{
	if (isStartDateSelected)
	{
		self.startDate = datePicker.date;
		if (timeInterval > 0)
		{
			NSDate *newDate = [[NSDate alloc] initWithTimeInterval:timeInterval sinceDate:startDate];
			self.endDate = newDate;
		}
	}
	else
	{
		self.endDate = datePicker.date;
		self.timeInterval = [endDate timeIntervalSinceDate:startDate];
	}
	
    [aValues removeAllObjects];
    [aValues addObject:[DateTimeFormatter formatHotelOrCarDateForBooking:startDate]];
    [aValues addObject:[DateTimeFormatter formatHotelOrCarDateForBooking:endDate]];
    
    [tableList reloadData];
    [tableList selectRowAtIndexPath:indexPathSelected animated:NO scrollPosition:UITableViewScrollPositionNone];
    
//	startDateValueLabel.text = [DateTimeFormatter formatHotelOrCarDateForBooking:startDate];
//	endDateValueLabel.text = [DateTimeFormatter formatHotelOrCarDateForBooking:endDate];
}

-(IBAction) btnDone:(id)sender
{
	[dateSpanDelegate setDateSpanFrom:startDate to:endDate];
	[self closeView];
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
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
    self.tableList = nil;
}



#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [aValues count];
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
    
    SettingsBaseCell *cell = (SettingsBaseCell *)[tableView dequeueReusableCellWithIdentifier: @"LabelCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"LabelCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[SettingsBaseCell class]])
                cell = (SettingsBaseCell *)oneObject;
    }
    
    cell.lblHeading.text = aValues[row];
    if(row == 0)
        cell.lblSubheading.text = [Localizer getLocalizedText:@"Check-in"];
    else
        cell.lblSubheading.text = [Localizer getLocalizedText:@"Check-out"];
    
    if(isStartDateSelected && row == 0)
        self.indexPathSelected = indexPath;
    else if(!isStartDateSelected && row == 1)
        self.indexPathSelected = indexPath;


    return cell;
}



#pragma mark -
#pragma mark Table View Delegate Methods
- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 54;	
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath 
{
	if (indexPath.row == 0)
    {
        self.indexPathSelected = indexPath;
        [self startDateButtonPressed:nil];
    }
    else
    {
        self.indexPathSelected = indexPath;
        [self endDateButtonPressed:nil];
    }
        

}

@end

