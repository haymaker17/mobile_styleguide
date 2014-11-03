//
//  TrainDateTimeVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainDateVC.h"
#import "DateTimeConverter.h"

@implementation TrainDateVC

@synthesize bcdDate;
@synthesize bcdTime;
@synthesize parentVC;

- (void)populateDate:(BookingCellData*)bcdDt time:(BookingCellData*)bcdTm
{
	self.bcdDate = bcdDt;
	self.bcdTime = bcdTm;
	//NSDate *localDateWithSameComponentsAsGmtDate = [DateTimeConverter localDateWithSameComponentsAsGmtDate:bcdDt.dateValue];
	self.initialDate = bcdDt.dateValue;//  localDateWithSameComponentsAsGmtDate;
	self.initialExtendedHour = bcdTm.extendedTime;
}

-(NSString*)getTitleLabel
{
	NSString *key = (isReturn ? @"Returning" : @"Departing");
	return [Localizer getLocalizedText:key];
}

-(NSString*)getDepartureDateLabel
{
	NSString *key = (isReturn ? @"Return Date" : @"Departure Date");
	return [Localizer getLocalizedText:key];
}

-(NSString*)getDepartureTimeLabel
{
	NSString *key = (isReturn ? @"Return Time" : @"Departure Time");
	return [Localizer getLocalizedText:key];
}

-(void) onDone
{
	//NSDate* gmtDateWithSameComponentsAsLocalDate = [DateTimeConverter gmtDateWithSameComponentsAsLocalDate:datePicker.date];

	bcdDate.val = [DateTimeFormatter formatDateForBooking:datePicker.date];
	bcdDate.dateValue = datePicker.date;
	
	bcdDate.extendedTime = selectedExtendedHour;
    bcdTime.extendedTime = selectedExtendedHour;
    bcdTime.val = [ExtendedHour getLocalizedTextForExtendedHour:bcdTime.extendedTime];
	//bcdDate.val = [ExtendedHour getLocalizedTextForExtendedHour:selectedExtendedHour];
	
	if(!isReturn && dateDiff >= 0 && parentVC.isRoundTrip)
	{
		//NSMutableArray *sectionValues = [parentVC.aList objectAtIndex:1];
		BookingCellData *bcdReturnDate = [parentVC getBCD:@"ReturnDate"];// [sectionValues objectAtIndex:2];
		bcdReturnDate.dateValue = [self addDaysToDate:datePicker.date NumDaysToAdd:dateDiff];
        
        if(bcdDate.extendedTime > bcdReturnDate.extendedTime)
            bcdReturnDate.extendedTime = bcdDate.extendedTime;

		bcdReturnDate.val = [DateTimeFormatter formatDateForBooking:bcdReturnDate.dateValue];

	}
	
	[super onDone];
}



@end

