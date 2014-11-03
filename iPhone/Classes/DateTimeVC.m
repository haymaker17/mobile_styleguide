//
//  DateTimeVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DateTimeVC.h"
#import "DateTimeFormatter.h"
#import "TrainBookVC.h"
#import "ExtendedHour.h"

@implementation DateTimeVC
@synthesize datePicker, dateDiff;
@synthesize lblDate, lblTime, initialDate, initialExtendedHour, isReturn, bbiTitle, scroller, pickerView, aTimes, selectedExtendedHour, isTime, btnBack1, btnBack2, lblDepartureTime, lblDepartureDate;
@synthesize	tBar, cancelBtn, doneBtn;
@synthesize tableList;
@synthesize aValues, indexPathSelected, isDateSelected;
/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/


// Use frame of containing view to work out the correct origin and size
// of the UIDatePicker.
-(void) layoutPicker:(UIInterfaceOrientation)orientation {
	
	if (UIInterfaceOrientationIsPortrait(orientation)) 
	{
        datePicker.frame = CGRectMake(0, self.view.frame.size.height - 216, 320, 216);
		pickerView.frame = datePicker.frame;
	}
    else {
       // datePicker.frame = CGRectMake(0, self.view.frame.size.height-162, 480, 162);
		datePicker.frame = CGRectMake(0, 140, 480, 162);
		pickerView.frame = datePicker.frame;
    }
	datePicker.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
	datePicker.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
	
	//pickerView.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
	//pickerView.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;

}

-(void) viewWillAppear:(BOOL)animated {
	[super viewWillAppear:animated];
	// Layout once here to ensure the current orientation is respected.
	[self layoutPicker:[UIApplication sharedApplication].statusBarOrientation];
    
    [tableList selectRowAtIndexPath:indexPathSelected animated:YES scrollPosition:UITableViewScrollPositionTop];
}

- (void)willAnimateRotationToInterfaceOrientation:
(UIInterfaceOrientation)orientation
										 duration:(NSTimeInterval)duration {
	[self layoutPicker:orientation];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void)setDateTimeViews
{
	if (isTime)
	{
		[datePicker setHidden:YES];
		[pickerView setHidden:NO];
		
		[btnBack1 setHighlighted:NO];
		[lblDate setHighlighted:NO];
		[lblDepartureDate setHighlighted:NO];
		
		[btnBack2 setHighlighted:YES];
		[lblTime setHighlighted:YES];
		[lblDepartureTime setHighlighted:YES];
        
        NSUInteger _path[2] = {0, 1};
        NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
        self.indexPathSelected = _indexPath;
        [tableList selectRowAtIndexPath:indexPathSelected animated:NO scrollPosition:UITableViewScrollPositionTop];
//        NSArray *_indexPaths = [[NSArray alloc] initWithObjects:_indexPath, nil];
//        [tableList reloadRowsAtIndexPaths:_indexPaths withRowAnimation:NO];
//        [_indexPaths release];
        self.isDateSelected = NO;

	}
	else 
    {
        self.isDateSelected = YES;
		[datePicker setHidden:NO];
		[pickerView setHidden:YES];
		[btnBack1 setHighlighted:YES];
		[lblDate setHighlighted:YES];
		[lblDepartureDate setHighlighted:YES];
		
		
		[btnBack2 setHighlighted:NO];
		[lblTime setHighlighted:NO];
		[lblDepartureTime setHighlighted:NO];
		
		[btnBack1 setHighlighted:YES];
        
        NSUInteger _path[2] = {0, 0};
        NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
        self.indexPathSelected = _indexPath;
        [tableList selectRowAtIndexPath:indexPathSelected animated:NO scrollPosition:UITableViewScrollPositionTop];
	}
}

#pragma mark - View Methods
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	
	//datePicker.frame = CGRectMake(0, datePicker.frame.origin.y + 44, datePicker.frame.size.width, datePicker.frame.size.height);
	[self initTimeList];
	
    [super viewDidLoad];
	
	self.tBar.topItem.title = [Localizer getLocalizedText:@"Date"];
	cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	doneBtn.title = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
    
    if(self.isTime)
        self.isDateSelected = NO;
    else
        self.isDateSelected = YES;
	
//	receiptView.frame = CGRectMake(0, 0, screenW, h);
//	receiptView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//	scrollView.frame = CGRectMake(0, 0, screenW, screenH);
//	scrollView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
//	receiptView.image = myimage;
	float w = 320.0;
	if([ExSystem isLandscape])
		w = 480;
	scroller.contentSize = CGSizeMake(w, 192);
//	scrollView.maximumZoomScale = 8.0;
//	scrollView.minimumZoomScale = 0.75;
	scroller.clipsToBounds = YES;
	scroller.delegate = self;
	
	bbiTitle.title = [self getTitleLabel];
	lblDepartureDate.text = [self getDepartureDateLabel];
	lblDepartureTime.text = [self getDepartureTimeLabel];
	
    lblDate.text = [self textFromDate:initialDate];
	lblTime.text = [ExtendedHour getLocalizedTextForExtendedHour:initialExtendedHour];
	if(initialExtendedHour < 0)
		self.selectedExtendedHour = 8;
	else 
		self.selectedExtendedHour = initialExtendedHour;
	
	// MOB - 2383
	//datePicker.timeZone = [NSTimeZone localTimeZone];
	//NSLog(@"initialDate = %@", initialDate);
	//NSLog(@"Now date = %@", [NSDate date]);
	//datePicker.timeZone = [NSTimeZone localTimeZone];
	datePicker.date = (initialDate != nil ? initialDate : [NSDate date]);
	datePicker.autoresizingMask = UIViewAutoresizingNone;
	

	//MOB-3129
	datePicker.maximumDate = [NSDate dateWithTimeIntervalSinceNow:(60.0 * 60.0 * 24.0 * 365.0)];
	datePicker.minimumDate = [NSDate date];
	
    self.tBar.tintColor = [UIColor darkBlueConcur_iOS6];
    self.tBar.alpha = 0.9f;
	
	[self setSelectedTime];
    
    self.aValues = [[NSMutableArray alloc] initWithObjects:[self textFromDate:initialDate], [ExtendedHour getLocalizedTextForExtendedHour:initialExtendedHour], nil];
    
    [self setDateTimeViews];
}

-(NSString*)getTitleLabel
{
	return @"";
}

-(NSString*)getDepartureDateLabel
{
	return @"";
}

-(NSString*)getDepartureTimeLabel
{
	return @"";
}


/*
-(void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    // Layout once here to ensure the current orientation is respected.
    [self layoutPicker:[UIApplication sharedApplication].statusBarOrientation];
}
 */

/*
-(BOOL) shouldAutorotateToInterfaceOrientation:
(UIInterfaceOrientation)orientation {
    return (orientation == UIInterfaceOrientationPortrait ||
            UIInterfaceOrientationIsLandscape(orientation));
}
 */

// Use frame of containing view to work out the correct origin and size
// of the UIDatePicker.
/*
-(void) layoutPicker:(UIInterfaceOrientation)orientation {
    CGFloat newPickerHeight = UIInterfaceOrientationIsLandscape(orientation) 
    ? 170 : 216;
    datePicker.frame = CGRectMake(0,
                                  self.view.frame.size.height - newPickerHeight, 
                                  self.view.frame.size.width, newPickerHeight);
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



-(IBAction) datePickerValueChanged:(id)sender
{
	lblDate.text = [self textFromDate:datePicker.date];
    [aValues removeAllObjects];
    [aValues addObject:[self textFromDate:datePicker.date]];
    [aValues addObject:[ExtendedHour getLocalizedTextForExtendedHour:selectedExtendedHour]];
    
    
    if(isDateSelected)
    {
        [tableList reloadData];
        [tableList selectRowAtIndexPath:indexPathSelected animated:NO scrollPosition:UITableViewScrollPositionNone];
    }
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
	//NSDate *now = [NSDate date];
	//int daysToAdd = 50;  // or 60 :-)
	
	// set up date components
	NSDateComponents *components = [[NSDateComponents alloc] init];
	[components setDay:daysToAdd];
	
	// create a calendar
	NSCalendar *gregorian = [[NSCalendar alloc] initWithCalendarIdentifier:NSGregorianCalendar];
	
	NSDate *newDate2 = [gregorian dateByAddingComponents:components toDate:dateDepart options:0];
	//NSLog(@"Clean: %@", newDate2);
	
	return newDate2;
}

-(NSString*) textFromDate:(NSDate*)date
{
	return [DateTimeFormatter formatHotelOrCarDateForBooking:date];
}

-(NSDate*) dateFromText:(NSString*)text
{
	return [DateTimeFormatter getNSDate:text Format:@"EEE MMM dd, yyyy"];
}

#pragma mark -
#pragma mark Picker view methods
-(void) initTimeList
{
//	aTimes = [[NSMutableArray alloc] initWithObjects:
//			  [NSNumber numberWithInt:MorningExtendedHour]
//			  , [NSNumber numberWithInt:AfternoonExtendedHour]
//			  , [NSNumber numberWithInt:EveningExtendedHour]
//			  , [NSNumber numberWithInt:MidnightExtendedHour]
//			  , nil];
    aTimes = [[NSMutableArray alloc] initWithObjects: nil];
	//[NSNumber numberWithInt:AnytimeExtendedHour]
	
	
	for (int i = 0; i < 24; i++)
		[aTimes addObject:@(i)];
}

- (UIView *)pickerView:(UIPickerView*)pickerView viewForRow:(NSInteger)row
		  forComponent:(NSInteger)component reusingView:(UIView *)view
{
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 200, 44)];
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(50, 0, 160, 44)];
	[lbl setFont:[UIFont boldSystemFontOfSize:24.0f]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setTextAlignment:NSTextAlignmentRight];
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 44, 44)];
	
    if(row >= 0 && row < 5)
		iv.image =  [UIImage imageNamed:@"time_night"];
	else if(row > 4 && row < 20)
		iv.image =  [UIImage imageNamed:@"time_day"];
	else if(row > 19)
		iv.image = [UIImage imageNamed:@"time_night"];
	else {
		iv.image = [UIImage imageNamed:@"time_any"];
	}
    
	NSInteger extendedHour = [aTimes[row] intValue];
	lbl.text = [ExtendedHour getLocalizedTextForExtendedHour:extendedHour];
	
	[v addSubview:lbl];
	[v addSubview:iv];
	
	
	return v;
	
}

- (NSInteger)numberOfComponentsInPickerView:(UIPickerView *)pickerView;
{
    return 1;
}

- (void)pickerView:(UIPickerView *)pickerView didSelectRow:(NSInteger)row inComponent:(NSInteger)component
{
    //mlabel.text=    [arrayNo objectAtIndex:row];
	NSInteger extendedHour = [aTimes[row] intValue];
	self.selectedExtendedHour = extendedHour;
	lblTime.text = [ExtendedHour getLocalizedTextForExtendedHour:extendedHour];
    
    [aValues removeAllObjects];
    [aValues addObject:[self textFromDate:datePicker.date]];
    [aValues addObject:[ExtendedHour getLocalizedTextForExtendedHour:selectedExtendedHour]];
    
    [tableList reloadData];
    [tableList selectRowAtIndexPath:indexPathSelected animated:NO scrollPosition:UITableViewScrollPositionNone];
}

- (NSInteger)pickerView:(UIPickerView *)pickerView numberOfRowsInComponent:(NSInteger)component;
{
    return [aTimes count];
}

- (NSString *)pickerView:(UIPickerView *)pickerView titleForRow:(NSInteger)row forComponent:(NSInteger)component;
{
	NSInteger extendedHour = [aTimes[row] intValue];
    return [ExtendedHour getLocalizedTextForExtendedHour:extendedHour];
}


-(void)setSelectedTime
{
	NSString *currTime = [ExtendedHour getLocalizedTextForExtendedHour:initialExtendedHour]; /*MOB-5924
                                                                                              OK, this one was caused by the code looking to whatever a label was set to, instead of looking at what the value is.*/
	
	for(int i = 0; i < [aTimes count]; i++)
	{
		NSInteger extendedHour = [aTimes[i] intValue];
		NSString *extendedHourText = [ExtendedHour getLocalizedTextForExtendedHour:extendedHour];
		if([currTime isEqualToString:extendedHourText])
		{
			[pickerView selectRow:i inComponent:0 animated:YES];
			return;
		}
	}
}

	
#pragma mark -
#pragma mark Switch up the views
-(IBAction)switchToTime:(id)sender
{
	isTime = YES;
	[self setDateTimeViews];
}
	
-(IBAction)switchToDate:(id)sender
{
	isTime = NO;
	[self setDateTimeViews];
	
//	UIButton *but = [UIButton alloc];
//	[but setE
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
        cell.lblSubheading.text = [self getDepartureDateLabel];
    else
        cell.lblSubheading.text = [self getDepartureTimeLabel];
    
    if(isDateSelected && row == 0)
        self.indexPathSelected = indexPath;
    else if(!isDateSelected && row == 1)
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
        [self switchToDate:nil];
    }
    else
    {
        self.indexPathSelected = indexPath;
        [self switchToTime:nil];
    }
    
    
}
@end
