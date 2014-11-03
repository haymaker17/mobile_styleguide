//
//  TravelRequest.m
//  ConcurMobile
//
//  Created by laurent mery on 18/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "TravelRequest.h"
#import "ExSystem.h"
#import "CCDateUtilities.h"

@implementation TravelRequest

+(NSDictionary*)contextTravelRequest:(NSDictionary*)tvrInfos{
	
	ExSystem *exSystem = [ExSystem sharedInstance];
	
	NSDictionary *context = @{
							  //currentUser/Role
							  @"hasTravelRequest"			:[exSystem hasTravelRequest] ? @"Y" : @"N",
							  @"isReaquestUser"				:[exSystem isRequestUser] ? @"Y" : @"N",
							  @"isRequestApprover"			:[exSystem isRequestApprover] ? @"Y" : @"N",
							  @"isRequestProcessor1"		:@"TODO:isRequestProcessor1",
							  @"isRequestProcessor2"		:@"TODO:isRequestProcessor2",
							  @"isRequestProcessor3"		:@"TODO:isRequestProcessor3",
							  @"isRiskProcessor"			:@"TODO:isRiskProcessor",
							  //  @"currentEmpKey"				:[exSystem.cacheData.cacheDict objectForKey:@"userId"],
							  @"currentWsiEmpKey"			:@"TODO1",
							  //tvr context
							  @"entranceScreen"				:[tvrInfos objectForKey:@"entranceScreen"],
							  @"currentScreen"				:[tvrInfos objectForKey:@"currentScreen"],
							  @"hasKey"						:[tvrInfos objectForKey:@"hasKey"],
							  //workflow
							  @"approvalStatusCode"			:[tvrInfos objectForKey:@"approvalStatusCode"],
							  @"WsiEmpKey"					:@"TODO2", //tvrInfos
							  @"stepRoleCode"				:@"TODO3" //tvrInfos
							  
							  //TODO: need more step workflow infos
							  //wsiEmpKey (currentUser & tvr), stepRoleCode
							  //startForm??
							  };
	
	return context;
}

/**
 webService could provide date with format Mdyyyy or YYYYMMddTHHmmss
 */

+(NSString*)formatedDateMdyyyy:(NSString*)value withTemplate:(NSString*)template{
	
	NSString *date = @"";
	if (value != nil) {
		
		NSDateFormatter *dateFormatter = [NSDateFormatter dateFormatterWithFormat:@"M/d/yyyy" timeZoneWithAbbreviation:@"GMT" locale:[NSLocale currentLocale]];
		NSDate *nsdate = [dateFormatter dateFromString:value];
		date = [CCDateUtilities formatDate:nsdate withTemplate:template localisedOrNil:nil];
	}
	
	return date;
}


+(NSString*)formatedDateYYYYMMddTHHmmss:(NSString*)value withTemplate:(NSString*)template{
	
	NSString *date = @"";
	if (value != nil && ![@"" isEqualToString:value]) {
		
		NSDate *nsdate = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:value];
		date = [CCDateUtilities formatDate:nsdate withTemplate:template localisedOrNil:nil];
	}
	
	return date;
}


+(NSString*)formatAmount:(NSString*)value withCurrency:(NSString*)crnCode{
	
	if (value == nil || [@"" isEqualToString:value]) {
		
		value = @"0";
	}
	
	NSNumber *numberAmount = [NSNumber numberWithDouble:[value doubleValue]];
	NSString *amount = [FormatUtils formatMoneyWithNumber:numberAmount crnCode:crnCode];
	
	return amount;
}

@end
