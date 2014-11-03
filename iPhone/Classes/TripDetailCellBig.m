//
//  TripDetailCellBig.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "TripDetailCellBig.h"
#import "TripDetailsViewController.h"
#import "TripDetailCell.h"
#import "ViewConstants.h"

@implementation TripDetailCellBig

@synthesize label;
@synthesize labelDateRange;
@synthesize labelLocator;
@synthesize btnDetail;
@synthesize baseVC;
@synthesize rootVC;
@synthesize currentRow;
@synthesize btnDrill;
@synthesize tripKey;

@synthesize tableList;

@synthesize tripBits;
@synthesize keys;

-(void)resetTripBits:(NSMutableDictionary *)newBits KeyBits:(NSMutableArray *)keyBits
{
//	//NSLog(@"keyBits1 retaincount=%d", [keyBits retainCount]);
//	//NSLog(@"keys1.25 retaincount=%d", [keys retainCount]);
//	//[keys release];
//	//NSLog(@"keys1.5 retaincount=%d", [keys retainCount]);
//	//keys = [[NSMutableArray alloc] initWithArray:keyBits];
//	//NSLog(@"keys1.75 retaincount=%d", [keys retainCount]);
//	//NSLog(@"keyBits2 retaincount=%d", [keyBits retainCount]);
//	//keys = keyBits;
//	
//	//[tripBits release];
//	//tripBits = nil;
//	//tripBits = newBits; //trip.bookings; 
	
	[tableList reloadData];
}


- (IBAction)buttonDetailPressed:(id)sender
{
	//[baseVC expandCell:sender detailType:@"Up" rowNumber:currentRow];	
}

- (IBAction)buttonDrillPressed:(id)sender IdKey:(NSString *)idKey
{
	//[baseVC switchViews:sender ParameterBag:nil];	
	//[baseVC :<#(id)sender#>
}

- (void)drillToSegment:(NSString *)idKey
{
	//[baseVC switchViews:sender ParameterBag:nil];	
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[label release];
	[labelDateRange release];
	[labelLocator release];
	[btnDetail release];
	[baseVC release];
	[rootVC release];
	//[currentRow release];
	[tripBits release]; 
	[keys release];
	[tableList release];
	[tripKey release];
    [super dealloc];
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [tripBits count];
    
}

- (NSInteger)tableView:(UITableView *)tableView 
 numberOfRowsInSection:(NSInteger)section
{
    if ([tripBits count] == 0)
        return 0;
    
    NSString *key = [keys objectAtIndex:section];
	for (NSString *segName in tripBits)
	{
		////NSLog(@"tripBits Key = %@", segName);
	}
	////NSLog(@"Key=%@", key);
    NSMutableArray *nameSection = [tripBits objectForKey:key]; //keys are not being updated... damn.
    return [nameSection count];
	//return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView 
         cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    
    NSString *key = [keys objectAtIndex:section]; //here is the bug... Trips List->Segment List->Segment Details->back->back->back->TripsList->Segment Details and now we are a zombie
    NSArray *segments = [tripBits objectForKey:key];
	////NSLog(@"TripDetailCellBig::cellForRowAtIndexPath:Number of segments for key %@ = %d",key, [segments count]);
	
	TripDetailCell *cell = (TripDetailCell *)[tableView dequeueReusableCellWithIdentifier: @"TripDetailCell"];
	if (cell == nil)  
	{
		NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TripDetailCell" owner:self options:nil];
		for (id oneObject in nib)
			if ([oneObject isKindOfClass:[TripDetailCell class]])
				cell = (TripDetailCell *)oneObject;
	}
	
	cell.rootCell = self;
	cell.rootVC = self.rootVC;
	
	//NSString *header = [rowDict objectForKey:@"Header"]; 
	
	if (row == 0) //header != @"")
	{
		[cell setAccessoryType:UITableViewCellAccessoryNone];
		
		cell.rootCell = self;
		[cell.labelHead setHidden:NO];
		cell.labelHead.text = key; 
		
		[cell.imgHead setHidden:YES];
		[cell.labelActionTime setHidden:YES];
		[cell.labelVendor setHidden:YES];
		[cell.labelFromToLocation setHidden:YES];
		[cell.labelDetails setHidden:YES];
		[cell.labelVendorDetail setHidden:YES];
		[cell.btnAction setHidden:YES];
		[cell.imgVendor setHidden:YES];
		[cell.btnDrill setHidden:YES];
		[cell.txtView setHidden:YES];
	}
	else 
	{
		SegmentData *segment = [segments objectAtIndex:row];
//		//NSLog(@"segemnt.type = %@", segment.type);
//		//NSLog(@"segemnt.startDateLocal = %@", segment.startDateLocal);
//		//NSLog(@"segemnt.endDateLocal = %@", segment.endDateLocal);
//		//NSLog(@"segemnt.cliqbookId = %@", segment.cliqbookId);
//		//NSLog(@"segemnt.legId = %@", segment.legId);
//		//NSLog(@"segemnt.confirmationNumber = %@", segment.confirmationNumber);
//		//NSLog(@"segemnt.flightNumber = %@", segment.flightNumber);
//		//NSLog(@"segemnt.duration = %@", segment.duration);
//		//NSLog(@"segemnt.startCityCode = %@", segment.startCityCode);
//		//NSLog(@"segemnt.endCityCode = %@", segment.endCityCode);
//		//NSLog(@"segemnt.startTerminal = %@", segment.startTerminal);
//		//NSLog(@"segemnt.endTerminal = %@", segment.endTerminal);
//		//NSLog(@"segemnt.startGate = %@", segment.startGate);
//		//NSLog(@"segemnt.endGate = %@", segment.endGate);
//		//NSLog(@"segemnt.vendor = %@", segment.vendor);
//		//NSLog(@"segemnt.idKey = %@", segment.idKey);
//		//NSLog(@"segemnt.bookingKey = %@", segment.bookingKey);
//		//NSLog(@"segemnt.operatedBy = %@", segment.operatedBy);
//		//NSLog(@"segemnt.operatedByFlightNumber = %@", segment.operatedByFlightNumber);
//		//NSLog(@"segemnt.status = %@", segment.status);
//		//NSLog(@"segemnt.numStops = %@", segment.numStops);		
		
		[cell.labelHead setHidden:YES]; //hide the date header
		[cell.imgHead setHidden:NO];
		segment.idKey = segment.cliqbookId;
		[cell setIdKey:segment.cliqbookId];
		[cell setSegmentType:segment.type];
		[cell setBookingKey:segment.bookingKey];
		
		//NSLog(@"1 - cell.segment count = %d", [cell.segment retainCount]);
		if (cell.segment != nil)
		{
			[cell.segment release];
		}
		//NSLog(@"2 - cell.segment count = %d", [cell.segment retainCount]);
		
		//NSLog(@"1 - segment count = %d, %ld", [segment retainCount], segment);
		[cell setSegment: segment];
		//NSLog(@"2 - segment count = %d, %ld", [segment retainCount], segment);
		//NSLog(@"3 - cell.segment count = %d", [cell.segment retainCount]);
		[cell setTripKey:tripKey];
		
		NSString *segmentType = segment.type;
		NSString *imageHeader = @"";//[[NSString alloc] init];
		
		if ([segmentType isEqualToString:SEG_TYPE_AIR])
		{
			imageHeader = @"24 flights.png";
		}
		else if ([segmentType isEqualToString:SEG_TYPE_CAR])
		{
			imageHeader = @"24 rental-car.png";
		}
		else if ([segmentType isEqualToString:SEG_TYPE_HOTEL])
		{
			imageHeader = @"24 hotel.png";
		}
		else if ([segmentType isEqualToString:SEG_TYPE_DINING])
		{
			imageHeader = @"24 dining.png";
		}
		else if ([segmentType isEqualToString:SEG_TYPE_RIDE])
		{
			imageHeader = @"24 taxi.png";
		}
		else if ([segmentType isEqualToString:SEG_TYPE_EVENT])
		{
			imageHeader = @"24_event.png";
		}
		else if ([segmentType isEqualToString:SEG_TYPE_RAIL])
		{
			imageHeader = @"24_rail.png";
		}
		else if ([segmentType isEqualToString:SEG_TYPE_PARKING])
		{
			imageHeader = @"24_parking.png";
		}
		else 
		{
			imageHeader = @"24 weather.png";
		}

		UIImage *gotImg = [UIImage imageNamed:imageHeader];
		[cell.imgHead setImage:gotImg];
		[imageHeader release];
		
		[cell.labelActionTime setHidden:NO];
		[cell.labelVendor setHidden:NO];
		[cell.labelFromToLocation setHidden:NO];
		[cell.labelDetails setHidden:NO];
		[cell.labelVendorDetail setHidden:YES];
		[cell.btnAction setHidden:YES];
		[cell.imgVendor setHidden:YES];
		[cell.btnDrill setHidden:NO];
		
		////NSLog(@"Type=%@; Leg=%@; Vendor=%@; Terminal=%@; Confirmation=%@;", segment.type, segment.legId, segment.vendor, segment.startTerminal, segment.confirmationNumber);
		
		NSString *actionVerb = @"";
		NSMutableString *actionText; // = [[NSMutableString alloc] init];
		////NSLog(@"TripDetailCellBig: actionVerbb retaincount1=%d", [actionVerb retainCount]);
		NSString *location = segment.startCityCode;
		NSString *vendor = [NSString stringWithFormat:@"%@ Confirm# %@", segment.vendor, segment.confirmationNumber];
		NSString *details = [NSString stringWithFormat:@"%@", @"1 (123) 456-7890"];
		
		//NSString *start = segment.startDateLocal;
		//NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
		//[dateFormat setDateFormat: @"HH:mm aa zzz"];
		//NSDate *startDate = [NSDate dateWithNaturalLanguageString:start locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
		NSString *startFormatted = [DateTimeFormatter formatTimeHHmmaazzz:segment.startDateLocal];// [dateFormat stringFromDate:startDate];
		//[dateFormat release];
		
		[cell.txtView setHidden:YES];
		
		if ([segment.type isEqualToString:SEG_TYPE_AIR])
		{
			actionVerb = [Localizer getLocalizedText:@"Departing"]; //line 1
			actionText = [NSString stringWithFormat:@"%@ %@ at %@", actionVerb, segment.startCityCode, startFormatted];
			vendor = [NSString stringWithFormat:@"on %@ %@ to %@", segment.vendor, segment.flightNumber, segment.endCityCode]; //line 2
			location = [NSString stringWithFormat:@"from Terminal %@ Gate %@", segment.startTerminal, segment.startGate]; //line 3			
			details = [NSString stringWithFormat:@"Confirmation #: %@", segment.confirmationNumber]; //line 4
//			[cell.txtView setHidden:NO];
//			cell.txtView.font = [UIFont fontWithName:@"Arial" size:12.0f]; // fontWithName:@"Arial" ofSize:10.0f];
//			cell.txtView.text = [NSString stringWithFormat:@"%@\n%@\n%@", vendor, location, details];
			
			//[cell.labelActionTime setHidden:YES];
//			[cell.labelVendor setHidden:YES];
//			[cell.labelFromToLocation setHidden:YES];
//			[cell.labelDetails setHidden:YES];
			
		}
		else if ([segment.type isEqualToString:SEG_TYPE_CAR])
		{
			actionText = [NSString stringWithFormat:@"%@ reserved at %@", segment.vendor, startFormatted];
			vendor = [NSString stringWithFormat:@"from %@", segment.startCityCode];//line 2
			location = segment.phoneNumber; //line 3
			details = [NSString stringWithFormat:@"Confirmation #: %@", segment.confirmationNumber]; //line 4
		}
		else if ([segment.type isEqualToString:SEG_TYPE_RIDE])
		{
			//actionVerb = [Localizer getLocalizedText:@"SLV_PICKUP_BY"];
			actionText = [NSString stringWithFormat:@"%@ at %@", segment.vendorName, startFormatted];		
			vendor = [NSString stringWithFormat:@"%@, %@, %@", segment.startAddress, segment.startCity, segment.startState];//line 2
			location = segment.pickupInstructions; //line 3
			details = [NSString stringWithFormat:@"%@", segment.meetingInstructions]; //line 4
		}
		else if ([segment.type isEqualToString:SEG_TYPE_HOTEL])
		{
			actionText = [NSString stringWithFormat:@"Check in to %@ %@", segment.vendor, segment.hotelName];
			vendor = [NSString stringWithFormat:@"%@", segment.startAddress];//line 2
			location = [NSString stringWithFormat:@"%@, %@ %@", segment.startCity, segment.startState, segment.startPostalCode]; //line 3
			details = [NSString stringWithFormat:@"Confirmation #: %@", segment.confirmationNumber]; //line 4
		}
		else if ([segment.type isEqualToString:SEG_TYPE_RAIL])
		{
			actionVerb =  [Localizer getLocalizedText:@"Departing"]; //line 1
			actionText = [NSString stringWithFormat:@"%@ %@ at %@", actionVerb, segment.startRailStation, startFormatted];
			vendor = [NSString stringWithFormat:@"on %@ %@ to %@", segment.vendor, segment.trainNumber, segment.endCityCode]; //line 2
			location = [NSString stringWithFormat:@"from Platform %@ Wagon #:%@", segment.startPlatform, segment.wagonNumber]; //line 3			
			details = [NSString stringWithFormat:@"Confirmation #: %@", segment.confirmationNumber]; //line 4
		}
		else if ([segment.type isEqualToString:SEG_TYPE_PARKING])
		{
			actionText = [NSString stringWithFormat:@"%@ drop off at %@", segment.vendor, startFormatted];
			vendor = [NSString stringWithFormat:@"%@", segment.startAddress];//line 2
			location = [NSString stringWithFormat:@"%@, %@ %@", segment.startCity, segment.startState, segment.startPostalCode]; //line 3
			details = [NSString stringWithFormat:@"Confirmation #: %@", segment.confirmationNumber]; //line 4
		}
		else if ([segment.type isEqualToString:SEG_TYPE_DINING] || [segment.type isEqualToString:SEG_TYPE_EVENT])
		{
			actionText = [NSString stringWithFormat:@"%@ %@", segment.vendorName, startFormatted];
			vendor = [NSString stringWithFormat:@"%@", segment.startAddress];//line 2
			location = [NSString stringWithFormat:@"%@, %@ %@", segment.startCity, segment.startState, segment.startPostalCode]; //line 3
			details = [NSString stringWithFormat:@"Confirmation #: %@", segment.confirmationNumber]; //line 4
		}
		else
		{
			actionText = [NSString stringWithFormat:@"%@", startFormatted];
		}
		
		cell.labelActionTime.text = actionText; 
		cell.labelVendor.text = vendor;
		cell.labelFromToLocation.text = location;
		cell.labelDetails.text = details;
		
		[cell.labelVendorDetail setHidden:YES];
		[cell.btnAction setHidden:YES];
		[cell.imgVendor setHidden:YES];
		
	}
	
    return cell;
}


#pragma mark -
#pragma mark Table View Delegate Methods
- (NSIndexPath *)tableView:(UITableView *)tableView 
  willSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    [tableView reloadData];
    return indexPath;
}

- (NSInteger)tableView:(UITableView *)tableView 
sectionForSectionIndexTitle:(NSString *)title 
               atIndex:(NSInteger)index
{
    NSString *key = [keys objectAtIndex:index];
    if (key == UITableViewIndexSearch)
    {
        [tableView setContentOffset:CGPointZero animated:NO];
        return NSNotFound;
    }
    else return index;
    
}

- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	NSUInteger row = [indexPath row];
	if (row == 0) 
	{
		return 30;
	}
	else {
		return 76;
	}
}


@end
