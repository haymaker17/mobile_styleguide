//
//  TrainBookListingVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainBookListingVC.h"
#import	"TrainBookingDetailCell.h"
#import "TrainBookingItem.h"
#import "FormatUtils.h"
#import "ConcurMobileAppDelegate.h"
#import "TrainSearchingVC.h"
#import "TrainStopsVC.h"
#import "MobileAlertView.h"
#import "DateTimeFormatter.h"

@implementation TrainBookListingVC
@synthesize aList, tableList, selectedRow, trainBooking, isReturn, stationArrive, stationDepart, isRound, numItems, noView;
/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	//[noView setHidden:YES];
	
	self.navigationController.toolbar.hidden = NO;
	self.navigationController.navigationBar.hidden = NO;
    [super viewDidLoad];
	
	//[self initList]; //sample test data ripped from Amtrak
	
	if(isReturn)
		self.title = [Localizer getLocalizedText:@"Select Train"];
	else 
		self.title = [Localizer getLocalizedText:@"Select Train"];
	
	[self configureToolbar];
	
	if([aList count] > 0 )
		[noView setHidden:YES];
	else 
		noView.hidden = NO;

}

-(void) initList
{
	aList = [[NSMutableArray alloc] initWithObjects:nil];
	TrainBookingItem *tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"7:30 AM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"11:00 AM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"501 Cascades";
	tbi.duration = @"3 hr, 30 min";
	tbi.canAddBike = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"ENTERTAINMENT"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Reserved Coach Seat";
	tbi.amount = 37.00;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"7:30 AM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"11:00 AM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"501 Cascades";
	tbi.duration = @"3 hr, 30 min";
	tbi.canAddBike = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"ENTERTAINMENT"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Business Class Seat";
	tbi.amount = 57.0;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"9:45 AM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"1:50 PM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"11 Coast Starlight";
	tbi.duration = @"4 hr, 5 min";
	tbi.canAddBike = NO;
	tbi.canBuyRooms = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"DINING"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Reserved Coach Seat";
	tbi.amount = 42.0;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"9:45 AM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"1:50 PM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"11 Coast Starlight";
	tbi.duration = @"4 hr, 5 min";
	tbi.canAddBike = NO;
	tbi.canBuyRooms = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"DINING"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Lower Level Seat";
	tbi.amount = 42.0;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"11:25 AM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"2:55 PM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"513 Cascades";
	tbi.duration = @"3 hr, 30 min";
	tbi.canAddBike = NO;
	tbi.canBuyRooms = NO;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"ENTERTAINMENT"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Reserved Coach Seat";
	tbi.amount = 42.0;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"2:20 PM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"5:50 PM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"507 Cascades";
	tbi.duration = @"3 hr, 30 min";
	tbi.canAddBike = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"ENTERTAINMENT"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Reserved Coach Seat";
	tbi.amount = 37.0;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"2:20 PM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"5:50 PM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"507 Cascades";
	tbi.duration = @"3 hr, 30 min";
	tbi.canAddBike = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"ENTERTAINMENT"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Business Class Seat";
	tbi.amount = 51.0;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"5:30 PM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"9:00 PM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"509 Cascades";
	tbi.duration = @"3 hr, 30 min";
	tbi.canAddBike = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"ENTERTAINMENT"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Reserved Coach Seat";
	tbi.amount = 37.0;
	[aList addObject:tbi];
	
	tbi = [[TrainBookingItem alloc] init];
	tbi.departureDate = @"Mon Jul 26 2010";
	tbi.departureTime = @"5:30 PM";
	tbi.departureIata = @"SEA";
	tbi.departureStation = @"Seattle, WA (SEA)";
	tbi.arrivalDate = @"Mon Jul 26 2010";
	tbi.arrivalTime = @"9:00 PM";
	tbi.arrivalIata = @"PDX";
	tbi.arrivalStation = @"Portland, OR (PDX)";
	tbi.trainType = @"509 Cascades";
	tbi.duration = @"3 hr, 30 min";
	tbi.canAddBike = YES;
	[tbi.aAmenities addObject:@"CHECKED_BAG"];
	[tbi.aAmenities addObject:@"LOUNGE"];
	[tbi.aAmenities addObject:@"ENTERTAINMENT"];
	[tbi.aAmenities addObject:@"RAMP"];
	tbi.seatType = @"Business Class Seat";
	tbi.amount = 51.0;
	[aList addObject:tbi];
	
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

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




#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}


//- (NSIndexPath *)tableView:(UITableView *)tableView willSelectRowAtIndexPath:(NSIndexPath *)indexPath {
//    return nil;
//}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return [aList count];
}

-(NSString *)getSeatClass:(NSString *)seatClass
{
	if([seatClass isEqualToString:@"C"])
		return @"Business Class Seat";
	else if([seatClass isEqualToString:@"Y"])
		return @"Coach Reserved Seat";
	else 
		return seatClass;
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	//NSUInteger section = [indexPath section];
	
	if(!isRound)
	{
		TrainBookingDetailCell *cell = (TrainBookingDetailCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainBookingDetailCell"];
		if (cell == nil)
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainBookingDetailCellv2" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[TrainBookingDetailCell class]])
					cell = (TrainBookingDetailCell *)oneObject;
		}
		
		RailChoiceData *rcd = aList[row];
		RailChoiceSegmentData *segmentDepart = (rcd.segments)[0];
		RailChoiceTrainData *train = (segmentDepart.trains)[0];
		
//		if(selectedRow == row)
//			[cell setAccessoryType:UITableViewCellAccessoryCheckmark];
//		else 
//			[cell setAccessoryType:UITableViewCellAccessoryNone];
		
		//NSLog(@"Rail Start");
		cell.lblDepartureDate.text = [DateTimeFormatter formatDateMedium:train.depDateTime];
		cell.lblDepartureTime.text = [DateTimeFormatter formatTimeForTravel:train.depDateTime];
		cell.lblDepartureStation.text = stationDepart; // train.depAirp;
		cell.lblArrivalTime.text = [DateTimeFormatter formatTimeForTravel:train.arrDateTime];
		cell.lblArivalDate.text = [DateTimeFormatter formatDateMedium:train.arrDateTime];
		cell.lblArrivalStation.text = stationArrive; // train.arrAirp;
		cell.lblDuration.text = [self formatDuration:segmentDepart.totalTime];
		cell.lblSeat.text = [self getSeatClass:train.fltClass]; // [NSString stringWithFormat:@"Seat Class: %@-%@", train.fltClass, train.bic];
		cell.lblAmount.text = [FormatUtils formatMoney:rcd.cost crnCode:@"USD"];
		if(train.fltNum == nil)
			train.fltNum = @"--";
		
		NSString *numLegs = @"1";
		numLegs = [NSString stringWithFormat:@"%d", [segmentDepart.trains count]];
		
		cell.lblTrain.text = [NSString stringWithFormat:@"Train %@, #Legs: %@", train.fltNum, numLegs];
		//NSLog(@"Rail END");
		
		cell.parentVC = self;
		
		cell.scroller.delegate = self;
		
		cell.scroller.clipsToBounds = YES;
		cell.scroller.scrollEnabled = YES;
		
		for(UIView *v in cell.scroller.subviews)
			[v removeFromSuperview];
		
		if([rcd.seats count] > 0)
		{
			float btnW = 140;
			float btnH = 30.0;
			float lblH = 25.0;
			float x = 0;
			float y = 0;
			float yBtn = 31;
			
			[cell.scroller setContentSize:CGSizeMake(((btnW + 15) * [rcd.seats count]),60.0)];
			
			int iTag = 0;
			for(NSArray * a in rcd.seats)
			{
				NSString *seatCost = a[1];
				NSString *seatCurrencyCode = a[2];
				NSString *seatDescription = a[3];
				
				UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(x, y, btnW, lblH)];
				lbl.text = seatDescription;
				lbl.numberOfLines = 2;
				lbl.lineBreakMode = NSLineBreakByWordWrapping;
				[cell.scroller addSubview:lbl];
				lbl.backgroundColor = [UIColor clearColor];
				[lbl setFont:[UIFont systemFontOfSize:10]];
				[lbl setTextAlignment:NSTextAlignmentCenter];

				NSString *formattedCost = [FormatUtils formatMoney:seatCost crnCode:seatCurrencyCode];
				UIButton *btn = [ExSystem makeColoredButtonRegular:@"GREEN" W:btnW H:btnH Text:formattedCost SelectorString:@"selectBooking:" MobileVC:self];
				btn.frame = CGRectMake(x, yBtn, btnW, btnH);
				btn.tag = iTag;
				[cell.scroller addSubview:btn];
				
				x = x + btnW + 10;
				iTag++;
			}
		}
		return cell;
	}
	else {
		TrainBookingDetailCell *cell = (TrainBookingDetailCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainBookingDetailRoundCell"];
		if (cell == nil)
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainBookingDetailRoundCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[TrainBookingDetailCell class]])
					cell = (TrainBookingDetailCell *)oneObject;
		}
		
		RailChoiceData *rcd = aList[row];
		RailChoiceSegmentData *segmentDepart = (rcd.segments)[0];
		RailChoiceTrainData *train = (segmentDepart.trains)[0];
		RailChoiceTrainData *trainArrive = (segmentDepart.trains)[([segmentDepart.trains count] - 1)];
		
//		if(selectedRow == row)
//			[cell setAccessoryType:UITableViewCellAccessoryCheckmark];
//		else 
//			[cell setAccessoryType:UITableViewCellAccessoryNone];
		
		cell.lblDepartureDate.text = [DateTimeFormatter formatDateMedium:train.depDateTime];
		cell.lblDepartureTime.text = [DateTimeFormatter formatTimeForTravel:train.depDateTime];
		cell.lblDepartureStation.text = stationDepart; // train.depAirp;
		cell.lblArrivalTime.text = [DateTimeFormatter formatTimeForTravel:trainArrive.arrDateTime];
		cell.lblArivalDate.text = [DateTimeFormatter formatDateMedium:trainArrive.arrDateTime];
		cell.lblArrivalStation.text = stationArrive; // train.arrAirp;
		cell.lblDuration.text = [self formatDuration:segmentDepart.totalTime];
		cell.lblSeat.text = [self getSeatClass:train.fltClass]; //[NSString stringWithFormat:@"Seat Class: %@-%@", train.fltClass, train.bic];
		cell.lblAmount.text = [FormatUtils formatMoney:rcd.cost crnCode:@"USD"];
		if(train.fltNum == nil)
			train.fltNum = @"--";
		
		NSString *numLegs = @"1";
		numLegs = [NSString stringWithFormat:@"%d", [segmentDepart.trains count]];
		
		cell.lblTrain.text = [NSString stringWithFormat:@"Train %@, #Legs: %@", train.fltNum, numLegs];
		
		//round leg
		segmentDepart = (rcd.segments)[1]; //should only have 2 segments if round trip
		train = (segmentDepart.trains)[0];
		
		if(selectedRow == row)
		{
			//[cell.ivBack setHidden:NO];
			//[cell setAccessoryType:UITableViewCellAccessoryCheckmark];
			//cell.lblAmount.backgroundColor = [UIColor colorWithRed:14.0f/255.0f green:169.0f/255.0f blue:29.0f/255.0f alpha:1.0f];
		}
		else 
		{
			//[cell.ivBack setHidden:YES];
			//[cell setAccessoryType:UITableViewCellAccessoryDetailDisclosureButton];
			//cell.lblAmount.backgroundColor = [UIColor colorWithRed:146.0f/255.0f green:189.0f/255.0f blue:247.0f/255.0f alpha:1.0f];
		}
		
		cell.lblDepartureDateR.text = [DateTimeFormatter formatDateMedium:train.depDateTime];
		cell.lblDepartureTimeR.text = [DateTimeFormatter formatTimeForTravel:train.depDateTime];
		cell.lblDepartureStationR.text = stationArrive; // train.depAirp;
		cell.lblArrivalTimeR.text = [DateTimeFormatter formatTimeForTravel:train.arrDateTime];
		cell.lblArivalDateR.text = [DateTimeFormatter formatDateMedium:train.arrDateTime];
		cell.lblArrivalStationR.text = stationDepart; // train.arrAirp;
		cell.lblDurationR.text = [self formatDuration:segmentDepart.totalTime];
		
		if(train.fltNum == nil)
			train.fltNum = @"--";
		
		numLegs = [NSString stringWithFormat:@"%d", [segmentDepart.trains count]];
		
		cell.lblTrainR.text = [NSString stringWithFormat:@"Train %@, #Legs: %@", train.fltNum, numLegs];
		//cell.lblTrain.text = [NSString stringWithFormat:@"Train %@", train.fltNum];
		
		cell.parentVC = self;
		
		cell.scroller.delegate = self;
		
		//[cell..scroller setBackgroundColor:[UIColor blackColor]];
		//[cell.scroller setCanCancelContentTouches:NO];
		
		//cell.scroller.indicatorStyle = UIScrollViewIndicatorStyleWhite;
		cell.scroller.clipsToBounds = YES;
		cell.scroller.scrollEnabled = YES;
		//cell.scroller.pagingEnabled = YES;
//		[cell.scroller setContentSize:CGSizeMake((150.0 * [rcd.seats count]),60.0)];
//		[cell.scroller setContentSize:CGSizeMake(700,60.0)];
		
		for(UIView *v in cell.scroller.subviews)
			[v removeFromSuperview];
		
		if([rcd.seats count] > 0)
		{
			float btnW = 140;
			float btnH = 30.0;
			float lblH = 25.0;
			float x = 0;
			float y = 0;
			float yBtn = 31;
			
			[cell.scroller setContentSize:CGSizeMake(((btnW + 15) * [rcd.seats count]),60.0)];
			
			int iTag = 0;
			for(NSArray * a in rcd.seats)
			{
				//NSString *seatBaseFare = [a objectAtIndex:0];
				NSString *seatCost = a[1];
				NSString *seatCurrencyCode = a[2];
				NSString *seatDescription = a[3];
				
				UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(x, y, btnW, lblH)];
				lbl.text = seatDescription;
				lbl.numberOfLines = 2;
				lbl.lineBreakMode = NSLineBreakByWordWrapping;
				[cell.scroller addSubview:lbl];
				lbl.backgroundColor = [UIColor clearColor];
				[lbl setFont:[UIFont systemFontOfSize:10]];
				[lbl setTextAlignment:NSTextAlignmentCenter];
				
//				UIButton *btn = [[UIButton alloc] initWithFrame:C	GRectMake(x, yBtn, btnW, btnH)];
//				[btn setBackgroundImage:[UIImage imageNamed:@"green_button.png"] forState:UIControlStateNormal];
//				//btn.titleLabel.text = ;
//				[btn setTitle:[FormatUtils formatMoney:seatCost crnCode:seatCurrencyCode] forState:UIControlStateNormal];
				NSString *formattedCost = [FormatUtils formatMoney:seatCost crnCode:seatCurrencyCode];
				UIButton *btn = [ExSystem makeColoredButtonRegular:@"GREEN" W:btnW H:btnH Text:formattedCost SelectorString:@"selectBooking:" MobileVC:self];
				btn.frame = CGRectMake(x, yBtn, btnW, btnH);
				btn.tag = iTag;
				[cell.scroller addSubview:btn];
//				[btn release];
				
				x = x + btnW + 10;
				iTag++;
			}
		}
		
		return cell;
	}

	
//	TrainBookingItem *tbi = [aList objectAtIndex:row];
//	
//	if(selectedRow == row)
//		[cell setAccessoryType:UITableViewCellAccessoryCheckmark];
//	else 
//		[cell setAccessoryType:UITableViewCellAccessoryNone];
//	
//	cell.lblDepartureDate.text = tbi.departureDate;
//	cell.lblDepartureTime.text = tbi.departureTime;
//	cell.lblDepartureStation.text = tbi.departureStation;
//	cell.lblArrivalTime.text = tbi.arrivalTime;
//	cell.lblArivalDate.text = tbi.arrivalDate;
//	cell.lblArrivalStation.text = tbi.arrivalStation;
//	cell.lblDuration.text = tbi.duration;
//	cell.lblSeat.text = tbi.seatType;
//	cell.lblAmount.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", tbi.amount] crnCode:@"USD"];
//	cell.lblTrain.text = tbi.trainType;
	
//	for(int i = 0; i < [tbi.aAmenities count]; i++)
//	{
//		NSString *imgType = [tbi.aAmenities objectAtIndex:i];
//		NSString *imgName = nil;
//		if([imgType isEqualToString:@"LOUNGE"])
//			imgName = @"train_icons_cup";
//		else if([imgType isEqualToString:@"DINING"])
//			imgName = @"train_icons_dining";
//		else if([imgType isEqualToString:@"CHECKED_BAG"])
//			imgName = @"train_icons_briefcase";
//		else if([imgType isEqualToString:@"RAMP"])
//			imgName = @"train_icons_wheelchair";
//		else if([imgType isEqualToString:@"ENTERTAINMENT"])
//			imgName = @"train_icons_entertainment";
//		
//		switch (i) {
//			case 0:
//				cell.iv1.image = [UIImage imageNamed:imgName];
//				break;
//			case 1:
//				cell.iv2.image = [UIImage imageNamed:imgName];
//				break;
//			case 2:
//				cell.iv3.image = [UIImage imageNamed:imgName];
//				break;
//			case 3:
//				cell.iv4.image = [UIImage imageNamed:imgName];
//				break;
//			case 4:
//				cell.iv5.image = [UIImage imageNamed:imgName];
//				break;
//			default:
//				break;
//		}
//	}
//	
//	for(int x = [tbi.aAmenities count]; x < 5; x++)
//	{
//		switch (x) {
//			case 0:
//				cell.iv1.image = nil;
//				break;
//			case 1:
//				cell.iv2.image = nil;
//				break;
//			case 2:
//				cell.iv3.image = nil;
//				break;
//			case 3:
//				cell.iv4.image = nil;
//				break;
//			case 4:
//				cell.iv5.image = nil;
//				break;
//			default:
//				break;
//		}
//	}
			
	//return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	return; //we don't know what to do with the stops and details, so let's not show anything.

	int lastSelectedRow = selectedRow;
	selectedRow = indexPath.row;
	
	NSUInteger _path[2] = {indexPath.section, lastSelectedRow};
	NSIndexPath *_indexPath = [[NSIndexPath alloc] initWithIndexes:_path length:2];
	NSArray *_indexPaths = @[_indexPath];
	
	[tableView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:YES];
	
	NSUInteger path[2] = {indexPath.section, indexPath.row};
	_indexPath = [[NSIndexPath alloc] initWithIndexes:path length:2];
	_indexPaths = @[_indexPath];
	[tableView selectRowAtIndexPath:_indexPath animated:YES scrollPosition: UITableViewScrollPositionNone];
	[tableView reloadRowsAtIndexPaths:_indexPaths withRowAnimation:YES];
	
	TrainBookingItem *tbi = aList[indexPath.row];
	trainBooking.departureTBI = tbi;

	RailChoiceData *rcd = aList[selectedRow];
	RailChoiceSegmentData *segmentDepart = (rcd.segments)[0];
	RailChoiceTrainData *train = (segmentDepart.trains)[0];
	
	TrainStopsVC *tsvc = [[TrainStopsVC alloc] initWithNibName:@"TrainStopsVC" bundle:nil];
	tsvc.train = train;
	tsvc.segmentDepart = segmentDepart;
	tsvc.rcd = rcd;
	tsvc.lblStation.text = stationDepart;
	
	NSString *numLegs = @"1";
	numLegs = [NSString stringWithFormat:@"%d", [segmentDepart.trains count]];
	tsvc.lblLegs.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Legs: token"], numLegs];
	tsvc.lblTrain.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Train: token"], train.fltNum];
	
	[self presentViewController:tsvc animated:YES completion:nil];
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	if(!isRound)
		return 132;
	else {
		return 190;
	}

}


-(IBAction) launchStops:(id)sender
{
	TrainStopsVC *tsvc = [[TrainStopsVC alloc] initWithNibName:@"TrainStopsVC" bundle:nil];
	[self presentViewController:tsvc animated:YES completion:nil];

}
	 

#pragma mark -
#pragma mark Other Methods
-(void)configureToolbar
{
	NSString *localText = [Localizer getLocalizedText:@"Number of schedules found: token"];
	NSString *numberItems = [NSString stringWithFormat:localText, numItems];
	const int buttonWidth = 160;
	const int buttonHeight = 30;
	UIView *cv = [[UIView alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, buttonWidth, buttonHeight)];
	lblText.numberOfLines = 2;
	lblText.lineBreakMode = NSLineBreakByWordWrapping;
	lblText.textAlignment = NSTextAlignmentCenter;
	lblText.text = numberItems;
	[lblText setBackgroundColor:[UIColor clearColor]];
	[lblText setTextColor:[UIColor whiteColor]];
	[lblText setShadowColor:[UIColor grayColor]];
	[lblText setShadowOffset:CGSizeMake(1, 1)];
	[lblText setFont:[UIFont boldSystemFontOfSize:12.0f]];
	[cv addSubview:lblText];

	UIBarButtonItem *btnCount = [[UIBarButtonItem alloc] initWithCustomView:cv];
	
	//UIBarButtonItem *btnNext = [self makeColoredButton:[UIColor blueColor] W:120.0 H:30.0 Text:@"Reserve Selected" SelectorString:@"selectBooking:"];
	//UIBarButtonItem *btnNext = [ExSystem makeColoredButton:@"GREEN" W:120.0 H:30.0 Text:@"Reserve Selected" SelectorString:@"selectBooking:" MobileVC:self];
	//[[UIBarButtonItem alloc] initWithTitle:@"Reserve Selected" style:UIBarButtonItemStyleBordered target:self action:@selector(selectBooking:)];
//	UIBarButtonItem *btnNextReturn = [[UIBarButtonItem alloc] initWithTitle:@"Select return train" style:UIBarButtonItemStyleBordered target:self action:@selector(selectBooking:)];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
	NSArray *toolbarItems = nil;
	
//	if(isReturn)
//		toolbarItems = [NSArray arrayWithObjects: flexibleSpace, btnNextReturn, nil];
//	else 
//		toolbarItems = [NSArray arrayWithObjects: flexibleSpace, btnNext, nil];
	
//	if([aList count] < 1)
		toolbarItems = @[flexibleSpace, btnCount, flexibleSpace];
//	else 
//		toolbarItems = [NSArray arrayWithObjects: flexibleSpace, btnCount, flexibleSpace, btnNext, nil];
	
	[self setToolbarItems:toolbarItems animated:NO];
	//[btnNext release];
//	[btnNextReturn release];
}


-(void)selectBooking:(id)sender
{
//	if(trainBooking.isRoundTrip && isReturn == NO)
//	{
//		TrainBookListingVC *tblvc = [[TrainBookListingVC alloc] initWithNibName:@"TrainBookListingVC" bundle:nil];
//		tblvc.trainBooking = trainBooking;
//		tblvc.isReturn = YES;
//		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//		[delegate.navController pushViewController:tblvc animated:YES];
//		[tblvc release];
//	}
//	else {
//		TrainSearchingVC *tsvc = [[TrainSearchingVC alloc] initWithNibName:@"TrainSearchingVC" bundle:nil];
//		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//		[delegate.navController pushViewController:tsvc animated:YES];
//		[tsvc release];
//	}
	
//	UIAlertView *alert = [[UIAlertView alloc] 
//						  initWithTitle:@"Reserve Train"
//						  message:@"Are you sure that you want to reserve the selected train(s)?"
//						  delegate:self 
//						  cancelButtonTitle:@"Close"
//						  otherButtonTitles:nil];
//	[alert show];
//	[alert release];
	
	UIAlertView* alert = [[MobileAlertView alloc] initWithTitle: [Localizer getLocalizedText:@"Reserve Train"] 
							message:[Localizer getLocalizedText:@"Are you sure that you want to reserve the selected train(s)?"]
							delegate:self 
							cancelButtonTitle: [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
							otherButtonTitles:[Localizer getLocalizedText:@"OK"], 
						  nil];
	[alert show];

	
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (buttonIndex == 1)
	{
		UIAlertView* alert = [[MobileAlertView alloc] initWithTitle: [Localizer getLocalizedText:@"Reserve Train"] 
			message:[Localizer getLocalizedText:@"MSG_TRAIN_SELL_NOT_READY"]
			delegate:nil 
			cancelButtonTitle: [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"] 
			otherButtonTitles:[Localizer getLocalizedText:@"OK"], 
			nil];
		[alert show];
	}
}

-(NSString *)formatDuration:(int)duration
{
	if(duration < 59)
	{
		return [Localizer getLocalizedText:@"token minute(s)"];
	}
	else {
		int hours = duration / 60;
		int minutes = duration - (hours * 60);
		return [NSString stringWithFormat:[Localizer getLocalizedText:@"token hour(s) %d minute(s)"], hours, minutes];
	}
}


#pragma mark -
#pragma mark Button Maker
-(UIBarButtonItem *)makeColoredButton:(UIColor *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle SelectorString:(NSString *)selectorString
{
	
	UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	
	NSString *btnImage = @"red_test_button";
	
	[button setBackgroundImage:[[UIImage imageNamed:btnImage]
								stretchableImageWithLeftCapWidth:12.0f 
								topCapHeight:0.0f]
					  forState:UIControlStateNormal];
	
	
	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0,w, h);
	
	//[button addTarget:self action:@selector(searchBooking:) forControlEvents:UIControlEventTouchUpInside];
	[button addTarget:self action:NSSelectorFromString(selectorString) forControlEvents:UIControlEventTouchUpInside];
	
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, w, h)];
	
	lbl.font = [UIFont boldSystemFontOfSize:13];
	//lbl.textColor = [UIColor whiteColor];
	//lbl.shadowColor = [UIColor lightGrayColor];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentCenter;
	
	
	lbl.textColor =  [UIColor whiteColor];
	lbl.shadowColor = [UIColor lightGrayColor];
	
	lbl.text = btnTitle;
	
	[v addSubview:button];
	[v addSubview:lbl];
	
	//create a UIBarButtonItem with the button as a custom view
	__autoreleasing UIBarButtonItem *customBarItem = [[UIBarButtonItem alloc] initWithCustomView:v];
	
	return customBarItem;
}
@end
