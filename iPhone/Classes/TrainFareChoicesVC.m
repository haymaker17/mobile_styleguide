//
//  TrainFareChoicesVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainFareChoicesVC.h"
#import "TrainFareChoiceCell.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"
#import "TrainDetailVC.h"
#import "ConcurMobileAppDelegate.h"
#import "UserConfig.h"
#import "MsgControl.h"
#import "ExSystem.h" 

#import "PolicyViolationConstants.h"
#import "HotelViolation.h"

@implementation TrainFareChoicesVC
@synthesize		lblTrain1, lblTrain1Time, lblTrain1FromCity, lblTrain1FromTime, lblTrain1ToCity, lblTrain1ToTime;
@synthesize		lblTrain2, lblTrain2Time, lblTrain2FromCity, lblTrain2FromTime, lblTrain2ToCity, lblTrain2ToTime;
@synthesize lblFromLabel, lblFrom, lblToLabel, lblTo, lblDateRange, tableList, aKeys, dictGroups, railChoice;
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


-(void) viewWillAppear:(BOOL)animated
{
	self.title = [Localizer getLocalizedText:@"Fare Choices"];
	
	if(railChoice != nil)
	{
		RailChoiceSegmentData *seg = (railChoice.segments)[0];
		RailChoiceTrainData	*train1 = (seg.trains)[0];
		

		
		NSString *vehicle = [Localizer getLocalizedText:@"Train"];
		if([train1.bic isEqualToString:@"T"])
			vehicle = [Localizer getLocalizedText:@"Bus"];
		
		lblTrain1.text = [NSString stringWithFormat:@"%@ # %@", vehicle, train1.fltNum];
		int hours = seg.totalTime / 60;
		int mins = seg.totalTime - (60 * hours);
		lblTrain1Time.text = [NSString stringWithFormat:@"%dh %dm", hours, mins];
		[self adjustLabel:lblTrain1 LabelValue:lblTrain1Time HeadingText:lblTrain1.text ValueText:lblTrain1Time.text ValueColor:[UIColor blackColor] W:-1];
		
		lblTrain1FromCity.text = train1.depAirp;
		lblTrain1FromTime.text = [DateTimeFormatter formatTimeForTravel:train1.depDateTime];
		lblTrain1ToCity.text = train1.arrAirp;
		lblTrain1ToTime.text = [DateTimeFormatter formatTimeForTravel:train1.arrDateTime];
		
		if([railChoice.segments count] > 1)
		{
			RailChoiceSegmentData *seg2 = (railChoice.segments)[1];
			RailChoiceTrainData	*train2 = (seg2.trains)[0];
			if([train2.bic isEqualToString:@"T"])
				vehicle = [Localizer getLocalizedText:@"Bus"];
			else 
				vehicle = [Localizer getLocalizedText:@"Train"];
			lblTrain2.text = [NSString stringWithFormat:@"%@ # %@", vehicle, train2.fltNum];
			hours = seg2.totalTime / 60;
			mins = seg2.totalTime - (60 * hours);
			lblTrain2Time.text = [NSString stringWithFormat:@"%dh %dm", hours, mins];
			
			[self adjustLabel:lblTrain2 LabelValue:lblTrain2Time HeadingText:lblTrain2.text ValueText:lblTrain2Time.text ValueColor:[UIColor blackColor] W:-1];
			
			lblTrain2FromCity.text = train2.depAirp;
			lblTrain2FromTime.text = [DateTimeFormatter formatTimeForTravel:train2.depDateTime];
			lblTrain2ToCity.text = train2.arrAirp;
			lblTrain2ToTime.text = [DateTimeFormatter formatTimeForTravel:train2.arrDateTime];
		}
		else {
			lblTrain2.hidden = YES;
			lblTrain2Time.hidden = YES;
			lblTrain2FromCity.hidden = YES;
			lblTrain2FromTime.hidden = YES;
			lblTrain2ToCity.hidden = YES;
			lblTrain2ToTime.hidden = YES;
		}

	}
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
}




#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 2;
}


-(NSString*) tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    if(section == 1)
        return [Localizer getLocalizedText:@"Select seat class below"];
    else
        return @"";
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{	
    if(section == 0)
        return 1;
    else
        return [aKeys count];
}


+(NSString *) fetchSegmentSeats:(NSString *)seatDescription NumberOfSeatsDesired:(int) numberOfSeatsDesired FrontToBack:(BOOL) isForward  JustTheOne:(int) segmentPosition
{
	__autoreleasing NSArray *aSeats = [seatDescription componentsSeparatedByString:@"/"];
	
	NSMutableString *seats = [[NSMutableString alloc] initWithString:@""];
	
	if(segmentPosition > 0)
	{
		int pos = segmentPosition;
		if( segmentPosition < [aSeats count])
			return [aSeats[pos] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]];
		else {
			return @"";
		}

	}
	
	if(isForward)
	{
		for(int i = 0; i < numberOfSeatsDesired; i++)
		{
			if([seats length] > 0)
				[seats appendString:@" / "];
			
			[seats appendString:[aSeats[i] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]]];
		}
	}
	else {
		int start = [aSeats count] - numberOfSeatsDesired;
		
		for(int i = start; i < [aSeats count]; i++)
		{
			if([seats length] > 0)
				[seats appendString:@" / "];
			
			[seats appendString:[aSeats[i] stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]]];
		}
	}
	
	return seats;

}

-(void) configureHeadingCell:(TrainGroupedCell*)cell atIndexPath:(NSIndexPath *)indexPath
{

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
    
    cell.lblTo.hidden = YES;
    cell.lblPrice1.hidden = YES;
    
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
	
	if([railChoice.segments count] > 1)
	{
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
        
        //		cell.lblTrain2FromCity.text = train2.depAirp;
        //		cell.lblTrain2FromTime.text = [DateTimeFormatter formatTimeForTravel:train2.depDateTime];
        //		cell.lblTrain2ToCity.text = train2.arrAirp;
        //		cell.lblTrain2ToTime.text = [DateTimeFormatter formatTimeForTravel:train2.arrDateTime];
	}
//	else {
////		int adjustment = 20;
////		cell.lblTrain1.frame = CGRectMake(cell.lblTrain1.frame.origin.x, cell.lblTrain1.frame.origin.y + adjustment, cell.lblTrain1.frame.size.width, cell.lblTrain1.frame.size.height);
////		cell.lblTrain1Time.frame = CGRectMake(cell.lblTrain1Time.frame.origin.x, cell.lblTrain1Time.frame.origin.y + adjustment, cell.lblTrain1Time.frame.size.width, cell.lblTrain1Time.frame.size.height);
////		cell.lblTrain1FromCity.frame = CGRectMake(cell.lblTrain1FromCity.frame.origin.x, cell.lblTrain1FromCity.frame.origin.y + adjustment, cell.lblTrain1FromCity.frame.size.width, cell.lblTrain1FromCity.frame.size.height);
////		cell.lblTrain1FromTime.frame = CGRectMake(cell.lblTrain1FromTime.frame.origin.x, cell.lblTrain1FromTime.frame.origin.y + adjustment, cell.lblTrain1FromTime.frame.size.width, cell.lblTrain1FromTime.frame.size.height);
////		cell.lblTrain1ToCity.frame = CGRectMake(cell.lblTrain1ToCity.frame.origin.x, cell.lblTrain1ToCity.frame.origin.y + adjustment, cell.lblTrain1ToCity.frame.size.width, cell.lblTrain1ToCity.frame.size.height);
////		cell.lblTrain1ToTime.frame = CGRectMake(cell.lblTrain1ToTime.frame.origin.x, cell.lblTrain1ToTime.frame.origin.y + adjustment, cell.lblTrain1ToTime.frame.size.width, cell.lblTrain1ToTime.frame.size.height);
////        
////        cell.lblTrain2FromTime.text = @"";
////        cell.lblTrain2.text = @"";
//	}
    
}

// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	NSUInteger section = [indexPath section];
    
    if(section == 0)
    {
        //heading
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
        

        [self configureHeadingCell:cell atIndexPath:indexPath];
        [cell setAccessoryType:UITableViewCellAccessoryNone];
        return cell;
        
    }
	else
    {
        TrainFareChoiceCell *cell = (TrainFareChoiceCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainFareChoiceCell"];
        if (cell == nil)
        {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainFareChoiceCell" owner:self options:nil];
            for (id oneObject in nib)
                if ([oneObject isKindOfClass:[TrainFareChoiceCell class]])
                    cell = (TrainFareChoiceCell *)oneObject;
        }
        

        RailChoiceData *rc = aKeys[row];
        
        cell.lblCost.text = [FormatUtils formatMoney:rc.cost crnCode:rc.currencyCode];
        [cell.lblCost setTextColor:[UIColor bookingBlueColor]];

        RailChoiceSegmentData *seg = (rc.segments)[0];
        RailChoiceTrainData	*train1 = (seg.trains)[0];
        RailChoiceTrainData	*trainFinal = nil;
        if([seg.trains count] > 1)
            trainFinal = (seg.trains)[[seg.trains count] - 1];

        NSString *seats = [TrainFareChoicesVC fetchSegmentSeats:rc.descript NumberOfSeatsDesired:[seg.trains count] FrontToBack:YES JustTheOne:0];
        if(trainFinal != nil)
            cell.lblLine1.text = [NSString stringWithFormat:@"%@ - %@", train1.depAirp, trainFinal.arrAirp];
        else 
            cell.lblLine1.text = [NSString stringWithFormat:@"%@ - %@", train1.depAirp, train1.arrAirp];
        cell.lblLine2.text = seats;
        
        if([rc.segments count] > 1)
        {
            RailChoiceSegmentData *seg2 = (rc.segments)[1];
            RailChoiceTrainData	*train2 = (seg2.trains)[0];

            seats = [TrainFareChoicesVC fetchSegmentSeats:rc.descript NumberOfSeatsDesired:[seg2.trains count] FrontToBack:NO JustTheOne:0];
            
            if(train2 != nil)
            {
                if([seg2.trains count] > 1)
                    trainFinal = (seg2.trains)[[seg2.trains count] - 1];
                
                if(trainFinal != nil)
                    cell.lblLine3.text = [NSString stringWithFormat:@"%@ - %@", train2.depAirp, trainFinal.arrAirp];
                else 
                    cell.lblLine3.text = [NSString stringWithFormat:@"%@ - %@", train2.depAirp, train2.arrAirp];
            
                cell.lblLine4.text = seats;
            }
        }
        else {
            int adjustment = 20;
            if(cell.lblLine1 != nil)
                cell.lblLine1.frame = CGRectMake(cell.lblLine1.frame.origin.x, cell.lblLine1.frame.origin.y + adjustment, cell.lblLine1.frame.size.width, cell.lblLine1.frame.size.height);
            
            if(cell.lblLine2 != nil)
            cell.lblLine2.frame = CGRectMake(cell.lblLine2.frame.origin.x, cell.lblLine2.frame.origin.y + adjustment, cell.lblLine2.frame.size.width, cell.lblLine2.frame.size.height);
        }

        cell.lblLine5.hidden = YES;
        if([UserConfig getSingleton].showGDSNameInSearchResults && [rc.gdsName length])
        {
            cell.lblLine5.hidden = NO;
            cell.lblLine5.text = [NSString stringWithFormat:@"(%@)", rc.gdsName];
        }
        
        int eLevel = [rc.maxEnforcementLevel intValue];
        if(rc.maxEnforcementLevel == nil || eLevel < kViolationLogForReportsOnly || eLevel == 100)
        {
            [cell.lblCost setTextColor:[UIColor bookingGreenColor]];
            cell.ivRule.hidden = YES;
        }
        else if(eLevel >= kViolationLogForReportsOnly && eLevel <= kViolationNotifyManager)
        {
            [cell.lblCost setTextColor:[UIColor bookingYellowColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_yellowex"];
            cell.ivRule.hidden = NO;
        }
        else if(eLevel >= kViolationRequiresPassiveApproval && eLevel <= kViolationRequiresApproval)
        {
            [cell.lblCost setTextColor:[UIColor bookingRedColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivRule.hidden = NO;
        }
        else if(eLevel == kViolationAutoFail)
        {
            [cell.lblCost setTextColor:[UIColor bookingGrayColor]];
            cell.ivRule.hidden = YES;
        }
        else
        {
            [cell.lblCost setTextColor:[UIColor bookingRedColor]];
            cell.ivRule.image = [UIImage imageNamed:@"icon_redex"];
            cell.ivRule.hidden = NO;
        }
        
        return cell;
    }
}


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.section == 0)
        return;
	
	NSUInteger	row = indexPath.row;
	
	RailChoiceData *rc = aKeys[row];
	
    if (rc.maxEnforcementLevel == nil || [rc.maxEnforcementLevel intValue] != kViolationAutoFail)
    {
        TrainDetailVC *tblvc = [[TrainDetailVC alloc] initWithNibName:@"TrainDetailVC" bundle:nil];
        tblvc.taFields = self.taFields;
        tblvc.aKeys = aKeys;
        tblvc.dictGroups = self.dictGroups;
        tblvc.railChoice = rc;
        [tblvc view];
        tblvc.hidesBottomBarWhenPushed = NO;
        
        ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        if([UIDevice isPad])
            [self.navigationController pushViewController:tblvc animated:YES];
        else
            [delegate.navController pushViewController:tblvc animated:YES];
        
        tblvc.lblFrom.text = lblFrom.text;
        tblvc.lblDateRange.text = lblDateRange.text;
    }
    else
    {
        NSMutableString *violationMessage = [[NSMutableString alloc] init];
        for (HotelViolation *violation in rc.violations)
        {
            if ([[violation enforcementLevel] intValue] == kViolationAutoFail)
                [violationMessage appendFormat:([violationMessage length] ? @"\n%@" : @"%@"),violation.message];
        }
        NSString *displayMessage = [violationMessage length] ? violationMessage : [@"Your company's travel policy will not allow this reservation." localize];
        //Display error message
        MobileAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:[Localizer getLocalizedText:@"Reservation Not Allowed"]
                                  message:displayMessage
                                  delegate:nil
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"]
                                  otherButtonTitles:nil];
		[alert show];
        [tableView deselectRowAtIndexPath:indexPath animated:YES];
    }	
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    int rowHeight = 82;
    if(indexPath.section == 0)
    {
        if([railChoice.segments count] > 1)
            rowHeight = 110;
        else
            rowHeight = 60;
    }
    else
    {
        if ([aKeys count] > indexPath.row)
        {
            if ([UserConfig getSingleton].showGDSNameInSearchResults)
            {
                NSManagedObject *managedObject = (aKeys)[indexPath.row];
                RailChoiceData *rc = (RailChoiceData *)managedObject;

                if([rc.gdsName length])
                {
                    rowHeight += 15;
                }
            }
        }
    }
    return rowHeight;
}


#pragma mark Label Adjustment
-(void) adjustLabel:(UILabel *) lblHeading LabelValue:(UILabel*) lblVal HeadingText:(NSString *) headText ValueText:(NSString *) valText ValueColor:(UIColor *) color W:(float)wOverride
{
	[lblHeading setHidden:NO];
	[lblVal setHidden:NO];
	
	NSString *val = [NSString stringWithFormat:@"%@", headText];
	lblHeading.text = val;// [NSString stringWithFormat:@"%@: %@", segRow2.rowLabel, segRow2.rowValue];
	
	CGSize lblSize = [val sizeWithFont:lblHeading.font];
	float x = lblHeading.frame.origin.x + lblSize.width + 3;
	
	//NSLog(@"lblHeading.frame.size.width %g", lblHeading.frame.size.width);
	float w = (lblHeading.frame.size.width - lblSize.width);
	if(wOverride > 0)
		w = wOverride - lblSize.width;
	lblVal.frame = CGRectMake(x, lblVal.frame.origin.y, w, lblVal.frame.size.height);
	lblVal.text = valText;
	
	if(color == nil)
		lblVal.textColor = [UIColor blackColor];
	else 
		lblVal.textColor = color;
}
@end
