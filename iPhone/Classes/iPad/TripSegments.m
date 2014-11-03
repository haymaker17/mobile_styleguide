//
//  TripSegments.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TripSegments.h"
#import "Detail.h"
#import "ExSystem.h" 

#import "FormatUtils.h"
#import "TripDetailCell.h"
#import "ViewConstants.h"
#import "SegmentRow.h"

@implementation TripSegments

#define kHOTEL 0
#define kAIR 1
//NSString * const ITIN_DETAILS_VIEW = @"ITIN_DETAILS_VIEW";


-(NSMutableArray *) makeTripDetails:(NSString *)segmentType Segment:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	NSMutableArray *a;
	if([segmentType isEqualToString:SEG_TYPE_HOTEL])
		a = [self makeHotelDetails:segment RVC:rootViewController];
	else if([segmentType isEqualToString:SEG_TYPE_AIR])
		a = [self makeAirDetails:segment RVC:rootViewController];
	else if([segmentType isEqualToString:SEG_TYPE_CAR])
		a = [self makeCarDetails:segment RVC:rootViewController];
	else if([segmentType isEqualToString:SEG_TYPE_RIDE])
		a = [self makeRideDetails:segment RVC:rootViewController];
	else if([segmentType isEqualToString:SEG_TYPE_RAIL])
		a = [self makeRailDetails:segment RVC:rootViewController];
	else if([segmentType isEqualToString:SEG_TYPE_DINING])
		a = [self makeDiningDetails:segment RVC:rootViewController];
	else 
		return nil;
	
	return a;
}


-(UILabel *) makeDetailLabel:(NSString *) lblText YPos:(float)yPos XPos:(float)xPos
{
	__autoreleasing UILabel *lbl = [[UILabel alloc]initWithFrame:CGRectMake(xPos, yPos, 100, 20)];
	//[lbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
	lbl.font = [UIFont boldSystemFontOfSize:14];
	[lbl setLineBreakMode:NSLineBreakByTruncatingTail];
	[lbl setTextAlignment:NSTextAlignmentLeft];
	[lbl setText:lblText];
	[lbl setBackgroundColor:[UIColor clearColor]];
	[lbl setHighlightedTextColor:[UIColor whiteColor]];
	
	return lbl;
}


-(UIView *) makeDetailValueButton:(NSString *) lblText YPos:(float)yPos XPos:(float)xPos CellWidth:(float)cellW Det:(Detail *)detail TripCell:(TripDetailCell *)cell
{
	
	int w = 300; //(cellW - xPos + 20); // 210;
	
	CGFloat height =  [FormatUtils getTextFieldHeight:w Text:lblText FontSize:14.0f];
	
	
	////
	__autoreleasing UIView *v = [[UIView alloc] initWithFrame:CGRectMake(xPos, yPos, w, height)];
	UIButton *button = [UIButton buttonWithType:UIButtonTypeCustom];
	

	//set the frame of the button to the size of the image (see note below)
	button.frame = CGRectMake(0, 0, w, height);
	
	if(detail.url != nil)
	{
		if(cell.aDetails == nil)
			cell.aDetails = [[NSMutableArray alloc] initWithObjects:nil];
		[cell.aDetails addObject:detail];
		button.tag = [cell.aDetails count]-1;
		[button addTarget:cell action:@selector(loadWebView:) forControlEvents:UIControlEventTouchUpInside];
	}
	else if(detail.mapAddress != nil)
	{
		
		if(cell.aDetails == nil)
			cell.aDetails = [[NSMutableArray alloc] initWithObjects:nil];
		[cell.aDetails addObject:detail];
		button.tag = [cell.aDetails count]-1;
		[button addTarget:cell action:@selector(goSomeplace:) forControlEvents:UIControlEventTouchUpInside];
	}
	
	UILabel *lbl = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, w, 20)];
	
	lbl.font = [UIFont systemFontOfSize:14];
	lbl.textColor = [UIColor blueColor]; // colorWithRed:222/255.0 green:137/255.0 blue:145/255.0 alpha:1.0f];
	lbl.backgroundColor = [UIColor clearColor];
	lbl.textAlignment = NSTextAlignmentLeft;
	lbl.text = lblText;
	
	[v addSubview:button];
	[v addSubview:lbl];

	return v;
}

-(UILabel *) makeDetailValue:(NSString *) lblText YPos:(float)yPos XPos:(float)xPos CellWidth:(float)cellW
{
	int w = 300; //(cellW - xPos + 20); // 210;

	CGFloat height =  [FormatUtils getTextFieldHeight:w Text:lblText FontSize:14.0f];
	__autoreleasing UILabel *lbl = [[UILabel alloc]initWithFrame:CGRectMake(xPos, yPos, w, height)];
	lbl.font = [UIFont systemFontOfSize:14];
	[lbl setLineBreakMode:NSLineBreakByWordWrapping];
	[lbl setTextAlignment:NSTextAlignmentLeft];
	[lbl setText:lblText];
	[lbl setNumberOfLines:3];
	[lbl setHighlightedTextColor:[UIColor whiteColor]];
	[lbl setBackgroundColor:[UIColor clearColor]];
	
	return lbl;
}


#pragma mark -
#pragma mark Hotel 
-(NSMutableArray *) makeHotelDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	__autoreleasing NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
	
	Detail *detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_IN"];
	detail.val = [DateTimeFormatter formatDateForTravel:segment.relStartLocation.dateLocal];
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_OUT"];
	detail.val = [DateTimeFormatter formatDateForTravel:segment.relEndLocation.dateLocal];
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
	detail.val = segment.status;
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
	detail.val = segment.confirmationNumber;
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DAILY_RATE"];
	detail.val = [FormatUtils formatMoneyWithNumber:segment.dailyRate crnCode:segment.currency]; 
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TOTAL_RATE"];
	detail.val = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency];
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CANCEL_POLICY"];
	detail.val = segment.cancellationPolicy;
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ROOM"];
	detail.val = segment.roomDescription;
	[a addObject:detail];
	
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"Vendor"];
	detail.val = segment.vendorName;
	detail.code = segment.vendor;
	//		UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynch:segment.vendor VendorType:@"h" RespondToCell:cell];
	//		if (gotImg != nil) 
	//		{
	//			[cell.imgView setImage:gotImg];
	//		}
	[a addObject:detail];
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"Property"];
	detail.val = segment.segmentName;
	detail.code = segment.vendor;
	//	CGRect myImageRect = CGRectMake(10.0f, 10.0f, 24.0f, 24.0f);
	//	UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
	//	[imgBack setImage:[self getSegmentTypeIconImage:segment.type]];
	//	[cell.contentView addSubview:imgBack];
	//	[imgBack release];
	[a addObject:detail];
	
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"Phone"];
	detail.val = segment.phoneNumber;
	detail.code = segment.phoneNumber;
	detail.actionType = @"PHONE";
	[a addObject:detail];
	
	
	detail = [[Detail alloc] init];
	detail.lbl = [Localizer getLocalizedText:@"Address"];
	detail.actionType = @"ADDRESS";
	NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
    // @"%@\n%@, %@, %@"
	
	detail.val = location;
	detail.code = location;
	detail.codeName = segment.vendorName;
	NSString *mapAddress = [SegmentData getMapAddress:segment.relStartLocation withLineBreaker:NO withDelimitor:YES];
    //[NSString stringWithFormat:@"%@, %@, %@ %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];
	detail.mapAddress = mapAddress;
	detail.imgDetail = [UIImage imageNamed:@"action_map"];
	[a addObject:detail];
	
	
	return a;
}


-(float) getHotelCellH:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	NSMutableArray *a = [self makeTripDetails:SEG_TYPE_HOTEL Segment:segment RVC:rootViewController];
	float h = ( [a count] * 21 ) + 50;
	return h;
}


#define kImgW 250
#define kImgH 250
-(void) fillHotelCell:(TripDetailCell *)cell Segment:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	float y = 33;
	float x = 260;
	NSMutableArray *a = [self makeTripDetails:SEG_TYPE_HOTEL Segment:segment RVC:rootViewController];
	float cellW = cell.contentView.frame.size.width;
	
//	for(int i = 0; i < [a count]; i ++)
//	{
//		Detail *d = [a objectAtIndex:i];
//		[cell.contentView addSubview:[self makeDetailLabel:d.lbl YPos:y XPos:x]];
//		UILabel *lblVal = [self makeDetailValue:d.val YPos:y XPos:x + 105 CellWidth:cellW];
//		[cell.contentView addSubview:lblVal];
//		y = y + lblVal.frame.size.height;
//	}
	
	for(int i = 0; i < [a count]; i ++)
	{
		Detail *d = a[i];
		[cell.contentView addSubview:[self makeDetailLabel:d.lbl YPos:y XPos:x]];
		
		if(d.url != nil)
		{
			UIView *btnSomething = [self makeDetailValueButton:d.val YPos:y XPos:x + 105 CellWidth:cellW Det:d TripCell:(TripDetailCell *)cell];
			
			[cell.contentView addSubview:btnSomething];
			y = y + btnSomething.frame.size.height;
		}
		else if(d.mapAddress != nil)
		{
			UIView *btnSomething = [self makeDetailValueButton:d.val YPos:y XPos:x + 105 CellWidth:cellW Det:d TripCell:(TripDetailCell *)cell];

			[cell.contentView addSubview:btnSomething];
			y = y + btnSomething.frame.size.height;
		}
		else 
		{
			UILabel *lblVal = [self makeDetailValue:d.val YPos:y XPos:x + 105 CellWidth:cellW];
			[cell.contentView addSubview:lblVal];
			y = y + lblVal.frame.size.height;
		}
	}
	
	UIToolbar *tb = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, cell.contentView.frame.size.width, 32)];
	tb.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	tb.tintColor = [UIColor brownColor];
	[cell.contentView addSubview:tb];
	[cell.contentView bringSubviewToFront:cell.imgHead];
	
	UILabel *lbl = [[UILabel alloc]initWithFrame:CGRectMake(40, 5, 400, 24)] ;
	[lbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
	lbl.font = [UIFont boldSystemFontOfSize:20];
	[lbl setLineBreakMode:NSLineBreakByTruncatingTail];
	[lbl setTextAlignment:NSTextAlignmentLeft];
	[lbl setText:[NSString stringWithFormat:@"%@ - %@", segment.vendorName, segment.segmentName]];
	
	lbl.textColor = [UIColor whiteColor]; // colorWithRed:222/255.0 green:137/255.0 blue:145/255.0 alpha:1.0f];
	lbl.shadowColor = [UIColor lightGrayColor];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	
	[cell.contentView addSubview:lbl];
	

	UIScrollView *sv = [[UIScrollView alloc] initWithFrame:CGRectMake(5, 33, kImgW, kImgH)];
	sv.contentSize = CGSizeMake(4 * kImgW, kImgH);
	sv.pagingEnabled = YES;
	sv.bounces = YES;
	sv.indicatorStyle = UIScrollViewIndicatorStyleBlack;
	//sv.delegate = cell;
	
	UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, kImgW, kImgH)];
	[iv setImage:[UIImage imageNamed:@"02.png"]];
	[sv addSubview:iv]; //[cell.contentView addSubview:iv];
	
	int iPos = 1;
	iv = [[UIImageView alloc] initWithFrame:CGRectMake(iPos * kImgW, 0, kImgW, kImgH)];
	[iv setImage:[UIImage imageNamed:@"01.png"]];
	[sv addSubview:iv]; //[cell.contentView addSubview:iv];
	iPos ++;
	
	iv = [[UIImageView alloc] initWithFrame:CGRectMake(iPos * kImgW, 0, kImgW, kImgH)];
	[iv setImage:[UIImage imageNamed:@"03.png"]];
	[sv addSubview:iv]; //[cell.contentView addSubview:iv];
	iPos ++;
	
	iv = [[UIImageView alloc] initWithFrame:CGRectMake(iPos * kImgW, 0, kImgW, kImgH)];
	[iv setImage:[UIImage imageNamed:@"04.png"]];
	[sv addSubview:iv]; //[cell.contentView addSubview:iv];
	
	[cell.contentView addSubview:sv];

}


#pragma mark -
#pragma mark Air
-(NSMutableArray *) makeAirDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	__autoreleasing NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
//	segRow.rowLabel = nil;
//	segRow.rowValue = nil;
//	segRow.isAirVendor = YES;
//	[section addObject:segRow];
//	[segRow release];
//	
//	segRow = [[SegmentRow alloc] init];
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"AIR";
	segRow.segment = segment;
	[section addObject:segRow];
	
	//Short statuses (useful):
	//Cancelled
	//Delayed
	//Early
	//No status
	//On time
	//
	//Long statuses (a mix of useful and confusing so we’ll steer clear):
	//No delays posted
	//Cancelled flight
	//Cancelled
	//Check with airline
	//No delays posted
	//Estimated time of arrival
	//Estimated time of departure
	//Arrived in the gate
	//Cancelled leg
	//Not Available
	//No-op
	//Departed off the ground
	//Arrived on the ground
	//Departed out of the gate
	//Preliminary estimated time
	//Schedule Change
	//
	//And for completeness, reason:
	//Aviation System Delay
	//Weather Delay
	//Airline Controlled Delay
	//Downline Delay
	//Other
	//None
	if([TripData isFlightDelayedOrCancelled: segment.relFlightStats])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = @"Flight Status";
		segRow.rowValue = segment.relFlightStats.departureShortStatus;
		segRow.isEmblazoned = YES;
		[section addObject:segRow];
	}
	else if(segment.relFlightStats.departureShortStatus != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = @"Flight Status";
		segRow.rowValue = segment.relFlightStats.departureShortStatus;
		[section addObject:segRow];
	}
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = nil;
		segRow.rowValue = segment.phoneNumber; // @"425-497-5946";
		segRow.isPhone = YES;
		[section addObject:segRow];
	}
	
	//class and seat
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = nil;
	NSString *classy = nil;
	NSString *seatNum = nil;
	if(segment.classOfServiceLocalized == nil)
		classy = @"";
	else 
		classy = [NSString stringWithFormat:@"Class: %@", segment.classOfServiceLocalized];
	
	if(segment.seatNumber == nil)
		seatNum = @"";
	else 
		seatNum = [NSString stringWithFormat:@"Seat: %@", segment.seatNumber];
	segRow.rowValue = [NSString stringWithFormat:@"%@ %@", classy, seatNum];
	segRow.isWeb = YES;
	segRow.isSeat = YES;
	segRow.url = @"mobile.seatguru.com/American_Airlines/Boeing_767-300_B/";
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Seat Map for t"], segment.aircraftCode];
	[section addObject:segRow];
	
	
	//confirmation number
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
	segRow.rowValue = segment.confirmationNumber;
	[section addObject:segRow];
	
	//new section
	if(segment.relFlightStats.departureScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Departure"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureScheduled];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Departure"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureEstimated];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Departure"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureActual];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureStatusReason != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Departure Status"];
		segRow.rowValue = segment.relFlightStats.departureStatusReason;
		if ([segment.relFlightStats.departureShortStatus isEqualToString:@"DY"] || [segment.relFlightStats.departureShortStatus isEqualToString:@"Delayed"]) 
			segRow.isEmblazoned = YES;
		[section addObject:segRow];
	}
	
	//flight duration
	if(segment.duration != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Duration"]; 
		int flightMinutes = [segment.duration intValue];
		int flightHours = flightMinutes / 60;
		
		if (flightHours > 0) 
			flightMinutes = flightMinutes - (flightHours * 60);
		
		NSString *dur = [NSString stringWithFormat:@"%d Hours and %d Minute(s)", flightHours, flightMinutes];
		
		if(flightHours < 1)
			dur = [NSString stringWithFormat:@"%d Minute(s)", flightMinutes];
		else if (flightHours == 1)
			[NSString stringWithFormat:@"%d Hour and %d Minute(s)", flightHours, flightMinutes];
		
		segRow.rowValue = dur;
		[section addObject:segRow];
	}
	
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRPORT"];
	segRow.rowValue = [SegmentData getAirportFullName:segment.relStartLocation];
    //[NSString stringWithFormat:@"(%@) %@, %@", segment.relStartLocation.cityCode, segment.startAirportName, segment.startAirportState];
	segRow.isWeb = YES;
	segRow.isAirport = YES;
	segRow.iataCode = segment.relStartLocation.cityCode;
	segRow.url = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relStartLocation.cityCode];
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal Map for t"], segment.relStartLocation.cityCode];
	[section addObject:segRow];
		
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Status"];
		segRow.rowValue = segment.status;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.equipmentScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentScheduled;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.equipmentActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentActual;
		[section addObject:segRow];
	}
	
	if(segment.meals != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEAL"];
		segRow.rowValue = segment.meals;
		[section addObject:segRow];
	}
	
	if(segment.specialInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SPECIAL_INSTRUCTIONS"];
		segRow.rowValue = segment.specialInstructions;
		[section addObject:segRow];
	}
	
	//airline web site
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = nil;
	segRow.rowValue = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
	segRow.url = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
	segRow.viewTitle = segment.vendorName;
	segRow.isWeb = YES;
	[section addObject:segRow];
	
	//arrival date time
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
	segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
	[section addObject:segRow];
	
	if(segment.relFlightStats.arrivalEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalEstimated];
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.arrivalActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalActual];
		[section addObject:segRow];
	}
	
	//arrival airport
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
	NSString *airportURL = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relEndLocation.cityCode];
	segRow.url = airportURL; //@"www.airportterminalmaps.com/SEATAC-airport-terminal-map.html";
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal Map for t"], segment.relEndLocation.cityCode];
	segRow.rowValue = [SegmentData getAirportFullName:segment.relEndLocation];
    //[NSString stringWithFormat:@"(%@) %@, %@", segment.endCityCode, segment.endAirportName, segment.endAirportState];
	if (airportURL == nil || [airportURL length] <= 0) 
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRPORT"]; 
	segRow.isWeb = YES;
	segRow.isAirport = YES;
	segRow.iataCode = segment.relEndLocation.cityCode;
	[section addObject:segRow];
	
	//arrival terminal gate
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [NSString stringWithFormat:@"%@\n%@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TERMINAL"], 
					   [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_GATE"]];
    NSMutableString *term = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    
    [SegmentData getArriveTermGate:segment terminal:term gate:gate];
    
	segRow.rowValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
	[section addObject:segRow];
	
	if(segment.relFlightStats.baggageClaim != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Baggage Claim"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = segment.relFlightStats.baggageClaim;
		[section addObject:segRow];
	}
	
	return section;
	
}



-(float) getAirCellH:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	NSMutableArray *a = [self makeTripDetails:SEG_TYPE_AIR Segment:segment RVC:rootViewController];
	float h = ( [a count] * 21 ) + 60;
	return h;
}


-(void) fillAirCell:(TripDetailCell *)cell Segment:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	float y = 33;
	float x = 20;
	NSMutableArray *a = [self makeTripDetails:SEG_TYPE_AIR Segment:segment RVC:rootViewController];
	float cellW = cell.contentView.frame.size.width;
	
	for(int i = 0; i < [a count]; i ++)
	{
		Detail *d = a[i];
		[cell.contentView addSubview:[self makeDetailLabel:d.lbl YPos:y XPos:x]];
		if(d.url != nil)
		{
			UIView *btnSomething = [self makeDetailValueButton:d.val YPos:y XPos:x + 105 CellWidth:cellW Det:d TripCell:(TripDetailCell *)cell];
			
			[cell.contentView addSubview:btnSomething];
			y = y + btnSomething.frame.size.height;
		}
		else 
		{
			UILabel *lblVal = [self makeDetailValue:d.val YPos:y XPos:x + 105 CellWidth:cellW];
			[cell.contentView addSubview:lblVal];
			y = y + lblVal.frame.size.height;
		}
	}
	
	NSString *startFormatted;
	if(segment.relStartLocation.dateLocal != nil)
	{
		startFormatted = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
	}
	else 
	{
		startFormatted = @"";
	}
	
	NSString *vendorName;
	if (segment.vendorName != nil)
	{
		vendorName = segment.vendorName;
	}
	else 
	{
		vendorName = segment.vendor;
	}
	
	NSString *vendor = [NSString stringWithFormat:@"%@ %@ %@", vendorName, segment.flightNumber, startFormatted]; //line 2
	
	UIToolbar *tb = [[UIToolbar alloc] initWithFrame:CGRectMake(0, 0, cell.contentView.frame.size.width, 32)];
	tb.autoresizingMask = UIViewAutoresizingFlexibleWidth;
	tb.tintColor = [UIColor purpleColor];
	[cell.contentView addSubview:tb];
	[cell.contentView bringSubviewToFront:cell.imgHead];
	
	UILabel *lbl = [[UILabel alloc]initWithFrame:CGRectMake(40, 5, 400, 24)] ;
	[lbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
	lbl.font = [UIFont boldSystemFontOfSize:20];
	[lbl setLineBreakMode:NSLineBreakByTruncatingTail];
	[lbl setTextAlignment:NSTextAlignmentLeft];
	[lbl setText:[NSString stringWithFormat:@"%@", vendor]];
	
	lbl.textColor = [UIColor whiteColor]; // colorWithRed:222/255.0 green:137/255.0 blue:145/255.0 alpha:1.0f];
	lbl.shadowColor = [UIColor lightGrayColor];
	lbl.shadowOffset = CGSizeMake(0, -1);
	lbl.backgroundColor = [UIColor clearColor];
	
	[cell.contentView addSubview:lbl];
	
//	UILabel *lbl = [[UILabel alloc]initWithFrame:CGRectMake(40, 5, 400, 20)] ;
//	[lbl setAutoresizingMask:UIViewAutoresizingFlexibleWidth];
//	lbl.font = [UIFont boldSystemFontOfSize:20];
//	[lbl setLineBreakMode:NSLineBreakByTruncatingTail];
//	[lbl setTextAlignment:NSTextAlignmentLeft];
//	[lbl setText:[NSString stringWithFormat:@"%@", vendor]];
//	[lbl setBackgroundColor:[UIColor clearColor]];
//	
//	[cell.contentView addSubview:lbl];
//	[lbl release];
}

#pragma mark -
#pragma mark Car Methods
-(NSMutableArray *) makeCarDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	__autoreleasing NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
	
			
				Detail *detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"Pickup"];
				detail.val = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"Return"];
				detail.val = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
				detail.val = segment.confirmationNumber;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
				detail.val = segment.status;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE"];
				detail.val = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DAILY_RATE"];
				detail.val = [FormatUtils formatMoneyWithNumber:segment.dailyRate crnCode:segment.currency];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE_TYPE"];
				detail.val = segment.rateType;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_BODY"];
				detail.val = segment.bodyTypeName;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TRANSMISSION"];
				detail.val = segment.transmission;
				[a addObject:detail];
				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRCOND"];
				detail.val = segment.airCond;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DISCOUNT"];
				detail.val =  segment.discountCode;
				[a addObject:detail];
				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_EQUIP"];
				detail.val = segment.specialEquipment;
				[a addObject:detail];

//				cell.labelLabel.text = @"";
//				cell.labelValue.text = @"";
//				cell.labelVendor.text = segment.vendorName;
//				cell.labelVendor.frame = CGRectMake(40, 0, cell.labelVendor.frame.size.width, 44);
//				
//				UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynch:segment.vendor VendorType:@"c" RespondToCell:cell];
//				if (gotImg != nil) 
//				{
//					[cell.imgView setImage:gotImg];
//				}
//				
//				CGRect myImageRect = CGRectMake(10.0f, 10.0f, 24.0f, 24.0f);
//				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
//				[imgBack setImage:[self getSegmentTypeIconImage:segment.type]];
//				[cell.contentView addSubview:imgBack];
//				[imgBack release];
				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_IN"];
				detail.val = segment.vendorName;
				[a addObject:detail];

//				[cell.labelLabel setHidden:YES];
//				cell.labelValue.text = [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
//				CGRect myImageRect = CGRectMake(10.0f, 10.0f, 24.0f, 24.0f);
//				UIImageView *imgBack = [[UIImageView alloc] initWithFrame:myImageRect];
//				[imgBack setImage:[UIImage imageNamed:@"www_or_link_24X24.png"]];
//				[cell.contentView addSubview:imgBack];
//				[imgBack release];
//				[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_IN"];
				detail.val =  [[ExSystem sharedInstance] getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
				[a addObject:detail];
				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_IN"];
				detail.val = [NSString stringWithFormat:@"(%@) %@\n%@, %@", segment.relStartLocation.cityCode, segment.relStartLocation.airportName, segment.relStartLocation.airportCity, segment.relStartLocation.airportState];
				[a addObject:detail];
	return a;
}


#pragma mark -
#pragma mark Ride Methods
-(NSMutableArray *) makeRideDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	__autoreleasing NSMutableArray *a = [[NSMutableArray alloc] initWithObjects:nil];
		
		Detail *detail = [[Detail alloc] init];
		detail.lbl = [Localizer getLocalizedText:@"Vendor"];
		detail.val =  segment.vendorName;
		[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"Pickup"];
				detail.val = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_ADDRESS"];
    detail.val = [SegmentData getMapAddress:segment.relStartLocation];
    //[NSString stringWithFormat:@"%@\n%@ %@, %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_INSTRUCTIONS"];
				detail.val =  segment.pickupInstructions;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEETING_INSTRUCTIONS"];
				detail.val =  segment.meetingInstructions;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF"];
				detail.val =  [DateTimeFormatter formatDateTimeFull:segment.relEndLocation.dateLocal];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF_ADDRESS"];
    detail.val = [SegmentData getMapAddress:segment.relEndLocation];
    // [NSString stringWithFormat:@"%@\n%@ %@, %@", segment.endAddress, segment.endCity, segment.endState, segment.endPostalCode];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF_INSTRUCTIONS"];
				detail.val =  segment.dropoffInstructions;
				[a addObject:detail];
	
				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
				detail.val =  segment.status;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
				detail.val = segment.confirmationNumber;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE"];
				detail.val =  [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE_DESCRIPTION"];
				detail.val =  segment.rateDescription;
				[a addObject:detail];

				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CANCEL_POLICY"];
				detail.val =  segment.cancellationPolicy;
				[a addObject:detail];

//				cell.labelLabel.text = @"";
//				cell.labelValue.text = @"";
//				cell.labelVendor.text = segment.vendorName;
//				////NSLog(@"vendor=%@", segment.vendor);
//				[[ExSystem sharedInstance].imageControl getVendorImageAsynch:segment.vendor VendorType:@"l" RespondToCell:cell];
				
				detail = [[Detail alloc] init];
				detail.lbl = [Localizer getLocalizedText:@"Vendor"];
				detail.val =  segment.vendorName;
				[a addObject:detail];
	return a;
	
	}

#pragma mark -
#pragma mark Rail Section
-(NSMutableArray *) makeRailDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
{
	__autoreleasing NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	
	if(segment.vendor != nil)
	{
		segRow.rowLabel = nil;
		segRow.rowValue = segment.vendorName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.isVendorRow = YES;
		segRow.vendorType = @"a";
		[section addObject:segRow];
		segRow = [[SegmentRow alloc] init];
	}
	
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"RAIL";
	segRow.segment = segment;
	[section addObject:segRow];
	
	
	if(segment.relStartLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_ADDRESS"];
		
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
		
		segRow.rowValue = location; //[NSString stringWithFormat:@"%@\n%@, %@, %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];;
		segRow.isMap = YES;
		
		[section addObject:segRow];
	}
	
	if(segment.vendor != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Info"];
		if (segment.operatedByVendor != nil)
			segRow.rowValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_FLIGHT_WITH_OB"], segment.vendor, segment.trainNumber, [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal], segment.operatedByVendor, segment.operatedByTrainNumber];
		else 
			segRow.rowValue = [NSString stringWithFormat:@"%@ #%@ %@", segment.vendor, segment.trainNumber, [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal]];
		[section addObject:segRow];
	}
	
	if(segment.classOfService != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CLASS"];
		segRow.rowValue = segment.classOfService;
		[section addObject:segRow];
	}
	
	if(segment.cabin != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CABIN"];
		segRow.rowValue = segment.cabin;
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.cityCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CITY"];
		segRow.rowValue = segment.relStartLocation.cityCode;
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.platform != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PLATFORM"];
		segRow.rowValue = segment.relStartLocation.platform;
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.railStation != nil)
	{
		NSString *railStation = [SegmentData getRailStation: segment.relStartLocation];
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATION"];
		segRow.rowValue = railStation;
		[section addObject:segRow];
	}
	
	if(segment.trainNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Train Number"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CLASS"];
		segRow.rowValue = segment.trainNumber;
		[section addObject:segRow];
	}
	
	if(segment.wagonNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_WAGON_NUM"];
		segRow.rowValue = segment.wagonNumber;
		[section addObject:segRow];
	}
	
	if(segment.amenities != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AMENITIES"];
		segRow.rowValue = segment.amenities;
		[section addObject:segRow];
	}
	
	if(segment.duration != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DURATION"];
		segRow.rowValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MINUTES"], segment.duration];
		[section addObject:segRow];
	}
	
	if(segment.numStops != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STOPS"];
		segRow.rowValue = [NSString stringWithFormat:@"%@", segment.numStops];
		[section addObject:segRow];
	}
	
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		[section addObject:segRow];
	}
	
	if(segment.meals != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEAL"];
		segRow.rowValue = segment.meals;
		[section addObject:segRow];
	}
	
	if(segment.numPersons != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_NUMBER_OF_PERSONS"];
		segRow.rowValue = [NSString stringWithFormat:@"%@", segment.numPersons];
		[section addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TOTAL_RATE"];
		segRow.rowValue = [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
		[section addObject:segRow];
	}
	
	if(segment.specialInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SPECIAL_INSTRUCTIONS"];
		segRow.rowValue = segment.specialInstructions;
		[section addObject:segRow];
	}
	
	
	//arrival details
	//section dump and reset
//	[sections addObject:section];
//	[section release];
//	section = [[NSMutableArray alloc] initWithObjects:nil];
	if(segment.relStartLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.cityCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CITY"];
		segRow.rowValue = segment.relEndLocation.cityCode;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.railStation != nil)
	{
		NSString *endRailStation = [SegmentData getRailStation: segment.relEndLocation];
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATION"];
		segRow.rowValue = endRailStation;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.platform != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PLATFORM"];
		segRow.rowValue = segment.relEndLocation.platform;
		[section addObject:segRow];
	}
	
	
	//Carrier details
	//section dump and reset
//	[sections addObject:section];
//	[section release];
//	section = [[NSMutableArray alloc] initWithObjects:nil];
	if(segment.vendorName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Vendor"];
		segRow.rowValue = segment.vendorName;
		[section addObject:segRow];
	}
	
	if(segment.trainTypeCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TRAIN_TYPE"];
		segRow.rowValue = segment.trainTypeCode;
		[section addObject:segRow];
	}
	
	if(segment.numPersons != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONTACT"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		[section addObject:segRow];
	}
	
	
//	[sections addObject:section];
//	[section release];
	
	return section;
}


#pragma mark -
#pragma mark Dining Section
-(NSMutableArray *) makeDiningDetails:(EntitySegment *)segment RVC:(RootViewController *)rootViewController
//-(void)fillDiningSections
{
	__autoreleasing NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = nil;
	
	if(segment.segmentName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = nil;
		segRow.rowValue = segment.segmentName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.isVendorRow = YES;
		segRow.vendorType = @"d";
		[section addObject:segRow];
	}
	
//	segRow.isSpecialCell = YES;
//	segRow.specialCellType = @"DINING";
//	segRow.segment = segment;
//	[section addObject:segRow];
//	[segRow release];
	
	
	if(segment.relStartLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Time"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
		[section addObject:segRow];
	}

	if(segment.relStartLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_ADDRESS"];
		
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
        
		segRow.rowValue = location; //[NSString stringWithFormat:@"%@\n%@, %@, %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];;
		segRow.isMap = YES;
		
		[section addObject:segRow];
	}
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		[section addObject:segRow];
	}
	
	if(segment.segmentName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Vendor"];
		segRow.rowValue = segment.segmentName;
		[section addObject:segRow];
	}
	
	if(segment.reservationId != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_REZ"];
		segRow.rowValue = segment.reservationId;
		[section addObject:segRow];
	}
	
	if(segment.numPersons != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_NUMBER_IN_PARTY"];
		segRow.rowValue = [NSString stringWithFormat:@"%@", segment.numPersons];
		[section addObject:segRow];
	}
	
	
	//[sections addObject:section];
	//[section release];
	
	return section;
	
	//	//Hotel Details
	//	//section dump and reset
	//	[sections addObject:section];
	//	[section release];
	//	section = [[NSMutableArray alloc] initWithObjects:nil];
}

-(NSString *)formatDuration:(int)duration
{
	if(duration < 59)
	{
		return [NSString stringWithFormat:@"%d minute(s)", duration];
	}
	else {
		int hours = duration / 60;
		int minutes = duration - (hours * 60);
		return [NSString stringWithFormat:@"%d hour(s) %d minute(s)", hours, minutes];
	}
}

@end
