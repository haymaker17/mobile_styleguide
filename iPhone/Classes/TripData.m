//
//  TripData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TripData.h"
#import "DateTimeFormatter.h"
#import "DateTimeConverter.h"
#import "EntityBooking.h"
#import "EntitySegment.h"
#import "EntitySegmentLocation.h"
#import "Location.h"
#import "MapViewController.h"
#import "OfferManager.h"


@interface TripData (Private)
+(void)addOfferEntriesInternal:(NSString *)segKey ma:(NSMutableArray *)ma seg:(EntitySegment *)seg;
@end

@implementation TripData

@synthesize itinSourceName;
@synthesize tripName;
@synthesize tripStartDateLocal;
@synthesize tripEndDateLocal;
@synthesize tripKey;
@synthesize cliqbookTripId;
@synthesize clientLocator;

@synthesize bookings;
@synthesize bookingKeys;
@synthesize booking;

@synthesize hasHotel;
@synthesize hasAir;
@synthesize hasRail;
@synthesize hasEvent;
@synthesize hasDining;
@synthesize hasCar;
@synthesize hasRide;
@synthesize hasUndefined;
@synthesize hasParking;
@synthesize offers;
@synthesize hasValidOffers;
@synthesize state, isExpensed, itinLocator;

-(id) init
{
    self = [super init];
    if (self)
    {
        self.offers = [[NSMutableDictionary alloc] init];
        self.state = 0; // None
    }
    return self;
}


-(void)initWithBookings
{
//	bookings = [[NSMutableDictionary alloc] init];//];
//	bookingKeys = [[NSMutableArray alloc] init];//
//	[self.itinSourceName initWithString:@""];
//	[self.tripStartDateLocal initWithString:@""];
	bookingId = 0;
}

-(void)finishBooking
{
//	if (booking.recordLocator != nil)
//	{
		if (bookings == nil)
		{
			bookings = [[NSMutableDictionary alloc] init];
		}
	bookings[[NSString stringWithFormat:@"%d", bookingId]] = booking; // booking.recordLocator];
		
		if (bookingKeys == nil)
		{
			bookingKeys = [[NSMutableArray alloc] init];
		}
	 [bookingKeys addObject:[NSString stringWithFormat:@"%d", bookingId]]; //booking.recordLocator];
	 bookingId++;
//	}
}


+ (NSMutableDictionary *)getSegmentsOrderByDate:(EntityTrip *) trip
{
	__autoreleasing NSMutableDictionary *segmentsByDate = [NSMutableDictionary new];
	
	for (EntityBooking * thisBooking in trip.relBooking)
	{
		NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
		// specify timezone
		[dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
		// Localizing date
		[dateFormat setLocale:[NSLocale currentLocale]];
		
		[dateFormat setDateFormat: @"EEE MMM dd"];	
		
		for(EntitySegment *seg in thisBooking.relSegment)
		{
			NSDate *startDate = [DateTimeFormatter getNSDateFromMWSDateString:seg.relStartLocation.dateLocal];//[DateTimeFormatter getNSDate:seg.relStartLocation.dateLocal Format:@"yyyy-MM-dd'T'HH:mm:ss"]; // [NSDate dateWithNaturalLanguageString:seg.startDateLocal locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
			NSString *startFormatted = nil;
			startFormatted = [dateFormat stringFromDate:startDate];
			
			NSMutableArray *existingSegs;
			
			if (seg.relStartLocation.dateLocal == nil)
			{
				startFormatted = @"Undefined";
			}
			
			if (segmentsByDate[startFormatted] != nil)
			{
				existingSegs = segmentsByDate[startFormatted];
				//only add hotel segments to the last position, all others need to be before hotel segments.
				if([seg.type isEqualToString:@"HOTEL"])
					[existingSegs addObject:seg];
				else 
				{
					int lastPos = [existingSegs count] - 1;
					SegmentData *lastSeg = existingSegs[lastPos];
					if([lastSeg.type isEqualToString:@"HOTEL"])
						[existingSegs insertObject:seg atIndex:lastPos];
					else 
						[existingSegs addObject:seg];
				}
			}
			else 
			{
				existingSegs = [[NSMutableArray alloc] initWithObjects:seg, nil];
				segmentsByDate[startFormatted] = existingSegs;
			}

		}
	}

	return segmentsByDate;		
}




//-(NSMutableArray *)getSegmentDates
//{
//	NSMutableArray *dates = [NSMutableArray new]; //array of the dates of the segments
//	NSMutableDictionary *segmentsByDate = [NSMutableDictionary new]; //dictionary that holds on to the arrays of segments by a key of date e.g. (12/20/2009)
//	
//	for (int x=0; x < [bookingKeys count]; x++)
//	{
//		NSString *recLoc = [bookingKeys objectAtIndex:x];
//		BookingData *thisBooking = [bookings objectForKey:recLoc];
//		for(NSString *segKey in thisBooking.segments)
//		{
//			SegmentData *seg = [thisBooking.segments objectForKey:segKey];
//			//NSString *idKey = seg.cliqbookId;
//			NSString *start = seg.startDateLocal;			
//			NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
//			// specify timezone
//			[dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//			// Localizing date
//			[dateFormat setLocale:[NSLocale currentLocale]];
//			
//			[dateFormat setDateFormat: @"EEE MMM dd"];			
//			NSDate *startDate = [DateTimeFormatter getNSDate:start Format:@"yyyy-MM-dd'T'HH:mm:ss"];// [NSDate dateWithNaturalLanguageString:start locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//			NSString *startFormatted = [dateFormat stringFromDate:startDate];
//			
//			
//			if ([segmentsByDate objectForKey:startFormatted] == nil)
//			{//could not find the date
//				[dates addObject:startFormatted]; //todo: sometimes tries to insert nils in here... why?
//				[segmentsByDate setObject:startFormatted forKey:startFormatted];
//				//existingSegs = [segmentsByDate objectForKey:startFormatted];
//			}
//			[dateFormat release];
//
//		}
//	}
//	[segmentsByDate release];
//	
//	return [dates autorelease];		
//}
//


//-(NSArray *)getBookingsOrderByType
//{
//	NSMutableArray *types = [[NSMutableArray alloc] init]; //array of the types Air, Car, Etc
//	NSMutableArray *returnVals = [[NSMutableArray alloc] init]; //The array that gets sent back
//	NSMutableDictionary *bookTypes = [[NSMutableDictionary alloc] init]; //dictionary that holds on to the arrays of reclocs by a key of type e.g. (Air)
//	//bookTypes = nil;
//	
//	for(int x = 0; x < [bookingKeys count]; x++)
//	{
//		NSString *recLoc = [bookingKeys objectAtIndex:x];		
//		BookingData *thisBooking = [bookings objectForKey:recLoc];
//		//NSMutableArray *booksforType; //the array of bookings that exist for a specific type, stored in dict
//		
////		if (bookTypes == nil)
////		{//first time in
////			booksforType = [[NSMutableArray alloc] init];
////			[types addObject:thisBooking.type];
////		}
////		else 
//		//NSLog(@"thisBooking.type=%@", thisBooking.type);
////		if ([thisBooking.type isEqualToString:@"AIR"]) 
////		{
//			for(NSString *segKey in thisBooking.segments)
//			{
//				SegmentData *seg = [thisBooking.segments objectForKey:segKey];
//				
//				NSDateFormatter *dateFormat = [[NSDateFormatter alloc] init];
//				// specify timezone
//				[dateFormat setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
//				// Localizing date
//				[dateFormat setLocale:[NSLocale currentLocale]];
//				
//				[dateFormat setDateFormat: @"EEE MMM dd HH:mm zzz"];
//				NSDate *startDate = [DateTimeFormatter getNSDate:seg.startDateLocal Format:@"yyyy-MM-dd'T'HH:mm:ss"];// [NSDate dateWithNaturalLanguageString:seg.startDateLocal locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
//				NSString *formedDate = [dateFormat stringFromDate:startDate];
//				[dateFormat release];
//				
//				if([seg.type isEqualToString:@"AIR"])
//				{			
//					if ([bookTypes objectForKey:seg.type] == nil)
//					{//never used this type before
//						NSMutableArray *booksforType = [[NSMutableArray alloc] init];
//						[types addObject:seg.type]; //so we know which types have been found...
//						//[booksforType addObject:seg.confirmationNumber]; //add the confirmation number to the array of bookings for this type
//						[booksforType addObject:[NSString stringWithFormat:@"%@ to %@ %@\nConfirmation #:%@", seg.startCityCode, seg.endCityCode, formedDate, seg.confirmationNumber]];
//						[bookTypes setObject:booksforType forKey:seg.type]; //add the booking recloc array for the type to the dictionary keyed by type
//						[booksforType release];
//					}
//					else 
//					{//type exists, get the array of reclocs for that type
//						NSMutableArray *booksforType = [bookTypes objectForKey:seg.type];
//						//[booksforType addObject:seg.confirmationNumber]; //add the recloc to the array of bookings for this type
//						//[booksforType addObject:[NSString stringWithFormat:@"%@ to %@ %@", seg.startCityCode, seg.endCityCode, formedDate]];
//						[booksforType addObject:[NSString stringWithFormat:@"%@ to %@ %@\nConfirmation #:%@", seg.startCityCode, seg.endCityCode, formedDate, seg.confirmationNumber]];
//						//[bookTypes setObject:booksforType forKey:thisBooking.type]; //add the booking recloc array for the type to the dictionary keyed by type
//					}
//				}
//			}
//		//}
//
//
//	}
//	
//	
//	
//	//[keys sortUsingDescriptors:[NSArray arrayWithObject:[[[NSSortDescriptor alloc] initWithKey:@"Score" ascending:NO] autorelease]]];
//	
//	for(int x = 0; x < [types count]; x++)
//	{//now process the found types
//		NSMutableArray *booksForType; // = [[NSMutableArray alloc] init];
//		if ([bookTypes objectForKey:[types objectAtIndex:x]] != nil)
//		{
//			booksForType = [bookTypes objectForKey:[types objectAtIndex:x]];
//			for(int y=0; y < [booksForType count]; y++)
//			{
//				[returnVals addObject:[booksForType objectAtIndex:y]]; //add an array of bookings for this type
//			}
//		}
//		//[booksForType release];
//	}
//	
//	
//	[bookTypes release];
//	[types release];
//	return [returnVals autorelease];		
//}
//


		

//-(NSArray *)getAirSegmentsInDateOrder
//{	
//	NSMutableArray *segments = [[NSMutableArray alloc] init];
//		
//	for(int x = 0; x < [bookingKeys count]; x++)
//	{
//		NSString *recLoc = [bookingKeys objectAtIndex:x];		
//		BookingData *thisBooking = [bookings objectForKey:recLoc];
//		
//		//now loop the segments looking for air
//		for(NSString *segKey in thisBooking.segments)
//		{
//			SegmentData *seg = [thisBooking.segments objectForKey:segKey];
//			if ([seg.type isEqualToString:@"AIR"]) 
//			{
//				NSMutableDictionary *segmentDates = [[NSMutableDictionary alloc] initWithObjectsAndKeys:seg.startDateLocal, @"START_DATE", seg, @"SEGMENT", nil];
//				[segments addObject:segmentDates]; 
//				[segmentDates release];
//			}
//		}
//		
//	}
//	//[keys sortUsingDescriptors:[NSArray arrayWithObject:[[[NSSortDescriptor alloc] initWithKey:@"Score" ascending:NO] autorelease]]];
//	[segments sortUsingDescriptors:[NSArray arrayWithObject:[[[NSSortDescriptor alloc] initWithKey:@"START_DATE" ascending:YES] autorelease]]];
//
//	return [segments autorelease];		
//}
//
//

	
-(NSString *)getRecLocsBySegmentType:(NSString *)segType
{//return the booking record locators for all bookings
	int cnt = [bookingKeys count];
	////NSLog(@"Count=%@", [bookingKeys count]);
	int x = 0;
	__autoreleasing NSMutableString *recLocs = nil;
	////NSLog(@"getRecLocsBySegmentType:segType: %@ ", segType);
	
	while(x < cnt)
	{
		NSString *recLoc = bookingKeys[x];		
		BookingData *thisBooking = bookings[recLoc];
		
		////NSLog(@"getRecLocsBySegmentType:thisBooking.type=%@", thisBooking.type);
		
		if ([segType isEqualToString:thisBooking.type]) 
		{
			
			////NSLog(@"recLoc=%@", recLoc);
			////NSLog(@"thisBooking.type=%@", thisBooking.type);
			if (recLocs == nil)
			{
				recLocs = [[NSMutableString alloc] initWithString:recLoc];
			}
			else 
			{
				[recLocs appendString:@", "];
				[recLocs appendString:recLoc];
			}
		}
		x++;
	}
	////NSLog(@"getRecLocsBySegmentType:recLocs=%@", recLocs);
	return recLocs;	
}

-(NSString *)getBookingRecLocs
{//return the booking record locators for all bookings
	int cnt = [bookingKeys count];
	int x = 0;
	__autoreleasing NSMutableString *recLocs;
	recLocs = nil;
	while(x < cnt)
	{
		NSString *recLoc = bookingKeys[x];
		if (recLocs == nil)
		{
			recLocs = [[NSMutableString alloc] initWithString:recLoc];
		}
		else 
		{
			[recLocs appendString:@", "];
			[recLocs appendString:recLoc];
		}
		x++;
	}
	return recLocs;	
}



//-(SegmentData *) getSegment:(NSString *)segKey
//{
//	
//	for(int i = 0; i < [bookings count]; i++) 
//	{
//		NSString *key = [bookingKeys objectAtIndex:i];
//		BookingData *book = [bookings objectForKey:key];
//		if([book.segments objectForKey:segKey] != nil)
//			return [book.segments objectForKey:segKey];
//	}
//	
//	return nil;
//}

-(NSMutableString *) getAgencyPCC
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	__autoreleasing NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
	
	for(NSString *key in bookings)
	{
		BookingData *booky = bookings[key];
		if (booky.agencyPCC != nil) 
		{
			NSString *appc = booky.agencyPCC;
			dict[appc] = appc;
		}
	}
	
	int i = 0;
	for(NSString *key in dict)
	{
		if(i > 0)
			[s appendString:@", "];
		[s appendString:key];
		i++;
	}
	
	return s;
}


-(NSMutableString *) getBookingSources
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	__autoreleasing NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
	
	for(NSString *key in bookings)
	{
		BookingData *booky = bookings[key];
		if (booky.bookSource != nil) 
		{
			NSString *sVal = booky.bookSource;
			dict[sVal] = sVal;
		}
	}
	
	int i = 0;
	for(NSString *key in dict)
	{
		if(i > 0)
			[s appendString:@", "];
		[s appendString:key];
		i++;
	}
	
	return s;
}

+ (NSMutableString *) getCompanyAccountingCodes:(EntityTrip*) trip
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	__autoreleasing NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
	
	for(EntityBooking *booky in trip.relBooking)
	{
		if (booky.companyAccountingCode != nil) 
		{
			NSString *sVal = booky.companyAccountingCode;
			dict[sVal] = sVal;
		}
	}
	
	int i = 0;
	for(NSString *key in dict)
	{
		if(i > 0)
			[s appendString:@", "];
		[s appendString:key];
		i++;
	}
	
	return s;
}

+(NSMutableString *) getRecordLocators:(EntityTrip*) trip
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	__autoreleasing NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
	
	for(EntityBooking *booky in trip.relBooking)
	{
		if (booky.recordLocator != nil) 
		{
			NSString *sVal = booky.recordLocator;
			dict[sVal] = sVal;
		}
	}
	
	int i = 0;
	for(NSString *key in dict)
	{
		if(i > 0)
			[s appendString:@", "];
		[s appendString:key];
		i++;
	}
	
	return s;
}

+(NSMutableString *) getTravelConfigIds:(EntityTrip*) trip
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	__autoreleasing NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
	
	for(EntityBooking *booky in trip.relBooking)
	{
		if (booky.travelConfigId != nil) 
		{
			NSString *sVal = booky.travelConfigId;
			dict[sVal] = sVal;
		}
	}
	
	int i = 0;
	for(NSString *key in dict)
	{
		if(i > 0)
			[s appendString:@", "];
		[s appendString:key];
		i++;
	}
	
	return s;
}

+ (NSMutableString *) getTypes:(EntityTrip*) trip
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	__autoreleasing NSMutableString *s = [[NSMutableString alloc] initWithString:@""];
	
	for(EntityBooking *booky in trip.relBooking)
	{
		if (booky.type != nil) 
		{
			NSString *sVal = booky.type;
			dict[sVal] = sVal;
		}
	}
	
	int i = 0;
	for(NSString *key in dict)
	{
		if(i > 0)
			[s appendString:@", "];
		[s appendString:key];
		i++;
	}
	
	return s;
}


-(int) getSegmentCount
{
    int i = 0;
    
    for (int x=0; x < [bookingKeys count]; x++)
	{
		NSString *recLoc = bookingKeys[x];
		BookingData *thisBooking = bookings[recLoc];
        i = i + [thisBooking.segments count];
    }
    
    return i;
}


+(BOOL)isOfferValid:(EntityOffer*)offer
{
    if ([[ExSystem sharedInstance].offersValidityChecking isEqualToString:@"NO"] || ([OfferManager hasValidTimeRange:offer ] && [OfferManager hasValidProximity:offer]))
    {
        return TRUE;
    }
    
    return FALSE;
 }


// Get all offers for each segment and shove valid offers into the list.
+ (void)addOfferEntriesInternal:(NSString *)segKey ma:(NSMutableArray *)ma seg:(EntitySegment *)seg
{
    NSString *segKeyStart = @"Start";
    NSString *segKeyDuration = @"Duration";
    NSString *segKeyEnd = @"End";
                
    NSUInteger index = [ma indexOfObject:seg];
    
    if (index == NSNotFound)
        return;
    
    NSArray *arrOffers = [[OfferManager sharedInstance] fetchOffersBySegIdKeyAndSegmentSide:segKey segmentSide:segKeyStart ];
    
    for (EntityOffer *offer in arrOffers) {
        if ([self isOfferValid:offer])
        {

            [ma insertObject:offer atIndex:index];
            index++;
        }
    }
    
    arrOffers = (NSMutableArray*)[[OfferManager sharedInstance] fetchOffersBySegIdKeyAndSegmentSide:segKey segmentSide:segKeyDuration];
    for (EntityOffer *offer in arrOffers) {
        if ([self isOfferValid:offer])
        {
            [ma insertObject:offer atIndex:index];
            index++;
        }
    }
    
    index++;
    
    arrOffers = (NSMutableArray*)[[OfferManager sharedInstance] fetchOffersBySegIdKeyAndSegmentSide:segKey segmentSide:segKeyEnd];
    for (EntityOffer *offer in arrOffers)
    {
        if ([self isOfferValid:offer]) 
        {
            if (index >= [ma count]) 
            {
                [ma addObject:offer];
            }
            else 
            {
                [ma insertObject:offer atIndex:index];
            }
            index++; 
        }
    }
}

+ (NSMutableDictionary *)makeSegmentDictGroupedByDate:(EntityTrip*) trip
{
	__autoreleasing NSMutableDictionary *segDictByDate = [[NSMutableDictionary alloc] init];
	NSMutableArray *holdKeys = [[NSMutableArray alloc] init];
	
#ifdef IGNITE
	// Allow segments (EVENT/DINING) without booking to come through
    for(EntitySegment *seg in trip.relSegment)
#else
    NSSet *bookings = trip.relBooking;
    
	for(EntityBooking *bookingData in bookings)
	{
//		BookingData *bookingData = [bookings objectForKey:bookingKey];
		for(EntitySegment *seg in bookingData.relSegment)
#endif
		{
//			SegmentData *seg = [bookingData.segments objectForKey:segKey];
			
			if (seg.relStartLocation.dateLocal == nil)
				seg.relStartLocation.dateLocal = @"1900-01-01 01:01";
            
			NSString *formedDate = [DateTimeFormatter formatDateForTravel:seg.relStartLocation.dateLocal];
            
			if (segDictByDate[formedDate] == nil ) 
			{
				NSMutableArray *ma = [[NSMutableArray alloc] initWithObjects:nil];
                [ma addObject:seg];

                segDictByDate[formedDate] = ma;
				[holdKeys addObject:seg.relStartLocation.dateLocal];
			}
			else
			{
				NSMutableArray *ma = segDictByDate[formedDate];
                
				if([seg.type isEqualToString:@"HOTEL"])
                {
					[ma addObject:seg];
                }
				else 
				{
					int lastPos = [ma count] - 1;
					EntitySegment *lastSeg = ma[lastPos];
					if([lastSeg isKindOfClass:[EntitySegment class]] && [lastSeg.type isEqualToString:@"HOTEL"])
						[ma insertObject:seg atIndex:lastPos];
                    else 
                        [ma addObject:seg]; // MOB-7813
				}

			}
		}
#ifdef IGNITE
#else
	}
#endif

	//now sort inside each day
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];
		NSSortDescriptor *descriptor = [[NSSortDescriptor alloc] initWithKey:@"relStartLocation.dateLocal" ascending:YES];
		[ma sortUsingDescriptors:[NSMutableArray arrayWithObjects:descriptor,nil]];
        		
        
		NSMutableArray *a = [[NSMutableArray alloc] initWithArray:ma];
		int iCount = 0;
		for(int i = 0; i < [a count]; i++)
		{
			EntitySegment *s = a[i];
#ifdef IGNITE
            // Dining Genius cell should be push to the back. Start time set to after 11:59pm to ensure last placement.
            // Insert hotel before dining
            EntitySegment* lastSeg = [ma objectAtIndex:([ma count]-1)];
			if([s isKindOfClass:[EntitySegment class]] && 
               [s.type isEqualToString:SEG_TYPE_HOTEL] && [lastSeg.type isEqualToString:SEG_TYPE_DINING])
            {
				//shove to the second to the last
				[ma removeObjectAtIndex:i - iCount];
				[ma insertObject:s atIndex:[ma count]-1];
                if (i < [a count]-2) // Increase offset, if before second to the last
                    iCount++;                    
            }
			else if([s isKindOfClass:[EntitySegment class]] && 
               ([s.type isEqualToString:@"HOTEL"] || ([s.type isEqualToString:SEG_TYPE_DINING] /*&& ![SegmentData isSegmentScheduled:s]*/)))
#else
			if([s isKindOfClass:[EntitySegment class]] && [s.type isEqualToString:@"HOTEL"])
#endif
			{
				//shove to the back
				[ma removeObjectAtIndex:i - iCount];
				[ma addObject:s];
				iCount++;
			}
		}
	}
    
    // New method to add offers in the segDictByDate
    // segDictByDate is already in sorted order 
    // Get all segments for each date
    //  {
    //      For each segment check all valid offers
    //       for each valid offer update segment array with that offer
    //  }
    for(NSString *segDate in segDictByDate)
    {
        NSMutableArray *ma = segDictByDate[segDate];
        NSMutableArray *a = [[NSMutableArray alloc] initWithArray:ma];
        
		for(int i = 0; i < [a count]; i++)
		{
			EntitySegment *seg = a[i];
             [self addOfferEntriesInternal: seg.idKey ma: ma seg: seg];
        }
    }
	
	NSArray *sortedKeys = [holdKeys sortedArrayUsingSelector:@selector(caseInsensitiveCompare:)];
	
	holdKeys = [[NSMutableArray alloc] init];
	int cnt = [sortedKeys count];
	for (int x = 0; x < cnt; x++)
	{
		NSString *sortedDate = sortedKeys[x]; 
		[holdKeys addObject:[DateTimeFormatter formatDateForTravel:sortedDate]];
	}
	
	return segDictByDate;
}

+ (NSString*) getFirstDestination:(EntityTrip *) trip
{
    NSArray *lstDates = [TripData makeSegmentArrayGroupedByDate:trip];
    NSDictionary *dictSegmentsByDate = [TripData makeSegmentDictGroupedByDate:trip];
    if (lstDates != nil && [lstDates count] > 0)
    {
        NSArray* segments = dictSegmentsByDate[lstDates[0]];
        __autoreleasing NSString * lastAirportCity = nil;
        for(EntitySegment *seg in segments)
        {		
			if (seg.relEndLocation.airportCity != nil)
				lastAirportCity = seg.relEndLocation.airportCity;
//            else if (seg.relEndLocation.city != nil)
//                return seg.relEndLocation.city;
        }
        return lastAirportCity;
    }
    return nil;
}

+ (NSMutableArray *)makeSegmentArrayGroupedByDate:(EntityTrip*) trip
{
	NSMutableDictionary *segDictByDate = [[NSMutableDictionary alloc] init];
	__autoreleasing NSMutableArray *holdKeys = [[NSMutableArray alloc] init];
	
	for(EntityBooking *bookingData in trip.relBooking)
    {
        for(EntitySegment *seg in bookingData.relSegment)
        {		
			if (seg.relStartLocation.dateLocal == nil)
				seg.relStartLocation.dateLocal = @"1900-01-01 01:01";
            
			NSString *formedDate = [DateTimeFormatter formatDateForTravel:seg.relStartLocation.dateLocal];
            
			if (segDictByDate[formedDate] == nil ) 
			{
				NSMutableArray *ma = [[NSMutableArray alloc] initWithObjects:nil];
                [ma addObject:seg];
   
				segDictByDate[formedDate] = ma;
				[holdKeys addObject:seg.relStartLocation.dateLocal];
			}
			else
			{
				NSMutableArray *ma = segDictByDate[formedDate];
                                
				if([seg.type isEqualToString:@"HOTEL"])
					[ma addObject:seg];
				else 
				{
					int lastPos = [ma count] - 1;
					EntitySegment *lastSeg = ma[lastPos];
					if([lastSeg.type isEqualToString:@"HOTEL"])
						[ma insertObject:seg atIndex:lastPos];
				}

			}
		}
	}
	
	//now sort inside each day
	for(NSString *segDate in segDictByDate)
	{
		NSMutableArray *ma = segDictByDate[segDate];

		NSMutableArray *a = [[NSMutableArray alloc] initWithArray:ma];
		int iCount = 0;
		for(int i = 0; i < [a count]; i++)
		{
			EntitySegment *s = a[i];
			if([s isKindOfClass:[EntitySegment class]] && [s.type isEqualToString:@"HOTEL"])
			{
				//shove to the back
				[ma removeObjectAtIndex:i - iCount];
				[ma addObject:s];
				iCount++;
				//break;
			}
		}
	}

	NSArray *sortedKeys = [holdKeys sortedArrayUsingSelector:@selector(caseInsensitiveCompare:)];
	
	holdKeys = [[NSMutableArray alloc] init];
	int cnt = [sortedKeys count];
	for (int x = 0; x < cnt; x++)
	{
		NSString *sortedDate = sortedKeys[x]; 
		[holdKeys addObject:[DateTimeFormatter formatDateForTravel:sortedDate]];
	}
	
	return holdKeys;
}

#pragma mark - APIs for booking defaults
+(NSMutableDictionary*)getHotelAndCarDefaultsFromFlightInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys
{
	// If my trip begins with a two-legged flight: Seattle to Houston followed by Houston to Atlanta,
	// there will be two air segments, one for Seattle to Houston and another for Houston to Atlanta.
	// Both air segments will have a legId of "1"
	//
	// If my trip ends with a two-legged flight, Atlanta to Houston followed by Houston to Seattle,
	// there will be two air segments, one for Atlanta to Houston and another for Houston to Seattle.
	// Both air segments will have a legId of "2"
	//
	// legIds is an ordered array of leg ids, in this the first element is "1" and the second is "2".
	//
	// airSegementsForLegId is a dictionary whose key is the leg id, and whose value (for each key)
	// is an ordered array of air segments (who have that leg id)
	//
    
	NSMutableArray *legIds = [[NSMutableArray alloc] initWithObjects:nil];
	NSMutableDictionary *airSegmentsForLegId = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    NSMutableArray *airSegments = [[NSMutableArray alloc] init];
	
	for (NSString *key in keys)
	{
		NSArray *segments = segmentDict[key];
		
		for (NSObject *obj in segments)
		{
			if ([obj isKindOfClass:[EntitySegment class]])
			{
				EntitySegment* segment = (EntitySegment*)obj;
				
				// For each air segment,
				if ([segment.type isEqualToString:SEG_TYPE_AIR])
				{
                    if ([segment.legId length])
                    {
                        // Grab the legId.
                        NSString *legId = segment.legId;
                        
                        // Add the legId to the end of the legIds array (if it's not already at the end)
                        NSString* lastLegIdInLegIds = (NSString*)[legIds lastObject];
                        if (![lastLegIdInLegIds isEqualToString:legId])
                        {
                            [legIds addObject:legId];
                        }
                        
                        // Grab the air segments for the legId.
                        NSMutableArray* segmentsInLeg = airSegmentsForLegId[legId];
                        
                        // If it doesn't exist, create one.
                        if (segmentsInLeg == nil)
                        {
                            segmentsInLeg = [[NSMutableArray alloc] initWithObjects:nil];
                            airSegmentsForLegId[legId] = segmentsInLeg;
                        }
                        
                        // Add the current segment to the segment list.
                        [segmentsInLeg addObject:segment];
                    }
                    [airSegments addObject:segment];
				}
			}
		}
	}
    
	__autoreleasing NSMutableDictionary *params = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	if ([legIds count] == 1)
	{
		NSString* legId = legIds[0];
		NSArray* airSegments = airSegmentsForLegId[legId];
		[self addHotelAndCarLocationsToDictionary:params forSegment:[airSegments lastObject]];
	}
	else if ([legIds count] > 1)
	{
		NSString* firstLegId = legIds[0];
		NSArray* airSegmentsInFirstLeg = airSegmentsForLegId[firstLegId];
		EntitySegment *lastAirSegmentInFirstLeg = [airSegmentsInFirstLeg lastObject];
        
        NSString* lastLegId = [legIds lastObject];
		NSArray* airSegmentsInLastLeg = airSegmentsForLegId[lastLegId];
		EntitySegment *firstAirSegmentInLastLeg = airSegmentsInLastLeg[0];
        
        [self addHotelAndCarLocationsToDictionary:params forSegmentEnding:lastAirSegmentInFirstLeg andStartingSegment:firstAirSegmentInLastLeg];
	}
    else if ([airSegments count])
    {
        [self addHotelAndCarDefaultsTo:params fromAirSegments:airSegments];
    }
	
	return params;
}

+(void) addHotelAndCarLocationsToDictionary:(NSMutableDictionary *)params forSegment:(EntitySegment*)lastAirSegment
{
    NSString *airportAddress = [SegmentData getAirportFullAddress:lastAirSegment.relEndLocation];
    
    [self addHotelAndCarLocationsToDictionary:params address:airportAddress locationName:lastAirSegment.relEndLocation.airportName iataCode:lastAirSegment.relEndLocation.cityCode];
    
    params[@"DEFAULT_HOTEL_CHECKIN_DATE"] = lastAirSegment.relEndLocation.dateLocal;
    params[@"DEFAULT_CAR_PICKUP_DATE"] = lastAirSegment.relEndLocation.dateLocal;
    
    NSDate *checkinOrPickupDate = [DateTimeFormatter getLocalDate:lastAirSegment.relEndLocation.dateLocal];
    NSTimeInterval secondsInDay = 60.0 * 60.0 * 24.0; // 60 seconds per minute * 60 minutes per hour * 24 hours per day
    NSDate *checkoutOrDropoffDate = [NSDate dateWithTimeInterval:secondsInDay sinceDate:checkinOrPickupDate];
    NSString *checkoutOrDropoffDateAsString = [DateTimeFormatter getLocalDateAsString:checkoutOrDropoffDate];
    
    params[@"DEFAULT_HOTEL_CHECKOUT_DATE"] = checkoutOrDropoffDateAsString;
    params[@"DEFAULT_CAR_DROPOFF_DATE"] = checkoutOrDropoffDateAsString;
}

+(void) addHotelAndCarLocationsToDictionary:(NSMutableDictionary *)params forSegmentEnding:(EntitySegment*)lastAirSegmentInFirstLeg andStartingSegment:(EntitySegment*)firstAirSegmentInLastLeg
{
    NSString *endAirportAddress = [SegmentData getAirportFullAddress:lastAirSegmentInFirstLeg.relEndLocation];
    
    NSString *loName = [SegmentData getAirportNameCode:lastAirSegmentInFirstLeg.relEndLocation];
    //Logan Intl Arpt, Boston, MA (BOS)
    [self addHotelAndCarLocationsToDictionary:params address:endAirportAddress locationName:loName iataCode:lastAirSegmentInFirstLeg.relEndLocation.cityCode];//lastAirSegmentInFirstLeg.endAirportName];
    
    params[@"DEFAULT_HOTEL_CHECKIN_DATE"] = lastAirSegmentInFirstLeg.relEndLocation.dateLocal;
    params[@"DEFAULT_CAR_PICKUP_DATE"] = lastAirSegmentInFirstLeg.relEndLocation.dateLocal;
    params[@"DEFAULT_HOTEL_CHECKOUT_DATE"] = firstAirSegmentInLastLeg.relStartLocation.dateLocal;
    params[@"DEFAULT_CAR_DROPOFF_DATE"] = firstAirSegmentInLastLeg.relStartLocation.dateLocal;
}

+(void)addHotelAndCarDefaultsTo:(NSMutableDictionary *)params fromAirSegments:(NSMutableArray *)airSegments
{
    if ([airSegments count] == 1) {
        [self addHotelAndCarLocationsToDictionary:params forSegment:[airSegments lastObject]];
    }
    else if ([airSegments count] > 1)
    {
        // ^(id a, id b){ return [a compare:b options:NSNumericSearch];}
        [airSegments sortUsingComparator:^(id obj1, id obj2) {
            EntitySegment *s1 = (EntitySegment*)obj1;
            EntitySegment *s2 = (EntitySegment*)obj2;
            return [[DateTimeFormatter getLocalDate:s1.relStartLocation.dateUtc] compare:[DateTimeFormatter getLocalDate:s2.relStartLocation.dateUtc]];
        }];
        int index = 1;
        NSTimeInterval duration = 0;
        for (int i = 1; i < [airSegments count]; i++)
        {
            if (airSegments[i])
            {
                EntitySegment *s1 = (EntitySegment*)airSegments[i-1];
                EntitySegment *s2 = (EntitySegment*)airSegments[i];
                NSTimeInterval durationSinceLastSegment = [[DateTimeFormatter getLocalDate:s2.relStartLocation.dateUtc] timeIntervalSinceDate:[DateTimeFormatter getLocalDate:s1.relEndLocation.dateUtc]];
                if (durationSinceLastSegment > duration)
                {
                    index = i;
                    duration = durationSinceLastSegment;
                }
            }
        }
        [self addHotelAndCarLocationsToDictionary:params forSegmentEnding:airSegments[index-1] andStartingSegment:airSegments[index]];
    }
}

+(NSMutableDictionary*)getHotelAndCarDefaultsFromRailInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys
{
	__autoreleasing NSMutableDictionary *params = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	EntitySegment* firstRailSegment = [TripData getFirstSegmentOfSegmentType:SEG_TYPE_RAIL inTripSegments:segmentDict withKeys:keys];
	if (firstRailSegment != nil)
	{
		NSString *locationName = firstRailSegment.relEndLocation.railStationLocalized;
		NSString *address = (locationName == nil ? firstRailSegment.relEndLocation.railStation : [NSString stringWithFormat:@"%@, %@", locationName, firstRailSegment.relEndLocation.railStation]);
		
		[self addHotelAndCarLocationsToDictionary:params address:address locationName:(locationName != nil ? locationName : address) iataCode:nil];
		
		params[@"DEFAULT_HOTEL_CHECKIN_DATE"] = firstRailSegment.relEndLocation.dateLocal;
		params[@"DEFAULT_CAR_PICKUP_DATE"] = firstRailSegment.relEndLocation.dateLocal;
	}
	
	EntitySegment* lastRailSegment = [TripData getLastSegmentOfSegmentType:SEG_TYPE_RAIL inTripSegments:segmentDict withKeys:keys];
	if (lastRailSegment != nil)
	{
		NSString *locationName = lastRailSegment.relStartLocation.railStationLocalized;
		NSString *address = (locationName == nil ? lastRailSegment.relStartLocation.railStation : [NSString stringWithFormat:@"%@, %@", locationName, lastRailSegment.relStartLocation.railStation]);
		
		[self addHotelAndCarLocationsToDictionary:params address:address locationName:(locationName != nil ? locationName : address) iataCode:nil];
		
		params[@"DEFAULT_HOTEL_CHECKOUT_DATE"] = lastRailSegment.relStartLocation.dateLocal;
		params[@"DEFAULT_CAR_DROPOFF_DATE"] = lastRailSegment.relStartLocation.dateLocal;
	}
    
	return params;
}

+(NSMutableDictionary*)getHotelAndCarDefaultsFromHotelInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys
{
	__autoreleasing NSMutableDictionary *params = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	EntitySegment* segment = [TripData getFirstSegmentOfSegmentType:SEG_TYPE_HOTEL inTripSegments:segmentDict withKeys:keys];
	if (segment != nil)
	{
		NSString *address = [SegmentData getMapAddress:segment.relStartLocation];
		
		NSString *locationName = [SegmentData getCityState:segment.relStartLocation];
		
		[self addHotelAndCarLocationsToDictionary:params address:address locationName:(locationName != nil ? locationName : address) iataCode:nil];
		
		params[@"DEFAULT_HOTEL_CHECKIN_DATE"] = segment.relStartLocation.dateLocal;
		params[@"DEFAULT_CAR_PICKUP_DATE"] = segment.relStartLocation.dateLocal;
		
		params[@"DEFAULT_HOTEL_CHECKOUT_DATE"] = segment.relEndLocation.dateLocal;
		params[@"DEFAULT_CAR_DROPOFF_DATE"] = segment.relEndLocation.dateLocal;
        
	}
	
	return params;
}

+(NSMutableDictionary*)getHotelAndCarDefaultsFromCarInTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys
{
	__autoreleasing NSMutableDictionary *params = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
	
	EntitySegment* segment = [TripData getFirstSegmentOfSegmentType:SEG_TYPE_CAR inTripSegments:segmentDict withKeys:keys];
	if (segment != nil)
	{
		NSString *address = [SegmentData getAirportFullAddress:segment.relEndLocation];
		
		NSString *locationName = @"";
		if (segment.relStartLocation.airportCity != nil)
			locationName = [locationName stringByAppendingFormat:@"%@", segment.relStartLocation.airportCity];
		if (segment.relStartLocation.airportState != nil)
			locationName = [locationName stringByAppendingFormat:@", %@", segment.relStartLocation.airportState];
		
		[self addHotelAndCarLocationsToDictionary:params address:address locationName:(locationName != nil ? locationName : address) iataCode:nil];
		
		params[@"DEFAULT_HOTEL_CHECKIN_DATE"] = segment.relStartLocation.dateLocal;
		params[@"DEFAULT_CAR_PICKUP_DATE"] = segment.relStartLocation.dateLocal;
		
		params[@"DEFAULT_HOTEL_CHECKOUT_DATE"] = segment.relEndLocation.dateLocal;
		params[@"DEFAULT_CAR_DROPOFF_DATE"] = segment.relEndLocation.dateLocal;
	}
	
	return params;
}

+(void)addHotelAndCarLocationsToDictionary:(NSMutableDictionary*)dictionary address:(NSString*)address locationName:(NSString*)locationName iataCode:(NSString *)iataCode
{
	NSString *latitude = nil;
	NSString *longitude = nil;
	
	if ([address length] > 0)
	{
        //address
		Location* loc = [MapViewController requestLocationByAddress:locationName];
		if(![iataCode isEqualToString:@"BOS"])
        {
            longitude = loc.location.longitude;
            latitude = loc.location.latitude;
        }
        else
        {
            latitude = @"42.36362935";
            longitude = @"-71.01003923";
        }
		
		dictionary[@"DEFAULT_HOTEL_LOCATION"] = locationName;
		dictionary[@"DEFAULT_CAR_PICKUP_LOCATION"] = locationName;
		
		if (latitude != nil && longitude != nil)
		{
			dictionary[@"DEFAULT_HOTEL_LATITUDE"] = latitude;
			dictionary[@"DEFAULT_HOTEL_LONGITUDE"] = longitude;
			
			dictionary[@"DEFAULT_CAR_PICKUP_LATITUDE"] = latitude;
			dictionary[@"DEFAULT_CAR_PICKUP_LONGITUDE"] = longitude;
		}
        
        if ([iataCode length])
        {
            dictionary[@"DEFAULT_CAR_PICKUP_IATA"] = iataCode;
        }
	}
}

+(EntitySegment*)getFirstSegmentOfSegmentType:(NSString*)segmentType inTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;
{
	for (NSString *key in keys)
	{
		NSArray *segments = segmentDict[key];
		
		for (NSObject *obj in segments)
		{
			if ([obj isKindOfClass:[EntitySegment class]])
			{
				EntitySegment* segment = (EntitySegment*)obj;
				
				if ([segment.type isEqualToString:segmentType])
				{
					return segment;
				}
			}
		}
	}
	return nil;
}

+(EntitySegment*)getLastSegmentOfSegmentType:(NSString*)segmentType inTripSegments:(NSDictionary*)segmentDict withKeys:(NSArray*)keys;
{
	EntitySegment *lastSegment = nil;
	
	for (NSString *key in keys)
	{
		NSArray *segments = segmentDict[key];
		
		for (NSObject *obj in segments)
		{
			if ([obj isKindOfClass:[EntitySegment class]])
			{
				EntitySegment* segment = (EntitySegment*)obj;
				
				if ([segment.type isEqualToString:segmentType])
				{
					lastSegment = segment;
				}
			}
		}
	}
	return lastSegment;
}


#pragma mark - Just give me the segments
+(NSArray*) makeSegmentsArray:(EntityTrip*) trip
{
    NSDictionary *dictDateSegs = [self makeSegmentDictGroupedByDate:trip];
    NSArray *aDateKeys = [self makeSegmentArrayGroupedByDate:trip];
    
    __autoreleasing NSMutableArray *aAllSegments = [[NSMutableArray alloc] initWithObjects: nil];
    
    for(int i = 0; i < [aDateKeys count]; i++)
    {
        NSString *dateKey = aDateKeys[i];
        NSArray *aSegments = dictDateSegs[dateKey];
        // Offers are added into the sorted dictionary
        for(NSObject *obj in aSegments)
        {
            if([obj isKindOfClass:[EntityOffer class]])
            {
                EntityOffer *offer = (EntityOffer *)obj;
                [aAllSegments addObject:offer];
            }
            else
            {
                EntitySegment *seg = (EntitySegment *)obj;
                [aAllSegments addObject:seg];
            }
    
        }
    }
    
    return aAllSegments;
}


+(EntityBooking*)getPrimaryBooking :(EntityTrip *) trip
{
	for (EntityBooking* eachBooking in trip.relBooking)
	{
		if ([eachBooking.isCliqbookSystemOfRecord boolValue])
			return eachBooking;
	}
	return nil;
}


-(BOOL) isWaitingForApproval
{
    return state == 101; // MOB-7341 We only handle 101 for now
}

+ (BOOL) isFlightDelayedOrCancelled:(EntityFlightStats *) flightStat
{
    return [flightStat.departureShortStatus isEqualToString:@"DY"] || [flightStat.departureShortStatus isEqualToString:@"Delayed"]
    || [flightStat.departureShortStatus isEqualToString:@"Cancelled"];
}

-(void)setFromEntityTrip:(EntityTrip*) oneTrip
{
    //MOB-10675
    //Extract necessary info from EntityTrip to TripData for displaying current active trip
    NSString *stringTripStartDateLocal = nil;
    NSString *stringTripEndDateLocal = nil;
    
    self.tripKey = oneTrip.tripKey;
    self.itinSourceName = oneTrip.itinSourceName;
    self.itinLocator = oneTrip.itinLocator;
    self.tripName = oneTrip.tripName;
    
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss"];
    stringTripStartDateLocal = [dateFormatter stringFromDate:oneTrip.tripStartDateLocal];
    stringTripEndDateLocal = [dateFormatter stringFromDate:oneTrip.tripEndDateLocal];
    
    self.tripStartDateLocal = stringTripStartDateLocal;
    self.tripEndDateLocal = stringTripEndDateLocal;
    self.cliqbookTripId = oneTrip.cliqbookTripId;
    self.clientLocator = oneTrip.clientLocator;

    self.hasAir = [oneTrip.hasAir boolValue];
    self.hasRail = [oneTrip.hasRail boolValue];
    self.hasEvent = [oneTrip.hasEvent boolValue];
    self.hasDining = [oneTrip.hasDining boolValue];
    self.hasCar = [oneTrip.hasCar boolValue];
    self.hasRide = [oneTrip.hasRide boolValue];
    self.hasHotel = [oneTrip.hasHotel boolValue];
    self.hasParking = [oneTrip.hasParking boolValue];

    self.isExpensed = [oneTrip.isExpensed boolValue];
    
    if (self.booking != nil) {
        [self.bookingKeys addObject:oneTrip.bookingId];
        (self.bookings)[oneTrip.bookingId] = self.booking;
    }
}
@end
