//
//  FormatUtils.h
//  ConcurMobile
//
//  Created by yiwen on 2/3/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface FormatUtils : NSObject {

}

//+ (NSString*) getCurrencySymbol:(NSString*)crnCode;

+ (NSDecimalNumber *)decimalNumberFromServerString: (NSString*)str;
+ (NSNumber *)boolNumberFromServerString:(NSString *) str __attribute__ ((deprecated("use @([aString boolValue]) instead")));

+ (NSString *)formatMoney :(NSString*)amount crnCode:(NSString*) crnCode;

/**
 Format currency into a standardized displayable string.
 @param num Currency amount
 @param crnCode Country code
 @return Standardized string representation of currency
 */
+ (NSString *)formatMoneyWithNumber :(NSNumber*)amount crnCode:(NSString*) crnCode;

+ (NSString *)formatMoneyWithNumber :(NSNumber*)num crnCode:(NSString*) crnCode decimalPlaces:(int)fractionDigits;
+ (NSString *)formatMoneyWithNumber :(NSNumber*)num crnCode:(NSString*) crnCode withCurrency:(BOOL) crnFlag;

+ (NSString*) formatMoneyString:(NSString*)numberString crnCode:(NSString*)crnCode decimalPlaces:(int)fractionDigits;

+ (NSString *)formatStyledMoneyWithoutCrn:(NSString *)amount crnCode:(NSString *)crnCode;
+ (NSString*) formatMoneyWithoutCrnInternational:(NSString*)amount crnCode:(NSString*) crnCode;
+ (NSString *)formatMoneyWithoutCrn :(NSString*)amount crnCode:(NSString*) crnCode;
+ (NSString *)formatDouble :(NSString*)number;
+ (NSString *)formatInteger :(NSString*)number;

+ (NSString *)formatDateFromXml:(NSString*) dateStr;
+ (CGFloat) getTextFieldHeight:(int)width Text:(NSString *)text FontSize:(float)fontSize;
+ (CGFloat) getTextFieldHeight:(int)width Text:(NSString *)text Font:(UIFont*) font;

+ (NSString *)makeXMLSafe:(NSString *)string;
+ (NSString *)formatDuration:(int)duration;
+ (NSString *)convertDoubleToStringUS:(NSString*)dblVal;
+ (NSString *)convertIntegerToStringUS:(NSString*)intVal;

+ (BOOL)isValidEmail:(NSString*)email __attribute__ ((deprecated("use @([aString isValidEmail]) instead")));
@end
