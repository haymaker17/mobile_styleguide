//
//  FormatUtils.m
//  ConcurMobile
//
//  Created by yiwen on 2/3/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FormatUtils.h"
#import "NSStringAdditions.h"
#define customDecimalOFF -1

@implementation FormatUtils


+ (NSDecimalNumber*) decimalNumberFromServerString: (NSString*)str
{
    NSLocale *usLoc = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
    return [NSDecimalNumber decimalNumberWithString:str locale:usLoc];
}

+ (NSNumber*) boolNumberFromServerString:(NSString*) str
{
    if (![str lengthIgnoreWhitespace])
        return nil;
    
    return [NSNumber numberWithBool:([@"true" caseInsensitiveCompare:str] == NSOrderedSame || [@"Y" caseInsensitiveCompare:str] == NSOrderedSame)];
}

//+ (NSString*) getCurrencySymbol:(NSString*)crnCode
//{
//    NSDictionary * CRN_SYMBOLS = [[NSDictionary alloc] initWithObjectsAndKeys:@"$", @"USD", @"\u00A3", @"GBP", @"\u20AC", @"EUR", nil];
//	
//    return [CRN_SYMBOLS objectForKey:crnCode]; 
//}

+ (NSString*) formatMoneyWithNumber :(NSNumber*)num crnCode:(NSString*) crnCode withCurrency:(BOOL) crnFlag
{
    return [FormatUtils formatMoneyWithNumber:num crnCode:crnCode decimalPlaces:customDecimalOFF withCurrency:crnFlag];
}

+ (NSString*) formatMoneyWithNumber :(NSNumber*)num crnCode:(NSString*) crnCode
{
    //"-1" decimalPlaces: no specific decimal config, use default crnCode config
    return [FormatUtils formatMoneyWithNumber:num crnCode:crnCode decimalPlaces:customDecimalOFF];
}

+ (NSString*) formatMoneyWithNumber :(NSNumber*)num crnCode:(NSString*) crnCode decimalPlaces:(int)fractionDigits
{
    return [FormatUtils formatMoneyWithNumber:num crnCode:crnCode decimalPlaces:customDecimalOFF withCurrency:YES];
}

+ (NSString*) formatMoneyString:(NSString*)numberString crnCode:(NSString*)crnCode decimalPlaces:(int)fractionDigits
{
    return [FormatUtils formatMoneyWithNumber:@([numberString doubleValue]) crnCode:crnCode decimalPlaces:fractionDigits withCurrency:YES];
}

+(NSNumberFormatter *) getCurrencyFormatter:(NSString*) crnCode
{
	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [currencyStyle setNumberStyle:NSNumberFormatterCurrencyStyle];
	[currencyStyle setCurrencyCode:crnCode];
    return currencyStyle;
}

+(NSNumberFormatter *) getDecimalFormatter:(NSString*) crnCode minDigits:(int)minFDigits maxDigits:(int)maxFDigits
{
	NSNumberFormatter *numberStyle = [[NSNumberFormatter alloc] init];
	[numberStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [numberStyle setNumberStyle:NSNumberFormatterDecimalStyle];
    [numberStyle setMinimumFractionDigits:minFDigits];
    [numberStyle setMaximumFractionDigits:maxFDigits];
    return numberStyle;
}
+ (NSString*) getFractionDigits:(NSString*)roundedCurrencyNum decimalSeparator:(NSRange) decimalRange
{
    // Get integer portion of the currency
    int decimalPos = decimalRange.location;
    NSString *result = @"";
    if (decimalPos != NSNotFound)
    {
        NSRange digitsRange = decimalRange;
        digitsRange.location = decimalRange.location+1;
        digitsRange.length = [roundedCurrencyNum length] - digitsRange.location;
        NSRange afterDigits = [roundedCurrencyNum rangeOfCharacterFromSet:[[NSCharacterSet decimalDigitCharacterSet] invertedSet] options:0 range:digitsRange];
        if (afterDigits.location == NSNotFound)
            result = [roundedCurrencyNum substringFromIndex:decimalPos+1];
        else
        {
            digitsRange.length = afterDigits.location - digitsRange.location;
            result = [roundedCurrencyNum substringWithRange:digitsRange];
        }
    }
    return result;
}

// CRMC-38267/MOB-13016
+ (NSString*) formatMoneyWithNumberWithCurrency :(NSNumber*)num crnCode:(NSString*) crnCode
{
    if (num == nil) {
        num = @(0);
    }
    //Currency Style
    NSNumberFormatter *currencyStyle = [self getCurrencyFormatter:crnCode];
    int minFDigits = currencyStyle.minimumFractionDigits;
    int maxFDigits = currencyStyle.maximumFractionDigits;
    NSString *decimalSeparator = currencyStyle.decimalSeparator;
    
    NSString *roundedCurrencyNum = [currencyStyle stringFromNumber:num];
    if (maxFDigits == 0 || ![decimalSeparator length])
	{
        return roundedCurrencyNum;
    }
    NSRange decimalRange = [roundedCurrencyNum rangeOfString:decimalSeparator options:NSBackwardsSearch];
    if (decimalRange.location == NSNotFound)
        return roundedCurrencyNum;
    
    NSString *fractionDigits = [self getFractionDigits:roundedCurrencyNum decimalSeparator:decimalRange];
    
    // Get fraction digits
    NSNumberFormatter *numberStyle = [self getDecimalFormatter:crnCode minDigits:minFDigits maxDigits:maxFDigits];
    NSString *rawNum = [numberStyle stringFromNumber:num];
    NSRange decimalRange2 = [rawNum rangeOfString:decimalSeparator options:NSBackwardsSearch];
    NSString *fraction = [self getFractionDigits:rawNum decimalSeparator:decimalRange2];
    
    NSString *retValue = roundedCurrencyNum;
    
    if (![fractionDigits isEqualToString:fraction])
    {
        // Get range of original decimal places
        NSRange replaceRange;
        replaceRange.location = decimalRange.location + 1;
        replaceRange.length = fractionDigits.length;
        // Replace it with new decimal places - handle the negative number format
        retValue = [roundedCurrencyNum stringByReplacingCharactersInRange:replaceRange withString:fraction];
    }
	return retValue;
}

+ (NSString*) formatMoneyWithNumber :(NSNumber*)num crnCode:(NSString*) crnCode decimalPlaces:(int)fractionDigits withCurrency:(BOOL) crnFlag
{
    if (crnFlag && fractionDigits == customDecimalOFF)
    {
        return [self formatMoneyWithNumberWithCurrency:num crnCode:crnCode];
    }
    
    //Currency Style
	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
    if (crnFlag)
        [currencyStyle setNumberStyle:NSNumberFormatterCurrencyStyle];
    else
        [currencyStyle setNumberStyle:NSNumberFormatterDecimalStyle];
    
	[currencyStyle setCurrencyCode:crnCode];
    //MOB-9302 display 3 decimal for USD carRate
    //"-1" decimalPlaces: no specific decimal config, use default crnCode config
    if (fractionDigits != customDecimalOFF) {
        [currencyStyle setMinimumFractionDigits:fractionDigits];
        [currencyStyle setMaximumFractionDigits:fractionDigits];
    }

	NSString *retValue = [currencyStyle  stringFromNumber:num];
	return retValue;
}

+ (NSString*) formatMoney :(NSString*)amount crnCode:(NSString*) crnCode
{
    NSNumber *num = @([amount doubleValue]);
    return [FormatUtils formatMoneyWithNumber:num crnCode:crnCode decimalPlaces:customDecimalOFF];
}

+ (NSString*) formatStyledMoneyWithoutCrn:(NSString *)amount crnCode:(NSString *)crnCode
{
    NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
	[currencyStyle setNumberStyle:NSNumberFormatterDecimalStyle];
    
    NSNumber *num = [currencyStyle numberFromString:amount];
	
	int32_t defaultFractionDigits = 0;
	double roundingIncrement = 0.0;
	CFStringRef cfCrnCode = (__bridge CFStringRef)crnCode;
	Boolean gotDecimalInfo = [crnCode length] && CFNumberFormatterGetDecimalInfoForCurrencyCode(cfCrnCode, &defaultFractionDigits, &roundingIncrement);
	if (gotDecimalInfo)
	{
		[currencyStyle setMaximumFractionDigits:defaultFractionDigits];
		[currencyStyle setMinimumFractionDigits:defaultFractionDigits];
	}
	else {
		if ([@"JPY" isEqualToString:crnCode])
		{
			[currencyStyle setMaximumFractionDigits:0];
		}
		else
		{
			[currencyStyle setMaximumFractionDigits:2];
			[currencyStyle setMinimumFractionDigits:2];
		}
	}
	
	[currencyStyle setMinimumIntegerDigits:1];
    
	NSString *retValue = [currencyStyle  stringFromNumber:num];
	return retValue;
}

// like formatMoneyWithoutCrn but works with , or . decimal separator
// the logic here is not desirable. ideally we would have a money object and pass around the data as data
// currently data is put to and read from the UI and pulled from the server in such a way that sometimes values come in as decimal format, sometimes as comma format
// this requires us to attempt both conversions. there is probably a better solution and the reader is encouraged to pursue one.
// given tight schedules, this is the best solution possible
+ (NSString*) formatMoneyWithoutCrnInternational:(NSString*)amount crnCode:(NSString*) crnCode
{
    // start by trying to convert as US style currency format
    NSLocale* usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
    NSLocale* franceLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"fr_FR"];

    NSNumberFormatter *amountFormatter = [[NSNumberFormatter alloc] init];
    [amountFormatter setLocale:[NSLocale currentLocale]];
    [amountFormatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [amountFormatter setNumberStyle:NSNumberFormatterDecimalStyle];
    [amountFormatter setLocale:usLocale];

    NSNumber* amountAsNumber = [amountFormatter numberFromString:amount];
    // if conversion using US currency format (xxx.yy) fails, try with comma (xxx,yy)
    if( NULL == amountAsNumber )
    {
        [amountFormatter setLocale:franceLocale];
        amountAsNumber = [amountFormatter numberFromString:amount];
    }
    
	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
	[currencyStyle setNumberStyle:NSNumberFormatterDecimalStyle];
	
	int32_t defaultFractionDigits = 0;
	double roundingIncrement = 0.0;
	CFStringRef cfCrnCode = (__bridge CFStringRef)crnCode;
	Boolean gotDecimalInfo = [crnCode length] && CFNumberFormatterGetDecimalInfoForCurrencyCode(cfCrnCode, &defaultFractionDigits, &roundingIncrement);
	if (gotDecimalInfo)
	{
		[currencyStyle setMaximumFractionDigits:defaultFractionDigits];
		[currencyStyle setMinimumFractionDigits:defaultFractionDigits];
	}
	else {
		if ([@"JPY" isEqualToString:crnCode])
		{
			[currencyStyle setMaximumFractionDigits:0];
		}
		else
		{
			[currencyStyle setMaximumFractionDigits:2];
			[currencyStyle setMinimumFractionDigits:2];
		}
	}
	
	[currencyStyle setMinimumIntegerDigits:1];
    
	NSString *retValue = [currencyStyle  stringFromNumber:amountAsNumber];
	return retValue;
}

+ (NSString*) formatMoneyWithoutCrn :(NSString*)amount crnCode:(NSString*) crnCode
{
    //MOB-9545 input 1000 in amount, but reset to 1 after change currency.
	NSNumberFormatter *amountFormatter = [[NSNumberFormatter alloc] init];
    [amountFormatter setLocale:[NSLocale currentLocale]];
    [amountFormatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
    [amountFormatter setNumberStyle:NSNumberFormatterDecimalStyle];
    
    NSNumber *num = [amountFormatter numberFromString:amount];
	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
	[currencyStyle setNumberStyle:NSNumberFormatterDecimalStyle];
	
	int32_t defaultFractionDigits = 0;
	double roundingIncrement = 0.0;
	CFStringRef cfCrnCode = (__bridge CFStringRef)crnCode;
	Boolean gotDecimalInfo = [crnCode length] && CFNumberFormatterGetDecimalInfoForCurrencyCode(cfCrnCode, &defaultFractionDigits, &roundingIncrement);
	if (gotDecimalInfo)
	{
		[currencyStyle setMaximumFractionDigits:defaultFractionDigits];
		[currencyStyle setMinimumFractionDigits:defaultFractionDigits];
	}
	else {
		if ([@"JPY" isEqualToString:crnCode])
		{
			[currencyStyle setMaximumFractionDigits:0];
		}
		else
		{
			[currencyStyle setMaximumFractionDigits:2];
			[currencyStyle setMinimumFractionDigits:2];
		}
	}
	
	[currencyStyle setMinimumIntegerDigits:1];

	NSString *retValue = [currencyStyle  stringFromNumber:num];
	return retValue;
}

+ (NSString*) formatDouble :(NSString*)number
{
	if (number == nil || [number isEqualToString:@""])
		return nil;
	NSNumber *num = @([number doubleValue]);
	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
	[currencyStyle setMaximumFractionDigits:8];
	[currencyStyle setMinimumIntegerDigits:1];
	NSString *retValue = [currencyStyle  stringFromNumber:num];
	return retValue;
}
+ (NSString*) formatInteger :(NSString*)number
{
	if (number == nil || [number isEqualToString:@""])
		return nil;

	NSNumber *num = @([number doubleValue]);
	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
	[currencyStyle setMaximumFractionDigits:0];
	[currencyStyle setMinimumIntegerDigits:1];
	NSString *retValue = [currencyStyle  stringFromNumber:num];
	return retValue;
}

+(NSString*) formatDateFromXml:(NSString*) dateStr
{
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat: @"yyyy-MM-dd"];
    NSDate* date = [dateFormatter dateFromString:dateStr];
    
    [dateFormatter setTimeStyle:NSDateFormatterNoStyle];
    [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
    [dateFormatter setLocale:usLocale];
    NSString* result = [dateFormatter stringFromDate:date];
    return result;
}


+(CGFloat) getTextFieldHeight:(int)width Text:(NSString *)text FontSize:(float)fontSize
{
	CGFloat		result = 20.0f;
	
	if (text)
	{
		CGSize		textSize = { width, 20000.0f };		// width and height of text area
		CGSize		size = [text sizeWithFont:[UIFont systemFontOfSize:fontSize] constrainedToSize:textSize lineBreakMode:NSLineBreakByWordWrapping];

		//size.height += 29.0f;				// top and bottom margin
		result = MAX(size.height, 20.0f);	// at least one row
		//NSLog(@"%f", result);
	}
	return result;
}

+(CGFloat) getTextFieldHeight:(int)width Text:(NSString *)text Font:(UIFont*) font
{
	CGFloat		result = 20.0f;
	
	if (text)
	{
		CGSize		textSize = { width, 20000.0f };		// width and height of text area
        //		CGSize		size = [text sizeWithFont:[UIFont systemFontOfSize:fontSize] constrainedToSize:textSize lineBreakMode:NSLineBreakByWordWrapping];
        CGSize		size = [text sizeWithFont:font constrainedToSize:textSize lineBreakMode:NSLineBreakByWordWrapping];
        
		//size.height += 29.0f;				// top and bottom margin
		result = MAX(size.height, 20.0f);	// at least one row
		//NSLog(@"%f", result);
	}
	return result;
}


+(NSString *)makeXMLSafe:(NSString *)string
{
	return [NSString stringByEncodingXmlEntities:string];
}

+(NSString *)formatDuration:(int)duration
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

// TODO - We need to call these when we save entry/header to server
+(NSString*) convertDoubleToStringUS:(NSString*)localNumber
{
	if (localNumber == nil || [localNumber isEqualToString:@""])
		return nil;

	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
	[currencyStyle setNumberStyle:NSNumberFormatterDecimalStyle];
    
    NSNumber *retValue = [currencyStyle numberFromString:localNumber];

    // MOB-17611 conversion fails if the locale is different, so try again with US format
    // This failure is usually cause we're sending server format right back, which is en_US
    if (retValue == nil) {
        [currencyStyle setLocale:[[NSLocale alloc] initWithLocaleIdentifier:@"en_US"]];
        retValue = [currencyStyle numberFromString:localNumber];

        // crap, this is probably an error.  we failed to convert the string
        if (retValue == nil) {
            return nil;
        }
    }

	NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
    NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
	[numberFormatter setLocale:usLocale];
	[numberFormatter setMaximumFractionDigits:8];
	[numberFormatter setMinimumFractionDigits:2];

    NSString *myVal = [numberFormatter stringFromNumber:retValue];
	return myVal;
}

+(NSString*) convertIntegerToStringUS:(NSString*)localNumber
{
	if (localNumber == nil || [localNumber isEqualToString:@""])
		return nil;
	
	int intVal = 0.0;
	NSScanner* scanner = [NSScanner scannerWithString:localNumber];
	[scanner setLocale:[NSLocale currentLocale]];
	if ([scanner isAtEnd] == NO)
	{
		if (![scanner scanInt:&intVal])
		{
			return nil;
		}
	}
	
	NSNumberFormatter *numberFormatter = [[NSNumberFormatter alloc] init];
    NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
	[numberFormatter setLocale:usLocale];
	 NSString *myVal = [numberFormatter stringFromNumber:@(intVal)];
	return myVal;
}

+(BOOL) isValidEmail:(NSString*) email
{
    BOOL result = YES;

    NSError *error = NULL;
    NSString* emailPattern = @"^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";
    NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:emailPattern                                  
                                                                           options:NSRegularExpressionCaseInsensitive
                                                                             error:&error];
    NSRange range;
    range.location = 0;
    range.length = [email length];
    NSUInteger count = [regex numberOfMatchesInString:email options:NSMatchingAnchored range:range];
    result = count ==1;
    return result;
}


@end
