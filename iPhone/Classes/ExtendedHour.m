//
//  ExtendedHour.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExtendedHour.h"
#import "DateTimeFormatter.h"

@implementation ExtendedHour

const NSInteger AnytimeExtendedHour			= 24;
const NSInteger MorningExtendedHour			= 25;
const NSInteger AfternoonExtendedHour		= 26;
const NSInteger EveningExtendedHour			= 27;
const NSInteger MidnightExtendedHour		= 28;

#define Anytime_Hour		 1
#define Morning_Hour		 5
#define Afternoon_Hour		14
#define Evening_Hour		18
#define	Midnight_Hour		 1

+(NSInteger)getHourFromExtendedHour:(NSInteger)extendedHour
{
	if (extendedHour < 24)
		return extendedHour;
	else if (extendedHour == AnytimeExtendedHour)
		return Anytime_Hour;
	else if (extendedHour == MorningExtendedHour)
		return Morning_Hour;
	else if (extendedHour == AfternoonExtendedHour)
		return Afternoon_Hour;
	else if (extendedHour == EveningExtendedHour)
		return Evening_Hour;
	else if (extendedHour == MidnightExtendedHour)
		return Midnight_Hour;
	
	return 1;
}

+(NSString*)getLocalizedTextForExtendedHour:(NSInteger)extendedHour
{
	if (extendedHour < 24)
		return [DateTimeFormatter formatHour:extendedHour];
		//return [NSString stringWithFormat:@"%i", extendedHour];
	else if (extendedHour == AnytimeExtendedHour)
		return [Localizer getLocalizedText:@"Anytime"];
	else if (extendedHour == MorningExtendedHour)
		return [Localizer getLocalizedText:@"Morning"];
	else if (extendedHour == AfternoonExtendedHour)
		return [Localizer getLocalizedText:@"Afternoon"];
	else if (extendedHour == EveningExtendedHour)
		return [Localizer getLocalizedText:@"Evening"];
	else if (extendedHour == MidnightExtendedHour)
		return [Localizer getLocalizedText:@"Midnight"];
	
	return [ExtendedHour getLocalizedTextForExtendedHour:[ExtendedHour getHourFromExtendedHour:extendedHour]];
}


@end
