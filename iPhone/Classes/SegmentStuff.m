//
//  SegmentStuff.m
//  ConcurMobile
//
//  Created by Paul Kramer on 10/4/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SegmentStuff.h"
#import "SegmentRow.h"
#import "SegmentData.h"
#import "FormatUtils.h"
#import "EntitySegment.h"
#import "Config.h"

@implementation SegmentStuff
@synthesize rootViewController;

-(NSMutableArray *) fillCarSections:(EntitySegment *)segment
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sections = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray	*sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sectionsPad = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	
	if(segment.vendor != nil)
	{
		segRow.rowLabel = nil;
		segRow.rowValue = segment.vendorName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.isVendorRow = YES;
		segRow.vendorType = @"c";
		segRow.segment = segment;
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
//		[sectionPad addObject:segRow];
	}
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	segRow = [[SegmentRow alloc] init];
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"CAR";
	segRow.segment = segment;
	[section addObject:segRow];
	[sectionPad addObject:segRow];
	
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
    if (segment.relStartLocation != nil && [UIDevice isPad])
    {
//        NSString *checkinDate = [DateTimeFormatter formatDateLong:segment.relStartLocation.dateLocal];
//        NSString *checkoutDate = [DateTimeFormatter formatDateLong:segment.relEndLocation.dateLocal];
//        
//        SegmentRow *segRow = [[SegmentRow alloc] init];
//        segRow.rowLabel = nil;
//        segRow.rowLabel = [NSString stringWithFormat:@"%@",[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_IN"]];
//        segRow.rowValue = checkinDate;
//        segRow.segment = segment;
//        [sectionPad addObject:segRow];
//        
//        segRow = [[SegmentRow alloc] init];
//        segRow.rowLabel = nil;
//        segRow.rowLabel = [NSString stringWithFormat:@"%@",[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_OUT"]];
//        segRow.rowValue = checkoutDate;
//        segRow.segment = segment;
//        [sectionPad addObject:segRow];

        NSString *pickUpDate = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
        NSString *pickupCity = segment.relStartLocation.city;
        NSString *pickupAirportCode = segment.relStartLocation.cityCode;
        NSString *pickup = @"";
        NSString *xtraCity = @"";
        
        if(pickupCity == nil)
            pickupCity = segment.relStartLocation.airportCity;
        
        if(pickupCity == nil)
            pickupCity = segment.relStartLocation.airportName;
        
        if(pickupAirportCode != nil)
        {
            pickup = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"Pickup"], pickUpDate];
            xtraCity = [NSString stringWithFormat:@"%@ (%@)", pickupCity, pickupAirportCode];
        }
        else
        {
            pickup = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"Pickup"], pickUpDate];
            xtraCity = [NSString stringWithFormat:@"%@", pickupCity];
        }

        SegmentRow *segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = nil;
        segRow.rowLabel = pickup;
        segRow.rowValue = xtraCity;
        segRow.isMap = YES;
        segRow.segment = segment;
        [sectionPad addObject:segRow];
    }

    if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
    if (segment.relEndLocation != nil && [UIDevice isPad])
    {
        NSString *pickUpDate = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
        NSString *pickupCity = segment.relEndLocation.city;
        NSString *pickupAirportCode = segment.relEndLocation.cityCode;
        NSString *pickup = @"";
        NSString *xtraCity = @"";
        
        if(pickupCity == nil)
            pickupCity = segment.relEndLocation.airportCity;

        if(pickupCity == nil)
            pickupCity = segment.relEndLocation.airportName;

        if(pickupAirportCode != nil)
        {
            pickup = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"Returning"], pickUpDate];
            xtraCity = [NSString stringWithFormat:@"%@ (%@)", pickupCity, pickupAirportCode];
        }
        else
        {
            pickup = [NSString stringWithFormat:@"%@: %@", [Localizer getLocalizedText:@"Returning"], pickUpDate];
            xtraCity = [NSString stringWithFormat:@"%@", pickupCity];
        }

        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = pickup;
        segRow.rowValue = xtraCity;
        segRow.isMap = YES;
        segRow.segment = segment;
        [sectionPad addObject:segRow];
    }
    
    if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
	//confirmation number
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
	segRow.rowValue = segment.confirmationNumber;
	segRow.segment = segment;
    segRow.isCopyEnable = YES;
	[section addObject:segRow];
	//[sectionPad addObject:segRow];
	
	
	//Car Details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	if(segment.status != nil)
	{
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		if(segment.statusLocalized != nil)
			segRow.rowValue = segment.statusLocalized;
		else 
			segRow.rowValue = segment.status;
		segRow.segment = segment;
		
		//NSLog(@"status code %@", segment.status);
		if([segment.status isEqualToString:@"HK"])
			segRow.color = [UIColor colorWithRed:0.0/255.0 green:102.0/255.0 blue:51.0/255.0 alpha:1.0f];
		
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency];
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.dailyRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DAILY_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.dailyRate crnCode:segment.currency]; 
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.rateType != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE_TYPE"];
		segRow.rowValue = segment.rateType;
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
    if ((segment.classOfCarLocalized != nil || segment.bodyTypeName != nil || segment.transmission != nil || segment.airCond != nil) && [UIDevice isPad])
    {
        segRow = [[SegmentRow alloc] init];
        NSMutableString *buildStr = [NSMutableString new];
        segRow.rowLabel = nil;
        if (segment.classOfCarLocalized != nil)
            buildStr = [NSMutableString stringWithString:segment.classOfCarLocalized];
        
        if (segment.bodyTypeName != nil)
            [buildStr appendFormat:@" / %@", segment.bodyTypeName];
        
        if (segment.transmission != nil)
            [buildStr appendFormat:@" / %@", segment.transmission];
        
        if (segment.airCond)
            [buildStr appendFormat:@" / %@", segment.airCond];
        
		segRow.rowValue = buildStr;
		segRow.segment = segment;
		[sectionPad addObject:segRow];
    }
	
//	if(segment.bodyTypeName != nil && [UIDevice isPad])
//	{
//		//segRow = [[SegmentRow alloc] init];
//		//segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_BODY"];
//		segRow.rowValue = segment.bodyTypeName;
//		segRow.segment = segment;
//		[section addObject:segRow];
//
//	}
//	
//	if(segment.transmission != nil && [UIDevice isPad])
//	{
//		segRow = [[SegmentRow alloc] init];
//		//segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TRANSMISSION"];
//		segRow.rowValue = segment.transmission;
//		segRow.segment = segment;
//		[section addObject:segRow];
//
//	}
//	
//	if(segment.airCond != nil && [UIDevice isPad])
//	{
//		segRow = [[SegmentRow alloc] init];
//		//segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRCOND"];
//		segRow.rowValue = segment.airCond;
//		segRow.segment = segment;
//		[section addObject:segRow];
//
//	}
    
    if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.numCars != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"# of Cars"];
		segRow.rowValue = [NSString stringWithFormat:@"%d", segment.numCars.intValue];
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}

	if(segment.discountCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DISCOUNT"];
		segRow.rowValue = segment.discountCode;
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.specialEquipment != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_EQUIP"];
		segRow.rowValue = segment.specialEquipment;
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}

    //MOB-12849 over lap of text in other cell
    if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
	if(segment.specialInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SPECIAL_INSTRUCTIONS"];
		segRow.rowValue = segment.specialInstructions;
		segRow.segment = segment;
        //MOB-12849 over lap of text in other cell
        segRow.showDisclosure = YES;
        segRow.isDescription = YES;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
	
	if([UIDevice isPad] && [ExSystem connectedToNetwork] && [segment.relTrip.allowCancel boolValue])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Cancel car reservation"];
		segRow.rowValue = nil;//[Localizer getLocalizedText:@"tap here to cancel reservation"]; //segment.roomDescription;
		segRow.segment = segment;
		segRow.isSpecialCell = NO;
		segRow.isCancel = YES;
		segRow.isEmblazoned = YES;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
		
		[sectionsPad addObject:sectionPad];
        
	}

	
	[sections addObject:section];
	
	if([UIDevice isPad])
	{
//		NSMutableArray *padSections = [[NSMutableArray alloc] initWithObjects:nil];
//		for(NSMutableArray *section in sections)
//		{
//			for(SegmentRow *sRow in section)
//				[padSections addObject:sRow];
//		}
		return sectionsPad;
	}
	else 
    {
		return sections;
    }
}


-(NSMutableArray *) fillHotelSections:(EntitySegment *)segment
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sections = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray	*sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sectionsPad = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	
	segRow.rowLabel = nil;
	segRow.rowValue = segment.vendorName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
	segRow.isVendorRow = YES;
	segRow.vendorType = @"h";
	segRow.segment = segment;
    segRow.isCopyEnable = YES;
	[section addObject:segRow];
//	[sectionPad addObject:segRow];
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	
//	if(segment.segmentName != nil && ![segment.segmentName isEqualToString:segment.vendorName])
//	{
//		segRow = [[SegmentRow alloc] init];
//		segRow.rowLabel = nil;
//		segRow.rowValue = segment.segmentName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
//		segRow.segment = segment;
//		[section addObject:segRow];
//		[segRow release];
//	}
//	
//	if([UIDevice isPad] && [sectionPad count] > 0)
//	{
//		[sectionsPad addObject:sectionPad];
//		[sectionPad release];
//		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
//	}
	
	segRow = [[SegmentRow alloc] init];
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"HOTEL";
	segRow.segment = segment;
	[section addObject:segRow];
	[sectionPad addObject:segRow];
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
    if (segment.relStartLocation != nil && segment.relEndLocation != nil && [UIDevice isPad]) {
        NSString *checkinDate = [DateTimeFormatter formatDateLong:segment.relStartLocation.dateLocal];
        NSString *checkoutDate = [DateTimeFormatter formatDateLong:segment.relEndLocation.dateLocal];
        
        SegmentRow *segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = nil;
        segRow.rowLabel = [NSString stringWithFormat:@"%@",[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_IN"]];
        segRow.rowValue = checkinDate;
        segRow.segment = segment;
        [sectionPad addObject:segRow];
        
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = nil;
        segRow.rowLabel = [NSString stringWithFormat:@"%@",[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CHECK_OUT"]];
        segRow.rowValue = checkoutDate;
        segRow.segment = segment;
        [sectionPad addObject:segRow];
    }
    
    if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
    if (segment.relStartLocation != nil && [UIDevice isPad])
    {
        NSString *city = segment.relStartLocation.city;
        NSString *startState = segment.relStartLocation.state;
        NSString *pc = segment.relStartLocation.postalCode;
        NSString *addr;
        NSString *startAddr = segment.relStartLocation.address;
        
        if(startState == nil)
            startState = @"";
        if(pc == nil)
            pc = @"";
        if(city == nil)
            city = @"";
        if(startAddr == nil)
            startAddr = @"";
        
        addr = [NSString stringWithFormat:@"%@ %@\n%@ %@", startAddr, city, startState, pc];
        
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = nil;
        segRow.rowValue = addr;
        segRow.isMap = YES;
        segRow.segment = segment;
        [sectionPad addObject:segRow];
    }
    
    if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
		
	//Hotel Details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		segRow.segment = segment;
		
		//NSLog(@"status code %@", segment.status);
		if([segment.status isEqualToString:@"HK"] || [segment.status isEqualToString:@"GK"]  || [segment.status isEqualToString:@"Confirmed"])
			segRow.color = [UIColor colorWithRed:0.0/255.0 green:102.0/255.0 blue:51.0/255.0 alpha:1.0f];
		
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.confirmationNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.confirmationNumber;
		segRow.segment = segment;
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
		//[sectionPad addObject:segRow];
	}
	
	if(segment.dailyRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DAILY_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.dailyRate crnCode:segment.currency]; 
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TOTAL_RATE"];
		segRow.rowValue = [FormatUtils formatMoneyWithNumber:segment.totalRate crnCode:segment.currency]; 
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	if(segment.cancellationPolicy != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CANCEL_POLICY"];
		segRow.rowValue = segment.cancellationPolicy;
		segRow.segment = segment;
        segRow.showDisclosure = YES;
        segRow.isDescription = YES;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.roomDescription != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ROOM"];
		segRow.rowValue = segment.roomDescription;
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
    if ([segment.travelPointsPosted length] || [segment.travelPointsPending length] || [segment.travelPointsBenchmark length])
    {
        NSString *rowLabelForTravelPoints = [@"Travel Points Booked" localize];
        NSString *rowValueForTravelPoints = @"--";
        
        if ([segment.travelPointsPosted length])
        {
            rowLabelForTravelPoints = [@"Travel Points Awarded" localize];
            rowValueForTravelPoints = segment.travelPointsPosted;
        }
        else if ([segment.travelPointsPending length])
        {
            rowLabelForTravelPoints = [@"Travel Points Booked" localize];
            rowValueForTravelPoints = segment.travelPointsPending;
        }
        
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = rowLabelForTravelPoints;
        segRow.rowValue = rowValueForTravelPoints;
        segRow.segment = segment;
        [section addObject:segRow];
        [sectionPad addObject:segRow];
        
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = [@"Price to Beat" localize];
        segRow.rowValue = [segment.travelPointsBenchmark length] ? [FormatUtils formatMoney:segment.travelPointsBenchmark crnCode:segment.travelPointsBenchmarkCurrency] : @"--";
        segRow.segment = segment;
        [section addObject:segRow];
        [sectionPad addObject:segRow];
        
        if([UIDevice isPad] && [sectionPad count] > 0)
        {
            [sectionsPad addObject:sectionPad];
            sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
        }
    }
	
	if([UIDevice isPad] && [ExSystem connectedToNetwork] && [segment.relTrip.allowCancel boolValue])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Cancel hotel reservation"];
		segRow.rowValue = nil;//[Localizer getLocalizedText:@"tap here to cancel reservation"]; //segment.roomDescription;
		segRow.segment = segment;
		segRow.isSpecialCell = NO;
		segRow.isCancel = YES;
		segRow.isEmblazoned = YES;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
		
		[sectionsPad addObject:sectionPad];
	

	}
	
	[sections addObject:section];
	
	if([UIDevice isPad])
	{
//		NSMutableArray *padSections = [[NSMutableArray alloc] initWithObjects:nil];
//		for(NSMutableArray *section in sections)
//		{
//			for(SegmentRow *sRow in section)
//				[padSections addObject:sRow];
//		}
		return sectionsPad;
	}
	else 
    {
		return sections;
    }
}


-(NSMutableArray *)fillRideSections:(EntitySegment *)segment
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sections = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	
	if(segment.vendor != nil)
	{
		segRow.rowLabel = nil;
		segRow.rowValue = segment.vendorName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.isVendorRow = YES;
		segRow.vendorType = @"l";
		segRow.segment = segment;
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
		segRow = [[SegmentRow alloc] init];
	}
	
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"RIDE";
	segRow.segment = segment;
	[section addObject:segRow];
	
	
	if(segment.relStartLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Pickup"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_ADDRESS"];
		
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
		segRow.rowValue = location; //[NSString stringWithFormat:@"%@\n%@, %@, %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];;
		segRow.isMap = YES;
		
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Phone"];
		segRow.rowValue = segment.phoneNumber;
		segRow.isPhone = YES;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.pickupInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PICKUP_INSTRUCTIONS"];
		segRow.rowValue = segment.pickupInstructions;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.meetingInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEETING_INSTRUCTIONS"];
		segRow.rowValue = segment.meetingInstructions;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.address != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF_ADDRESS"];
		segRow.rowValue = [SegmentData getMapAddress:segment.relEndLocation];
        //[NSString stringWithFormat:@"%@\n%@ %@, %@", segment.endAddress, segment.endCity, segment.endState, segment.endPostalCode];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.dropoffInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_DROP_OFF_INSTRUCTIONS"];
		segRow.rowValue = segment.dropoffInstructions;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		segRow.segment = segment;
		
		//NSLog(@"status code %@", segment.status);
		if([segment.status isEqualToString:@"HK"])
			segRow.color = [UIColor colorWithRed:0.0/255.0 green:102.0/255.0 blue:51.0/255.0 alpha:1.0f];
		
		[section addObject:segRow];
	}
	
	if(segment.confirmationNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.confirmationNumber;
		segRow.segment = segment;
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE"];
		segRow.rowValue = [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.rateDescription != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_RATE_DESCRIPTION"];
		segRow.rowValue = segment.rateDescription;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.cancellationPolicy != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CANCEL_POLICY"];
		segRow.rowValue = segment.cancellationPolicy;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	[sections addObject:section];
	
	//	//Hotel Details
	//	//section dump and reset
	//	[sections addObject:section];
	//	[section release];
	//	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	if([UIDevice isPad])
	{
		__autoreleasing NSMutableArray *padSections = [[NSMutableArray alloc] initWithObjects:nil];
		for(NSMutableArray *section in sections)
		{
			for(SegmentRow *sRow in section)
				[padSections addObject:sRow];
		}
		return padSections;
	}
	else 
		return sections;
}

-(NSMutableArray *)fillDiningSections:(EntitySegment *)segment
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sections = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	
	if(segment.segmentName != nil)
	{
		segRow.rowLabel = nil;
		segRow.rowValue = segment.segmentName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.isVendorRow = YES;
		segRow.vendorType = @"d";
		segRow.segment = segment;
		[section addObject:segRow];
		segRow = [[SegmentRow alloc] init];
	}
	
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"DINING";
	segRow.segment = segment;
	[section addObject:segRow];
	
	
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
		segRow.rowValue = [NSString stringWithFormat:@"%d", segment.numPersons.intValue];
		[section addObject:segRow];
	}
	
	
	[sections addObject:section];
	
	//	//Hotel Details
	//	//section dump and reset
	//	[sections addObject:section];
	//	[section release];
	//	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	if([UIDevice isPad])
	{
		__autoreleasing NSMutableArray *padSections = [[NSMutableArray alloc] initWithObjects:nil];
		for(NSMutableArray *section in sections)
		{
			for(SegmentRow *sRow in section)
				[padSections addObject:sRow];
		}
		return padSections;
	}
	else 
		return sections;
}

-(NSMutableArray *)fillRailSections:(EntitySegment *)segment
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sections = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	
	if(segment.vendor != nil)
	{
		segRow.rowLabel = nil;
		segRow.rowValue = segment.vendorName; // [rootViewController getURLMap:@"CAR_RENTERS" LocalConstant:segment.vendor];
		segRow.isVendorRow = YES;
		segRow.vendorType = @"a";
		segRow.segment = segment;
        segRow.isCopyEnable = YES;
        segRow.railSegOniPad = NO;
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
		segRow.railSegOniPad = NO;
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
	
	if(segment.relStartLocation.platform != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PLATFORM"];
		segRow.rowValue = segment.relStartLocation.platform;
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	if(segment.relStartLocation.railStation != nil)
	{
		NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATION"];
		segRow.rowValue = railStation;
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	if(segment.trainNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Train Number"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CLASS"];
		segRow.rowValue = segment.trainNumber;
        segRow.railSegOniPad = NO;
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
        segRow.railSegOniPad = NO;
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
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	if(segment.numStops != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STOPS"];
		segRow.rowValue = [NSString stringWithFormat:@"%d", segment.numStops.intValue];
		[section addObject:segRow];
	}
	
	if(segment.status != nil)
	{
		NSString *status = segment.statusLocalized;
		if(status == nil)
			status = segment.statusLocalized;
		
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Status"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = status;
		
		//NSLog(@"status code %@", segment.status);
		if([segment.status isEqualToString:@"HK"])
			segRow.color = [UIColor colorWithRed:0.0/255.0 green:102.0/255.0 blue:51.0/255.0 alpha:1.0f];
		
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
		segRow.rowValue = [NSString stringWithFormat:@"%d", segment.numPersons.intValue];
		[section addObject:segRow];
	}
	
	if(segment.totalRate != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TOTAL_RATE"];
        if (segment.currency != nil)
            segRow.rowValue = [NSString stringWithFormat:@"%@ (%@)", segment.totalRate, segment.currency];
        else
            segRow.rowValue = [NSString stringWithFormat:@"%@", segment.totalRate];
		
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
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	if(segment.relStartLocation.dateLocal != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relStartLocation.dateLocal];
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.cityCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CITY"];
		segRow.rowValue = segment.relEndLocation.cityCode;
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.railStation != nil)
	{
		NSString *endRailStation = [SegmentData getRailStation:segment.relEndLocation];
        
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATION"];
		segRow.rowValue = endRailStation;
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	if(segment.relEndLocation.platform != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PLATFORM"];
		segRow.rowValue = segment.relEndLocation.platform;
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	
	//Carrier details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	if(segment.vendorName != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Vendor"];
		segRow.rowValue = segment.vendorName;
        segRow.isCopyEnable = YES;
        segRow.railSegOniPad = NO;
		[section addObject:segRow];
	}
	
	if(segment.trainTypeCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TRAIN_TYPE"];
		segRow.rowValue = segment.trainTypeCode;
        segRow.railSegOniPad = NO;
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
	
	if([UIDevice isPad])
	{
		__autoreleasing NSMutableArray *padSections = [[NSMutableArray alloc] initWithObjects:nil];
		for(NSMutableArray *section in sections)
		{
			for(SegmentRow *sRow in section)
                if (sRow.railSegOniPad)
                    [padSections addObject:sRow];
		}
		return padSections;
	}
	else 
		return sections;
}

-(NSMutableArray *) fillAirSections:(EntitySegment *)segment 
{
	NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableArray	*sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sectionsPad = [[NSMutableArray alloc] initWithObjects:nil];
	__autoreleasing NSMutableArray	*sections = [[NSMutableArray alloc] initWithObjects:nil];
	
	SegmentRow *segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = nil;
	segRow.rowValue = nil;
	segRow.isAirVendor = YES;
	segRow.segment = segment;
	[section addObject:segRow];
	//[sectionPad addObject:segRow];
	
    // represents one REGULAR row in the tableview
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	segRow = [[SegmentRow alloc] init];
	segRow.isSpecialCell = YES;
	segRow.specialCellType = @"AIR";
	segRow.segment = segment;
  	[section addObject:segRow];
	[sectionPad addObject:segRow];
	
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
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	if([TripData isFlightDelayedOrCancelled: segment.relFlightStats])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
		segRow.rowValue = segment.relFlightStats.departureShortStatus;
		segRow.isEmblazoned = YES;
		segRow.segment = segment;
		
		if ([segment.relFlightStats.departureShortStatus isEqualToString:@"Cancelled"])
			segRow.color = [UIColor redColor];
		
		//////Kanye West is gonna let me speak now
		segRow.isFlightStats = YES;
			
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	else if(segment.relFlightStats.departureShortStatus != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
		segRow.rowValue = segment.relFlightStats.departureShortStatus;
		segRow.segment = segment;
		segRow.isFlightStats = YES;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	else {
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Status"];
		segRow.rowValue = [Localizer getLocalizedText: @"Scheduled On-time"];
		segRow.isEmblazoned = NO;
		segRow.segment = segment;		
		//////Kanye West is gonna let me speak now
		segRow.isFlightStats = YES;
		
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}

    if(segment.operatedBy != nil && segment.operatedByFlightNumber != nil && segment.operatedByVendor != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Operated By"];
		segRow.rowValue = [NSString stringWithFormat:@"%@ (%@) %@", segment.operatedBy, segment.operatedByVendor, segment.operatedByFlightNumber];
		[section addObject:segRow];
	}

	if(segment.phoneNumber != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = nil;
		segRow.rowValue = segment.phoneNumber; // @"425-497-5946";
		segRow.isPhone = YES;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
		
	if(segment.statusLocalized != nil && [UIDevice isPad])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Status"]; 
		segRow.rowValue = segment.statusLocalized;

		if([segment.status isEqualToString:@"HK"])
			segRow.color = [UIColor colorWithRed:0.0/255.0 green:102.0/255.0 blue:51.0/255.0 alpha:1.0f];
		else if ([segment.statusLocalized isEqualToString:@"Cancelled"])
			segRow.color = [UIColor redColor];
		
		segRow.segment = segment;
		[sectionPad addObject:segRow];
	}
	
	//confirmation number
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
	segRow.rowValue = segment.confirmationNumber;
	segRow.segment = segment;
    segRow.isCopyEnable = YES;
	[section addObject:segRow];
	//[sectionPad addObject:segRow];
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
	//class and seat
	NSString *classy = nil;
	NSString *seatNum = nil;
	if(segment.classOfServiceLocalized == nil)
		classy = @"--";
	else
		classy = segment.classOfServiceLocalized;
	
	if(segment.seatNumber == nil)
		seatNum = @"--";
	else
		seatNum = segment.seatNumber;
    
    if (![UIDevice isPad])
    {
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = nil;
//        NSString *classy = nil;
//        NSString *seatNum = nil;
//        if(segment.classOfServiceLocalized == nil)
//            classy = @"--";
//        else
//            classy = [NSString stringWithFormat:[Localizer getLocalizedText:@"Class: %@"], segment.classOfServiceLocalized];
//        
//        if(segment.seatNumber == nil)
//            seatNum = @"--";
//        else
//            seatNum = segment.seatNumber;
//        
        if(segment.seatNumber != nil && segment.aircraftCode != nil)
        {
            seatNum = [NSString stringWithFormat:[Localizer getLocalizedText:@"Seat: %@"], seatNum];
            
            segRow.rowLabel = [Localizer getLocalizedText:@"Class / Seat"];
            
            segRow.rowValue = [NSString stringWithFormat:@"%@ %@", classy, seatNum];
            
            if([segment.vendor isEqualToString:@"WN"])
                segment.aircraftCode = @"737";
            
            if(segment.aircraftCode != nil)
            {
                NSString *airURL = [self getAircraftURL:segment.vendor AircraftCode:segment.aircraftCode];
                //NSLog(@"airURL = %@", airURL);
                
                if(airURL != nil)
                {
                    segRow.isWeb = YES;
                    segRow.isSeat = YES;
                    segRow.url = airURL; // @"mobile.seatguru.com/American_Airlines/Boeing_767-300_B/";
                    NSString* strAirCraft = segment.aircraftCode == nil? @"Flight":segment.aircraftCode;
                    segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Seat Map for t"], strAirCraft];
                    
                }		
            }
            
            segRow.segment = segment;
            [section addObject:segRow];
        }
    }
    else
    {
        if (segment.classOfServiceLocalized != nil)
        {
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = nil;
            segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CLASS"];
            segRow.rowValue = classy;
            segRow.segment = segment;
            [sectionPad addObject:segRow];
        }
        
        if (segment.seatNumber != nil)
        {
            segRow = [[SegmentRow alloc] init];
            segRow.rowLabel = nil;
            if(segment.seatNumber != nil && segment.aircraftCode != nil)
            {
                if([segment.vendor isEqualToString:@"WN"])
                    segment.aircraftCode = @"737";
                
                if(segment.aircraftCode != nil)
                {
                    NSString *airURL = [self getAircraftURL:segment.vendor AircraftCode:segment.aircraftCode];
                    //NSLog(@"airURL = %@", airURL);
                    
                    if(airURL != nil)
                    {
                        segRow.isWeb = YES;
                        segRow.isSeat = YES;
                        segRow.url = airURL; // @"mobile.seatguru.com/American_Airlines/Boeing_767-300_B/";
                        NSString* strAirCraft = segment.aircraftCode == nil? @"Flight":segment.aircraftCode;
                        segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Seat Map for t"], strAirCraft];
                    }
                }
            }
            
            segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SEAT"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
            segRow.rowValue = segment.seatNumber;
            segRow.segment = segment;
            [sectionPad addObject:segRow];
        }
    }
    
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	if(segment.eTicket != nil && [UIDevice isPad])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Ticketing"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [Localizer getLocalizedText:@"E Ticket"];
		segRow.segment = segment;
		[sectionPad addObject:segRow];
	}
//	
	if(segment.miles != nil && [segment.miles intValue] != 0 && [UIDevice isPad])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Distance"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [NSString stringWithFormat:@"%@ %@", segment.miles, [Localizer getLocalizedText:@"miles"]];
		segRow.segment = segment;
		[sectionPad addObject:segRow];
	}
	
	//flight duration
	if(segment.duration != nil && [segment.duration intValue] != 0 && [UIDevice isPad])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Duration"]; 
		int flightMinutes = [segment.duration intValue];
		int flightHours = flightMinutes / 60;
		
		if (flightHours > 0) 
			flightMinutes = flightMinutes - (flightHours * 60);
		
		NSString *dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hours and %d Minute(s)"], flightHours, flightMinutes];
		
		if(flightHours < 1)
			dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Minute(s)"], flightMinutes];
		else if (flightHours == 1)
			[NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hour and %d Minute(s)"], flightHours, flightMinutes];
		
		segRow.rowValue = dur;
		segRow.segment = segment;
		//[sectionPad addObject:segRow];
	}
	
	if([segment.vendor isEqualToString:@"WN"])
		segment.aircraftCode = @"737";
	
	if(segment.aircraftCode != nil)
	{
		segRow = [[SegmentRow alloc] init];
		
		NSString *airURL = [self getAircraftURL:segment.vendor AircraftCode:segment.aircraftCode];
		//NSLog(@"airURL = %@", airURL);
		
		if(airURL == nil)
		{
			segRow.rowLabel = [Localizer getLocalizedText:@"Aircraft"];
			//			segRow.rowValue = [NSString stringWithFormat:@"%@, %@",segment.vendor, segment.aircraftCode];
			segRow.rowValue = [segment.aircraftName length] ? segment.aircraftName : segment.aircraftCode;
		}
		else {
			
			segRow.rowLabel = [Localizer getLocalizedText:@"Aircraft"];
			segRow.isWeb = YES;
			segRow.isSeat = YES;
			segRow.url = airURL; // @"mobile.seatguru.com/American_Airlines/Boeing_767-300_B/";
			NSString* strAirCraft = segment.aircraftCode == nil? @"Flight":segment.aircraftCode;
			
			NSString *aircraftCode = [NSString stringWithFormat:@"%@", segment.aircraftCode];
			if([aircraftCode intValue] > 300 && [aircraftCode intValue] < 400)
				aircraftCode = [NSString stringWithFormat:@"Airbus %@", aircraftCode];
			else if([aircraftCode intValue] > 700 && [aircraftCode intValue] < 800)
				aircraftCode = [NSString stringWithFormat:@"Boeing %@", aircraftCode];
			
			segRow.rowValue = [segment.aircraftName length] ? segment.aircraftName : aircraftCode;
			segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Seat Map for t"], strAirCraft];
			
		}
		segRow.segment = segment;
		[section addObject:segRow];
		[sectionPad addObject:segRow];
	}
	
	if(segment.numStops != nil && [UIDevice isPad])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Stops"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [NSString stringWithFormat:@"%d", [segment.numStops intValue]];
		segRow.segment = segment;
		[sectionPad addObject:segRow];
	}
	
	//Departure Details
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	//new section
	if(segment.relFlightStats.departureScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureScheduled];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureEstimated];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Departure"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.departureActual];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.departureStatusReason != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Departure Status"]; //[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_CONFIRM_NUM"];
		segRow.rowValue = segment.relFlightStats.departureStatusReason;
		if ([segment.relFlightStats.departureShortStatus isEqualToString:@"DY"] || [segment.relFlightStats.departureShortStatus isEqualToString:@"Delayed"]) 
			segRow.isEmblazoned = YES;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	//flight duration
	if(segment.duration != nil && [segment.duration intValue] != 0)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Flight Duration"]; 
		int flightMinutes = [segment.duration intValue];
		int flightHours = flightMinutes / 60;
		
		if (flightHours > 0) 
			flightMinutes = flightMinutes - (flightHours * 60);
		
		NSString *dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hours and %d Minute(s)"], flightHours, flightMinutes];
		
		if(flightHours < 1)
			dur = [NSString stringWithFormat:[Localizer getLocalizedText:@"%d Minute(s)"], flightMinutes];
		else if (flightHours == 1)
			[NSString stringWithFormat:[Localizer getLocalizedText:@"%d Hour and %d Minute(s)"], flightHours, flightMinutes];
		
		segRow.rowValue = dur;
		segRow.segment = segment;
		[section addObject:segRow];
		//[sectionPad addObject:segRow];
	}
	
	
	if(segment.meals != nil && [UIDevice isPad])
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEAL"];
		segRow.rowValue = segment.meals;
		segRow.segment = segment;
		[sectionPad addObject:segRow];
	}
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_AIRPORT"];
    segRow.rowValue = [SegmentData getAirportFullName:segment.relStartLocation];
	segRow.isWeb = YES;
	segRow.isAirport = YES;
	segRow.iataCode = segment.relStartLocation.cityCode;
	segRow.url = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relStartLocation.cityCode];
	segRow.viewTitle = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal Map for t"], segment.relStartLocation.cityCode];
	segRow.segment = segment;
	[section addObject:segRow];
	
	//Departure Flight Details section
	//section dump and reset
	[sections addObject:section];
//	[sectionPad addObject:segRow];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	
	if(segment.status != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Reservation Status"]; 
		//[Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_STATUS"];
		segRow.rowValue = segment.status;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.equipmentScheduled != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentScheduled;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.equipmentActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Equipment"];
		segRow.rowValue = segment.relFlightStats.equipmentActual;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.meals != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MEAL"];
		segRow.rowValue = segment.meals;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.specialInstructions != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_SPECIAL_INSTRUCTIONS"];
		segRow.rowValue = segment.specialInstructions;
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	//airline web site
	NSString *urlMap = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
	if(urlMap != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = nil;
		//	NSLog(@"URL=%@", [rootViewController getURLMap:@"AIRLINES" LocalConstant:segment.vendor]);
		//	NSString *airlineURL = [rootViewController getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
		//	if (airlineURL != nil & [airlineURL length] > 0) 
		//	{
		//		[self loadWebView:airlineURL WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segment.vendorName]];
		//	}
		segRow.rowValue = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
		segRow.url = [[ExSystem sharedInstance] getURLMap:@"AIRLINES" LocalConstant:segment.vendor];
		segRow.viewTitle = segment.vendorName;
		segRow.isWeb = YES;
		segRow.segment = segment;
        segRow.isCopyEnable = YES;
		[section addObject:segRow];
	}
	
	
	//on hold until Mobiata gives me a good link
	if(segment.confirmationNumber != nil && segment.flightNumber != nil && segment.vendor != nil && segment.relStartLocation.cityCode != nil && segment.relEndLocation.cityCode != nil && segment.relStartLocation.dateLocal != nil && segment.relEndLocation.dateLocal != nil && ![[ExSystem sharedInstance] isGovernment])
	{
		//flighttrack:saveSearch?airlineID=CO&flightNumber=1&departureAirportID=IAH&arrivalAirportID=HNL&departureDate=201003310&arrivalDate=20100331
		//replace saveSearch with performSearch for the other flight option
		//moflighttrack:saveFlight?departureDate=201009260800&departureAirportID=PHL&arrivalAirportID=AUA&airlineID=US&flightNumber=853&arrivalDate=201009261230&notes=My%20Notes&confirmationNumber=ABC123&source=concur

		NSString *depDate = [NSString stringWithFormat:@"%@%@", [DateTimeFormatter formatDateyyyyMMdd:segment.relStartLocation.dateLocal], [DateTimeFormatter formatTimeHHmm:segment.relStartLocation.dateLocal]];
		NSString *arrDate = [NSString stringWithFormat:@"%@%@", [DateTimeFormatter formatDateyyyyMMdd:segment.relEndLocation.dateLocal], [DateTimeFormatter formatTimeHHmm:segment.relEndLocation.dateLocal]];

        NSString *flightTrackURL = [NSString stringWithFormat:
                                    @"moflighttrack:saveFlight?departureDate=%@&departureAirportID=%@&arrivalAirportID=%@&airlineID=%@&flightNumber=%@&arrivalDate=%@&notes=Concur&confirmationNumber=%@&source=concur"
                                    ,depDate, segment.relStartLocation.cityCode, segment.relEndLocation.cityCode
                                    , segment.vendor, segment.flightNumber, arrDate, segment.confirmationNumber];
        
        //NSLog(@"flightTrackURL=%@", flightTrackURL);
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = @"Flight Tracker";
        segRow.rowValue = @"Track your flight";
        segRow.url = flightTrackURL;
        segRow.isApp = YES;
        [section addObject:segRow];
        [sectionPad addObject:segRow];
	}
    
    if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
    // Flight schedules section
    segRow = [[SegmentRow alloc] init];
    segRow.rowLabel = [Localizer getLocalizedText:@"Flight Schedules"];
	segRow.rowValue = [Localizer getLocalizedText:@"See Alternative Flights"];
    segRow.isFlightSchedule = YES;
    segRow.segment = segment;
    [section addObject:segRow];
    [sectionPad addObject:segRow];
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
	
	//	if(segment.price != nil)	
	//	{
	//		segRow.rowLabel = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_PRICE"];
	//		segRow.rowValue = nil;
	//		[section addObject:segRow];
	//		[segRow release];
	//		segRow = [[SegmentRow alloc] init];	
	//	}
	
	//Arrival Section
	//section dump and reset
	[sections addObject:section];
	section = [[NSMutableArray alloc] initWithObjects:nil];
	
	//arrival date time
	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [Localizer getLocalizedText:@"Scheduled Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
	segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relEndLocation.dateLocal];
	segRow.segment = segment;
	[section addObject:segRow];
	
	if(segment.relFlightStats.arrivalEstimated != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Estimated Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalEstimated];
		segRow.segment = segment;
		[section addObject:segRow];
	}
	
	if(segment.relFlightStats.arrivalActual != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText:@"Actual Arrival"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.relFlightStats.arrivalActual];
		segRow.segment = segment;
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
	segRow.segment = segment;
	[section addObject:segRow];
//	[sectionPad addObject:segRow];
	
	if([UIDevice isPad] && [sectionPad count] > 0)
	{
		[sectionsPad addObject:sectionPad];
		//[sectionPad release];
		//sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
	}
    
    if ([segment.travelPointsPosted length] || [segment.travelPointsPending length] || [segment.travelPointsBenchmark length])
    {
        NSString *rowLabelForTravelPoints = [@"Travel Points Booked" localize];
        NSString *rowValueForTravelPoints = @"--";
        
        if ([segment.travelPointsPosted length])
        {
            rowLabelForTravelPoints = [@"Travel Points Awarded" localize];
            rowValueForTravelPoints = segment.travelPointsPosted;
        }
        else if ([segment.travelPointsPending length])
        {
            rowLabelForTravelPoints = [@"Travel Points Booked" localize];
            rowValueForTravelPoints = segment.travelPointsPending;
        }
        
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = rowLabelForTravelPoints;
        segRow.rowValue = rowValueForTravelPoints;
        segRow.segment = segment;
        [section addObject:segRow];
        [sectionPad addObject:segRow];
        
        segRow = [[SegmentRow alloc] init];
        segRow.rowLabel = [@"Price to Beat" localize];
        segRow.rowValue = [segment.travelPointsBenchmark length] ? [FormatUtils formatMoney:segment.travelPointsBenchmark crnCode:segment.travelPointsBenchmarkCurrency] : @"--";
        segRow.segment = segment;
        [section addObject:segRow];
        [sectionPad addObject:segRow];
        
        if([UIDevice isPad] && [sectionPad count] > 0)
        {
            [sectionsPad addObject:sectionPad];
            sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
        }
    }

	segRow = [[SegmentRow alloc] init];
	segRow.rowLabel = [NSString stringWithFormat:@"%@\n%@", [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_TERMINAL"], 
					   [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_GATE"]];
    NSMutableString *term = [NSMutableString string];
    NSMutableString *gate = [NSMutableString string];
    
    [SegmentData getArriveTermGate:segment terminal:term gate:gate];
	segRow.rowValue = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
	segRow.segment = segment;
	[section addObject:segRow];
	
	if(segment.relFlightStats.baggageClaim != nil)
	{
		segRow = [[SegmentRow alloc] init];
		segRow.rowLabel = [Localizer getLocalizedText: @"Baggage Claim"]; // [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_ARRIVE"];
		segRow.rowValue = segment.relFlightStats.baggageClaim;
		segRow.segment = segment;
		[section addObject:segRow];
	}
  

    
    if([UIDevice isPad] && [ExSystem connectedToNetwork] /*&& [segment.relTrip.allowCancel boolValue]*/)
	{
//        if([UIDevice isPad] && [sectionPad count] > 0)
//        {
//            sectionPad = [[NSMutableArray alloc] initWithObjects:nil];
//        }

        [sections addObject:section];
//        section = [[NSMutableArray alloc] initWithObjects:nil];
//        
//		segRow = [[SegmentRow alloc] init];
//		segRow.rowLabel = [Localizer getLocalizedText:@"Cancel air reservation"];
//		segRow.rowValue = [Localizer getLocalizedText:@"tap here to cancel reservation"]; //segment.roomDescription;
//		segRow.segment = segment;
//		segRow.isSpecialCell = NO;
//		segRow.isCancel = YES;
//		segRow.isEmblazoned = YES;
//		[section addObject:segRow];
//        [sections addObject:section];
//		[sectionPad addObject:segRow];
//		[segRow release];
//		
//		[sectionsPad addObject:sectionPad];
//        [sectionPad release];
//
//        [section release];
        
	}
    else
    {
        [sections addObject:section];
    }
	

    //[sectionPad release];
	
	if([UIDevice isPad])
	{
		return sectionsPad;
	}
	else 
    {
		return sections;
    }
}



-(NSString *)getAircraftURL:(NSString *)vendorCode AircraftCode:(NSString *)aircraftCode
{
	NSString *path = [[NSBundle mainBundle] bundlePath];
	NSString *finalPath = [path stringByAppendingPathComponent:@"AirlineInfo.plist"]; //en_Configuration
	NSDictionary *airDict = [NSDictionary dictionaryWithContentsOfFile:finalPath];
	
	if([aircraftCode intValue] > 300 && [aircraftCode intValue] < 400)
		aircraftCode = [NSString stringWithFormat:@"A%@", aircraftCode];
	
	NSString *key = [NSString stringWithFormat:@"%@,%@", vendorCode, aircraftCode];
	__autoreleasing NSString* result = nil;
    
	if(airDict[key] != nil)
	{
		result = airDict[key];
	}
	else 
	{
		//let's get kind of fuzzy here and try to see if our aircraft and vendor string exists inside of any keys
		for(NSString *currKey in airDict)
		{
			NSRange match;
			match = [currKey rangeOfString: key];
			
			if(match.location != NSNotFound)
            {
				result = airDict[currKey];
                break;
            }
		}
	}
    return result;
}

//-(NSMutableArray *) fillOfferRows:(SegmentData *)segment 
//{
//    NSMutableArray	*section = [[NSMutableArray alloc] initWithObjects:nil];
//	NSMutableArray	*sections = [[NSMutableArray alloc] initWithObjects:nil];
//	
//	SegmentRow *segRow = [[SegmentRow alloc] init];
//	
//	if(segment.startDateLocal != nil)
//	{
//		segRow.rowLabel = [Localizer getLocalizedText:@"Offer"];
//		segRow.rowValue = [DateTimeFormatter formatDateTimeForTravel:segment.startDateLocal];
//		segRow.segment = segment;
//		[section addObject:segRow];
//		[segRow release];
//	}
//		
//	[sections addObject:section];
//	[section release];
//	
//	if([UIDevice isPad])
//	{
//		NSMutableArray *padSections = [[NSMutableArray alloc] initWithObjects:nil];
//		for(NSMutableArray *section in sections)
//		{
//			for(SegmentRow *sRow in section)
//				[padSections addObject:sRow];
//		}
//		[sections release];
//		return [padSections autorelease];
//	}
//	else 
//		return [sections autorelease];
//
//}
@end
