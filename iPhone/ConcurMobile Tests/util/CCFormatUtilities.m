//
//  CCFormatUtilities.m
//  ConcurMobile
//
//  Created by laurent mery on 02/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <XCTest/XCTest.h>
#import "CCFormatUtilities.h"


@interface CCFormatUtilitiesTests : XCTestCase

@end


@implementation CCFormatUtilitiesTests


-(void)testFormatDatesProvideByWebnService{
	
	/*
	+(NSDate*)dateWithMdyyyy:(NSString*)Mdyyyy;
	+(NSDate*)dateWithYYYYMMddTHHmmss:(NSString*)YYYYMMddTHHmmss;
	+(NSDate*)dateWithYYYYMMddHHmmss:(NSString*)YYYYMMddHHmmss;
	 */
	
	NSDate *nsDate;
	NSString *hashToBeTested, *hash;
	
	NSDictionary *hash_input = @{
								 @"429580800":	@"8/13/2014"
								 ,@"441763200":	@"1/1/2015"
								 ,@"473212800":	@"12/31/2015"
								 ,@"352166400":	@"2/29/2012"
								 ,@"0":			@"2/29/2014"  // << nil
								 };

	for(hash in hash_input){
		
		nsDate = [CCFormatUtilities dateWithMdyyyy:[hash_input objectForKey:hash]];
		
		hashToBeTested = [NSString stringWithFormat:@"%lu", (unsigned long)nsDate.hash];
		XCTAssertTrue([hashToBeTested isEqualToString:hash], @"* source is %@ (hash: %@ ; hash expected:%@)  NsDate returned id %@", [hash_input objectForKey:hash], hashToBeTested, hash, nsDate);
	}
	
	
	NSDictionary *hash_input2 = @{
				   @"429667199":	@"2014-08-13T23:59:59"
				   ,@"441763201":	@"2015-01-01T00:00:01"
				   ,@"473212800":	@"2015-12-31T00:00:00"
				   ,@"352170000":	@"2012-02-29T01:00:00"
				   ,@"0":			@"2014-02-29T00:00:00"  // << nil
				   };
	
	for(hash in hash_input2){
		
		nsDate = [CCFormatUtilities dateWithYYYYMMddTHHmmss:[hash_input2 objectForKey:hash]];

		hashToBeTested = [NSString stringWithFormat:@"%lu", (unsigned long)nsDate.hash];
		XCTAssertTrue([hashToBeTested isEqualToString:hash], @"* source is %@ (hash: %@ ; hash expected:%@)  NsDate returned id %@", [hash_input2 objectForKey:hash], hashToBeTested, hash, nsDate);
	}
	
	
	
	NSDictionary *hash_input3 = @{
				   @"429667199":	@"2014-08-13 23:59:59.0"
				   ,@"441763201":	@"2015-01-01 00:00:01.0"
				   ,@"473212800":	@"2015-12-31 00:00:00.0"
				   ,@"352170000":	@"2012-02-29 01:00:00.0"
				   ,@"0":			@"2014-02-29 00:00:00.0"  // << nil
				   };

	
	for(hash in hash_input3){
		
		
		nsDate = [CCFormatUtilities dateWithYYYYMMddHHmmss:[hash_input3 objectForKey:hash]];
		
		NSString *hashToBeTested = [NSString stringWithFormat:@"%lu", (unsigned long)nsDate.hash];
		XCTAssertTrue([hashToBeTested isEqualToString:hash], @"* source is %@ (hash: %@ ; hash expected:%@)  NsDate returned id %@", [hash_input3 objectForKey:hash], hashToBeTested, hash, nsDate);
	}

	
	
	
	NSDictionary *hash_input4 = @{
								  @"4263420136":		@"8:54 PM"
								  ,@"4263345016":	@"12:02 AM"
								  ,@"4263344956":	@"0:01 AM"
								  ,@"4263409636":	@"5:59 PM"
								  ,@"4263431236":	@"11:59 PM"
								  };
	
	
	for(hash in hash_input4){
		
		
		nsDate = [CCFormatUtilities dateWithHma:[hash_input4 objectForKey:hash]];
		
		NSString *hashToBeTested = [NSString stringWithFormat:@"%lu", (unsigned long)nsDate.hash];
		XCTAssertTrue([hashToBeTested isEqualToString:hash], @"* source is %@ (hash: %@ ; hash expected:%@)  NsDate returned id %@", [hash_input4 objectForKey:hash], hashToBeTested, hash, nsDate);
	}

	
	//+(NSDate*)dateWithHma:(NSString*)hma
}

-(void) testFormatDateWithTemplateLocalisedOrNil {
	
	NSDictionary *locales = @{
							  @"en_US": [[NSLocale alloc]initWithLocaleIdentifier:@"en_US"],
							  @"fr_FR": [[NSLocale alloc]initWithLocaleIdentifier:@"fr_FR"],
							  @"ja_JP": [[NSLocale alloc]initWithLocaleIdentifier:@"ja_JP"]};
	
	//template ddMMyyyy
	NSDictionary *datasShortFormatDictionnary = @{// new years eve and new years day
												  @"1995-01-01T23:59:59": @{
														  @"en_US": @"01/01/1995",
														  @"fr_FR": @"01/01/1995",
														  @"ja_JP": @"1995/01/01"},
												  @"2014-01-01T00:00:00": @{
														  @"en_US": @"01/01/2014",
														  @"fr_FR": @"01/01/2014",
														  @"ja_JP": @"2014/01/01"},
												  @"2013-12-31T00:00:00": @{
														  @"en_US": @"12/31/2013",
														  @"fr_FR": @"31/12/2013",
														  @"ja_JP": @"2013/12/31"},
												  // a leap year
												  @"2012-02-29T00:00:00": @{
														  @"en_US": @"02/29/2012",
														  @"fr_FR": @"29/02/2012",
														  @"ja_JP": @"2012/02/29"},
												  
												  @"2014-01-01T11:59:59": @{
														  @"en_US": @"01/01/2014",
														  @"fr_FR": @"01/01/2014",
														  @"ja_JP": @"2014/01/01"},
												  @"2013-12-31T23:59:59": @{
														  @"en_US": @"12/31/2013",
														  @"fr_FR": @"31/12/2013",
														  @"ja_JP": @"2013/12/31"},
												  
												  @"2014-11-11T11:11:11": @{
														  @"en_US": @"11/11/2014",
														  @"fr_FR": @"11/11/2014",
														  @"ja_JP": @"2014/11/11"},
												  @"2013-06-06T01:00:00": @{
														  @"en_US": @"06/06/2013",
														  @"fr_FR": @"06/06/2013",
														  @"ja_JP": @"2013/06/06"},
												  
												  @"2014-07-06T00:00:00": @{
														  @"en_US": @"07/06/2014",
														  @"fr_FR": @"06/07/2014",
														  @"ja_JP": @"2014/07/06"},
												  @"2013-07-06T00:00:00": @{
														  @"en_US": @"07/06/2013",
														  @"fr_FR": @"06/07/2013",
														  @"ja_JP": @"2013/07/06"}};
	
	//template dMMM
	NSDictionary *datasShortLiterralFormatDictionnary = @{// new years eve and new years day
														  @"1995-01-01T23:59:59": @{
																  @"en_US": @"Jan 1",
																  @"fr_FR": @"1 janv.",
																  @"ja_JP": @"1月1日"},
														  @"2014-01-01T00:00:00": @{
																  @"en_US": @"Jan 1",
																  @"fr_FR": @"1 janv.",
																  @"ja_JP": @"1月1日"},
														  @"2013-12-31T00:00:00": @{
																  @"en_US": @"Dec 31",
																  @"fr_FR": @"31 déc.",
																  @"ja_JP": @"12月31日"},
														  // a leap year
														  @"2012-02-29T00:00:00": @{
																  @"en_US": @"Feb 29",
																  @"fr_FR": @"29 févr.",
																  @"ja_JP": @"2月29日"},
														  
														  @"2014-01-01T11:59:59": @{
																  @"en_US": @"Jan 1",
																  @"fr_FR": @"1 janv.",
																  @"ja_JP": @"1月1日"},
														  @"2013-12-31T23:59:59": @{
																  @"en_US": @"Dec 31",
																  @"fr_FR": @"31 déc.",
																  @"ja_JP": @"12月31日"},
														  
														  @"2014-11-11T11:11:11": @{
																  @"en_US": @"Nov 11",
																  @"fr_FR": @"11 nov.",
																  @"ja_JP": @"11月11日"},
														  @"2013-06-06T01:00:00": @{
																  @"en_US": @"Jun 6",
																  @"fr_FR": @"6 juin",
																  @"ja_JP": @"6月6日"},
														  
														  @"2014-07-06T00:00:00": @{
																  @"en_US": @"Jul 6",
																  @"fr_FR": @"6 juil.",
																  @"ja_JP": @"7月6日"},
														  @"2013-07-06T00:00:00": @{
																  @"en_US": @"Jul 6",
																  @"fr_FR": @"6 juil.",
																  @"ja_JP": @"7月6日"}
														  };
	
	//template eeeeddMMMMyyyy
	NSDictionary *datasLongLiterralFormatDictionnary = @{// new years eve and new years day
														 @"1995-01-01T23:59:59": @{
																 @"en_US": @"Sunday, January 01, 1995",
																 @"fr_FR": @"dimanche 01 janvier 1995",
																 @"ja_JP": @"1995年1月01日日曜日"},
														 @"2014-01-01T00:00:00": @{
																 @"en_US": @"Wednesday, January 01, 2014",
																 @"fr_FR": @"mercredi 01 janvier 2014",
																 @"ja_JP": @"2014年1月01日水曜日"},
														 @"2013-12-31T00:00:00": @{
																 @"en_US": @"Tuesday, December 31, 2013",
																 @"fr_FR": @"mardi 31 décembre 2013",
																 @"ja_JP": @"2013年12月31日火曜日"},
														 // a leap year
														 @"2012-02-29T00:00:00": @{
																 @"en_US": @"Wednesday, February 29, 2012",
																 @"fr_FR": @"mercredi 29 février 2012",
																 @"ja_JP": @"2012年2月29日水曜日"},
														 
														 @"2014-01-01T11:59:59": @{
																 @"en_US": @"Wednesday, January 01, 2014",
																 @"fr_FR": @"mercredi 01 janvier 2014",
																 @"ja_JP": @"2014年1月01日水曜日"},
														 @"2013-12-31T23:59:59": @{
																 @"en_US": @"Tuesday, December 31, 2013",
																 @"fr_FR": @"mardi 31 décembre 2013",
																 @"ja_JP": @"2013年12月31日火曜日"},
														 
														 @"2014-11-11T11:11:11": @{
																 @"en_US": @"Tuesday, November 11, 2014",
																 @"fr_FR": @"mardi 11 novembre 2014",
																 @"ja_JP": @"2014年11月11日火曜日"},
														 @"2013-06-06T01:00:00": @{
																 @"en_US": @"Thursday, June 06, 2013",
																 @"fr_FR": @"jeudi 06 juin 2013",
																 @"ja_JP": @"2013年6月06日木曜日"},
														 
														 @"2014-07-06T00:00:00": @{
																 @"en_US": @"Sunday, July 06, 2014",
																 @"fr_FR": @"dimanche 06 juillet 2014",
																 @"ja_JP": @"2014年7月06日日曜日"},
														 @"2013-07-06T00:00:00": @{
																 @"en_US": @"Saturday, July 06, 2013",
																 @"fr_FR": @"samedi 06 juillet 2013",
																 @"ja_JP": @"2013年7月06日土曜日"}
														 };
	
	//define templates
	NSDictionary *templates = @{
								@"ddMMyyyy": datasShortFormatDictionnary
								,@"dMMM": datasShortLiterralFormatDictionnary
								,@"eeeeddMMMMyyyy": datasLongLiterralFormatDictionnary
								};
	
	NSDate *nsDate;
	NSString *valueToBeTested;
	NSString *comparator;
	NSString *pathKey;
	NSDictionary *datas;
	
	NSString *formatDateInput = @"yyyy-MM-dd'T'HH:mm:ss";
	NSDateFormatter *formatterInput = [[NSDateFormatter alloc]init];
	[formatterInput setTimeZone: [NSTimeZone timeZoneWithAbbreviation:@"GMT"]]; //important!
	[formatterInput setDateFormat:formatDateInput];
	
	//run all tests
	for (id template in templates){
		
		
		datas = [templates objectForKey:template];
		
		for (id key in datas){
			
			for (id id_localIdentifier in [datas objectForKey:key]) {
				
				//prepare assert operation
				pathKey = [NSString stringWithFormat:@"%@.%@",key, id_localIdentifier];
				comparator = [datas valueForKeyPath:pathKey];
				
				//convert NSString to NSDate (prepare parameters to test CCDateUtilities method)
				nsDate = [formatterInput dateFromString:key];
				
				//******************************************************************************
				//start test CCDateUtilities method **************************************************
				
				valueToBeTested = [CCFormatUtilities formatedDate:nsDate withTemplate:template localisedOrNil:[locales objectForKey:id_localIdentifier]];
				
				XCTAssertTrue([valueToBeTested isEqualToString:comparator], @"* %@-%@: actual output %@ does not match expected output %@ for input source %@", template, id_localIdentifier, valueToBeTested, comparator, key);
				
				//end Test *********************************************************************
				//******************************************************************************
			}
		}
	}
}



-(void) testFormatTimeWithTemplateLocalisedOrNil {
	
	NSDictionary *locales = @{
							  @"en_US": [[NSLocale alloc]initWithLocaleIdentifier:@"en_US"],
							  @"fr_FR": [[NSLocale alloc]initWithLocaleIdentifier:@"fr_FR"],
							  @"ja_JP": [[NSLocale alloc]initWithLocaleIdentifier:@"ja_JP"]};
	
	//====================== input 24 HH:mm
	
	//template output Hm or HHm -> same result
	NSDictionary *datas24LongTimeDictionnary = @{// new years eve and new years day
												  @"15:38": @{
														  @"en_US": @"15:38",
														  @"fr_FR": @"15:38",
														  @"ja_JP": @"15:38"},
												  @"23:59": @{
														  @"en_US": @"23:59",
														  @"fr_FR": @"23:59",
														  @"ja_JP": @"23:59"},
												  @"00:01": @{
														  @"en_US": @"00:01",
														  @"fr_FR": @"00:01",
														  @"ja_JP": @"0:01"},
												  // a leap year
												  @"12:00": @{
														  @"en_US": @"12:00",
														  @"fr_FR": @"12:00",
														  @"ja_JP": @"12:00"}};

	//template output hma
	NSDictionary *datas12MixedTimeDictionnary = @{// new years eve and new years day
												  @"15:38": @{
														  @"en_US": @"3:38 PM",
														  @"fr_FR": @"3:38 PM",
														  @"ja_JP": @"午後3:38"},
												  @"23:59": @{
														  @"en_US": @"11:59 PM",
														  @"fr_FR": @"11:59 PM",
														  @"ja_JP": @"午後11:59"},
												  @"00:01": @{
														  @"en_US": @"12:01 AM",
														  @"fr_FR": @"12:01 AM",
														  @"ja_JP": @"午前0:01"},
												  // a leap year
												  @"12:00": @{
														  @"en_US": @"12:00 PM",
														  @"fr_FR": @"12:00 PM",
														  @"ja_JP": @"午後0:00"}};
	
	
	//define templates
	NSDictionary *templates = @{
								//@"Hm": datas24LongTimeDictionnary
								@"hma": datas12MixedTimeDictionnary
								};
	
	NSDate *nsDate;
	NSString *valueToBeTested;
	NSString *comparator;
	NSString *pathKey;
	NSDictionary *datas;
	
	NSString *formatDateInput = @"HH:mm";
	NSDateFormatter *formatterInput = [[NSDateFormatter alloc]init];
	[formatterInput setTimeZone: [NSTimeZone timeZoneWithAbbreviation:@"GMT"]]; //important!
	[formatterInput setDateFormat:formatDateInput];
	
	//run all tests
	for (id template in templates){
		
		
		datas = [templates objectForKey:template];
		
		for (id key in datas){
			
			for (id id_localIdentifier in [datas objectForKey:key]) {
				
				//prepare assert operation
				pathKey = [NSString stringWithFormat:@"%@.%@",key, id_localIdentifier];
				comparator = [datas valueForKeyPath:pathKey];
				
				//convert NSString to NSDate (prepare parameters to test CCDateUtilities method)
				nsDate = [formatterInput dateFromString:key];
				
				//******************************************************************************
				//start test CCDateUtilities method **************************************************
				
				valueToBeTested = [CCFormatUtilities formatedDate:nsDate withTemplate:template localisedOrNil:[locales objectForKey:id_localIdentifier]];
				
				XCTAssertTrue([valueToBeTested isEqualToString:comparator], @"* %@-%@: actual output %@ does not match expected output %@ for input source %@", template, id_localIdentifier, valueToBeTested, comparator, key);
				
				//end Test *********************************************************************
				//******************************************************************************
			}
		}
	}
}


-(void)testFormatAmount{
	
	NSDictionary *locales = @{
							  @"Usa": [[NSLocale alloc]initWithLocaleIdentifier:@"en_US"],
							  @"France": [[NSLocale alloc]initWithLocaleIdentifier:@"fr_FR"],
							  @"Japan": [[NSLocale alloc]initWithLocaleIdentifier:@"ja_JP"]
							  };

	NSString *result = [CCFormatUtilities formatAmount:@"23" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥23");
	result = [CCFormatUtilities formatAmount:@"23" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"France"]];
	XCTAssertEqualObjects(result, @"23 ¥JP");
	result = [CCFormatUtilities formatAmount:@"23" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Japan"]];
	XCTAssertEqualObjects(result, @"¥23");
	
	//currency with 3 decimales digits : KWD - Koweit dinar
	result = [CCFormatUtilities formatAmount:@"23.332" withCurrency:@"KWD" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"KWD23.332");
	result = [CCFormatUtilities formatAmount:@"23.332" withCurrency:@"KWD" localisedOrNil:[locales objectForKey:@"France"]];
	XCTAssertEqualObjects(result, @"23,332 KWD");
	result = [CCFormatUtilities formatAmount:@"23.332" withCurrency:@"KWD" localisedOrNil:[locales objectForKey:@"Japan"]];
	XCTAssertEqualObjects(result, @"KWD23.332");
	
	
	//remove decimal value for JPY
	result = [CCFormatUtilities formatAmount:@"23.3" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥23");
	result = [CCFormatUtilities formatAmount:@"23.3" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"France"]];
	XCTAssertEqualObjects(result, @"23 ¥JP");
	result = [CCFormatUtilities formatAmount:@"23.3" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Japan"]];
	XCTAssertEqualObjects(result, @"¥23");
	
	//empty -> 0
	result = [CCFormatUtilities formatAmount:@"0" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥0");
	result = [CCFormatUtilities formatAmount:nil withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥0");
	result = [CCFormatUtilities formatAmount:@"" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥0");
	result = [CCFormatUtilities formatAmount:@" " withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Japan"]];
	XCTAssertEqualObjects(result, @"¥0");

	//round
	result = [CCFormatUtilities formatAmount:@"0.1" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥0");
	result = [CCFormatUtilities formatAmount:@"0.5" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥0");
	result = [CCFormatUtilities formatAmount:@"0.6" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥1");
	result = [CCFormatUtilities formatAmount:@"0.59999" withCurrency:@"JPY" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"¥1");
	
	//USD
	result = [CCFormatUtilities formatAmount:@"0.1" withCurrency:@"USD" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"$0.10");
	result = [CCFormatUtilities formatAmount:@"0.1" withCurrency:@"USD" localisedOrNil:[locales objectForKey:@"France"]];
	XCTAssertEqualObjects(result, @"0,10 $US");
	result = [CCFormatUtilities formatAmount:@"0.1" withCurrency:@"USD" localisedOrNil:[locales objectForKey:@"Japan"]];
	XCTAssertEqualObjects(result, @"$0.10");

	
	//Euro
	result = [CCFormatUtilities formatAmount:@"0.1" withCurrency:@"EUR" localisedOrNil:[locales objectForKey:@"Usa"]];
	XCTAssertEqualObjects(result, @"€0.10");
	result = [CCFormatUtilities formatAmount:@"0.1" withCurrency:@"EUR" localisedOrNil:[locales objectForKey:@"France"]];
	XCTAssertEqualObjects(result, @"0,10 €");
	result = [CCFormatUtilities formatAmount:@"0.1" withCurrency:@"EUR" localisedOrNil:[locales objectForKey:@"Japan"]];
	XCTAssertEqualObjects(result, @"€0.10");

}

@end
