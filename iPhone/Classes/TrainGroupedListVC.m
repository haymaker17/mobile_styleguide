//
//  TrainGroupedListVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainGroupedListVC.h"
#import "TrainGroupedCell.h"
#import "RailChoiceData.h"
#import "RailChoiceSegmentData.h"
#import "RailChoiceTrainData.h"
#import "FormatUtils.h"
#import "DateTimeFormatter.h"
#import "TrainFareChoicesVC.h"
#import "ConcurMobileAppDelegate.h"
#import "ExSystem.h" 


@implementation TrainGroupedListVC
@synthesize lblFromLabel, lblFrom, lblToLabel, lblTo, lblDateRange, tableList, aKeys, dictGroups;
@synthesize vNothing;
@synthesize lblNothing;
@synthesize taFields;

// The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
/*
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization.
    }
    return self;
}
*/

-(void) viewWillAppear:(BOOL)animated
{
	self.title = [Localizer getLocalizedText:@"Train Choices"];
}


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	lblFromLabel.text = [Localizer getLocalizedText:@"From"];
	lblToLabel.text = [Localizer getLocalizedText:@"To"];
}



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
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
	self.lblFromLabel = nil;
	self.lblFrom = nil;
	self.lblToLabel = nil;
	self.lblTo = nil;
	self.lblDateRange = nil;
	self.tableList = nil;
}



#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{	
	return [aKeys count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	
	NSString *key = aKeys[row];
	NSMutableArray *railChoices = dictGroups[key];
	RailChoiceData *railChoice = railChoices[0];
    
    TrainGroupedCell *cell = nil;
    if([railChoice.segments count] > 1)
    {
         cell = (TrainGroupedCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainGroupedCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainGroupedCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[TrainGroupedCell class]])
                    cell = (TrainGroupedCell *)oneObject;
        }
    }
    else
    {
        cell = (TrainGroupedCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainGroupedSingleCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainGroupedSingleCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[TrainGroupedCell class]])
                    cell = (TrainGroupedCell *)oneObject;
        }
    }
	

    
    NSLog(@"railChoice.descript %@", railChoice.descript);
    NSArray *aSeats = [railChoice.descript componentsSeparatedByString:@" / "];
    NSString *regional1 = nil;
    if([aSeats count] > 0)
    {
        NSString *firstSeat = aSeats[0];
        NSRange ranger = [firstSeat rangeOfString:@"Acela" options:NSCaseInsensitiveSearch];
        if (ranger.location != NSNotFound)
            regional1 = [Localizer getLocalizedText:@"Express"];
        else
            regional1 = [Localizer getLocalizedText:@"Regional"];
            
    }
    else
        regional1 = @"";
    
    cell.lblRegional1.text = regional1;
	
	double lowest = 9999999999999;
	double highest = 0;
	for(RailChoiceData *rc in railChoices)
	{
		if([rc.cost doubleValue] < lowest)
			lowest = [rc.cost doubleValue];
		
		if([rc.cost doubleValue] > highest)
			highest = [rc.cost doubleValue];
	}
	
	cell.lblPrice1.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%g", lowest] crnCode:railChoice.currencyCode];
	cell.lblPrice2.text = [FormatUtils formatMoney:[NSString stringWithFormat:@"%g", highest] crnCode:railChoice.currencyCode];
    [cell.lblPrice1 setTextColor:[UIColor bookingBlueColor]];
    [cell.lblPrice2 setTextColor:[UIColor bookingBlueColor]];
    
    cell.lblTo.text = [NSString stringWithFormat:@"%@:", [Localizer getLocalizedText:@"Starting"]];
	
	RailChoiceSegmentData *seg = (railChoice.segments)[0];
	RailChoiceTrainData	*train1 = (seg.trains)[0];
	
    int hours = seg.totalTime / 60;
	int mins = seg.totalTime - (60 * hours);
	NSString *duration = [NSString stringWithFormat:@"%dh %dm", hours, mins];
    
	cell.lblTrain1.text = [NSString stringWithFormat:@"%@ #%@ (%@)", [Localizer getLocalizedText:@"Train"], train1.fltNum, duration];

    NSMutableString *sDeparture = [[NSMutableString alloc] initWithString:@""];
	[sDeparture appendString:train1.depAirp];
    [sDeparture appendString:@" "];
	[sDeparture appendString:[DateTimeFormatter formatTimeForTravel:train1.depDateTime]];
    [sDeparture appendString:@" - "];
	[sDeparture appendString:train1.arrAirp];
    [sDeparture appendString:@" "];
	[sDeparture appendString: [DateTimeFormatter formatTimeForTravel:train1.arrDateTime]];
    cell.lblTrain1FromTime.text = sDeparture;
	
	if([railChoice.segments count] > 1)
	{
        NSString *regional2 = nil;
        if([aSeats count] > 1)
        {
            NSString *firstSeat = aSeats[1];
            NSRange ranger = [firstSeat rangeOfString:@"Acela" options:NSCaseInsensitiveSearch];
            if (ranger.location != NSNotFound)
                regional2 = [Localizer getLocalizedText:@"Express"];
            else
                regional2 = [Localizer getLocalizedText:@"Regional"];
            
        }
        else
            regional2 = @"";
        
        cell.lblRegional2.text = regional2;
        
		RailChoiceSegmentData *seg2 = (railChoice.segments)[1];
		RailChoiceTrainData	*train2 = (seg2.trains)[0];
        
        hours = seg2.totalTime / 60;
        mins = seg2.totalTime - (60 * hours);
        duration = [NSString stringWithFormat:@"%dh %dm", hours, mins];
		cell.lblTrain2.text = [NSString stringWithFormat:@"%@ # %@ (%@)",[Localizer getLocalizedText:@"Train"], train2.fltNum, duration];

        NSMutableString *sReturn = [[NSMutableString alloc] initWithString:@""];
        [sReturn appendString:train2.depAirp];
        [sReturn appendString:@" "];
        [sReturn appendString:[DateTimeFormatter formatTimeForTravel:train2.depDateTime]];
        [sReturn appendString:@" - "];
        [sReturn appendString:train2.arrAirp];
        [sReturn appendString:@" "];
        [sReturn appendString: [DateTimeFormatter formatTimeForTravel:train2.arrDateTime]];
        cell.lblTrain2FromTime.text = sReturn;
        
//		cell.lblTrain2FromCity.text = train2.depAirp;
//		cell.lblTrain2FromTime.text = [DateTimeFormatter formatTimeForTravel:train2.depDateTime];
//		cell.lblTrain2ToCity.text = train2.arrAirp;
//		cell.lblTrain2ToTime.text = [DateTimeFormatter formatTimeForTravel:train2.arrDateTime];
	}
	else {
//		int adjustment = 20;
//		cell.lblTrain1.frame = CGRectMake(cell.lblTrain1.frame.origin.x, cell.lblTrain1.frame.origin.y + adjustment, cell.lblTrain1.frame.size.width, cell.lblTrain1.frame.size.height);
//		cell.lblTrain1Time.frame = CGRectMake(cell.lblTrain1Time.frame.origin.x, cell.lblTrain1Time.frame.origin.y + adjustment, cell.lblTrain1Time.frame.size.width, cell.lblTrain1Time.frame.size.height);
//		cell.lblTrain1FromCity.frame = CGRectMake(cell.lblTrain1FromCity.frame.origin.x, cell.lblTrain1FromCity.frame.origin.y + adjustment, cell.lblTrain1FromCity.frame.size.width, cell.lblTrain1FromCity.frame.size.height);
//		cell.lblTrain1FromTime.frame = CGRectMake(cell.lblTrain1FromTime.frame.origin.x, cell.lblTrain1FromTime.frame.origin.y + adjustment, cell.lblTrain1FromTime.frame.size.width, cell.lblTrain1FromTime.frame.size.height);
//		cell.lblTrain1ToCity.frame = CGRectMake(cell.lblTrain1ToCity.frame.origin.x, cell.lblTrain1ToCity.frame.origin.y + adjustment, cell.lblTrain1ToCity.frame.size.width, cell.lblTrain1ToCity.frame.size.height);
//		cell.lblTrain1ToTime.frame = CGRectMake(cell.lblTrain1ToTime.frame.origin.x, cell.lblTrain1ToTime.frame.origin.y + adjustment, cell.lblTrain1ToTime.frame.size.width, cell.lblTrain1ToTime.frame.size.height);
//        
//        cell.lblTrain2FromTime.text = @"";
//        cell.lblTrain2.text = @"";
	}


	cell.lblChoices.text = [NSString stringWithFormat:@"%lu %@", (unsigned long)[railChoices count], [Localizer getLocalizedText:@"choices"]];
	
	return cell;
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger	row = indexPath.row;
	
	NSString *key = aKeys[row];
	NSMutableArray *railChoices = dictGroups[key];
	RailChoiceData *railChoice = railChoices[0];
	
	TrainFareChoicesVC *tblvc = [[TrainFareChoicesVC alloc] initWithNibName:@"TrainFareChoicesVC" bundle:nil];
    [tblvc view];
	tblvc.hidesBottomBarWhenPushed = NO;
    tblvc.taFields = self.taFields;
	tblvc.aKeys = railChoices; // aggTrains;// trainTimes.keys;
	tblvc.dictGroups = self.dictGroups;
	tblvc.railChoice = railChoice;
	
	ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	if([UIDevice isPad])
		[self.navigationController pushViewController:tblvc animated:YES];
	else 
		[delegate.navController pushViewController:tblvc animated:YES];

	tblvc.lblTo.text = lblTo.text;
	tblvc.lblFrom.text = lblFrom.text;
	tblvc.lblDateRange.text = lblDateRange.text;
	
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];
	
	NSString *key = aKeys[row];
	NSMutableArray *railChoices = dictGroups[key];
	RailChoiceData *railChoice = railChoices[0];
    
    if([railChoice.segments count] > 1)
        return 110; //MOB-5975
    else
        return 60;
//	return 110;
}

@end
