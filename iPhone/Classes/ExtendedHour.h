//
//  ExtendedHour.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

const extern NSInteger AnytimeExtendedHour;
const extern NSInteger MorningExtendedHour;
const extern NSInteger AfternoonExtendedHour;
const extern NSInteger EveningExtendedHour;
const extern NSInteger MidnightExtendedHour;

@interface ExtendedHour : NSObject
{
}

+(NSInteger)getHourFromExtendedHour:(NSInteger)extendedHour;
+(NSString*)getLocalizedTextForExtendedHour:(NSInteger)extendedHour;

@end
