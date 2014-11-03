//
//  ConcurJSONUtility.m
//  ConcurMobile
//
//  Created by ernest cho on 7/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ConcurJSONUtility.h"

@interface ConcurJSONUtility()

@end

// The JSON spec at ietf.org/rfc/rfc4627.txt contains this sentence in section 2.5:
// "All Unicode characters may be placed within the quotation marks except for the characters that must be escaped: quotation mark, reverse solidus, and the control characters (U+0000 through U+001F)."
// reverse solidus is backslash
@implementation ConcurJSONUtility

// Make input string safe for use inside json
+ (NSString *)escapeJson:(NSString *)input
{
    return [self removeJsonSpecials:input];
}

// workaround for json parsing issues
+ (NSString *)removeJsonSpecials:(NSString*)input
{
    NSString *output = [[NSString alloc] init];
    if (input != nil) {
        output = [input stringByReplacingOccurrencesOfString:@"\n" withString:@" "];
        output = [output stringByReplacingOccurrencesOfString:@"\r" withString:@" "];
        output = [output stringByReplacingOccurrencesOfString:@"\t" withString:@" "];
        output = [output stringByReplacingOccurrencesOfString:@"\"" withString:@" "];
        output = [output stringByReplacingOccurrencesOfString:@"\\" withString:@" "];
    }
    return output;
}

// Does not work with Salesforce web services
+ (NSString *)escapeJsonSpecials:(NSString*)input
{
    NSString *output = [[NSString alloc] init];
    if (input != nil) {
        output = [input stringByReplacingOccurrencesOfString:@"\n" withString:@"\\n"];
        output = [output stringByReplacingOccurrencesOfString:@"\r" withString:@"\\r"];
        output = [output stringByReplacingOccurrencesOfString:@"\t" withString:@"\\t"];
        output = [output stringByReplacingOccurrencesOfString:@"\"" withString:@"\\\""];
        output = [output stringByReplacingOccurrencesOfString:@"\\" withString:@"\\\\"];
    }
    return output;
}

// Does not work with Salesforce web services
+ (NSString *)escapeJsonSpecialsWithUnicode:(NSString*)input
{
    NSString *output = [[NSString alloc] init];
    if (input != nil) {
        output = [input stringByReplacingOccurrencesOfString:@"\n" withString:@"\\u000A"];
        output = [output stringByReplacingOccurrencesOfString:@"\r" withString:@"\\u000D"];
        output = [output stringByReplacingOccurrencesOfString:@"\t" withString:@"\\u0009"];
        output = [output stringByReplacingOccurrencesOfString:@"\"" withString:@"\\u0022"];
        output = [output stringByReplacingOccurrencesOfString:@"\\" withString:@"\\u005C"];
    }
    return output;
}

// Does not work with Salesforce web services
+ (NSString *)escapeJsonWithHTML:(NSString*)input
{
    NSString *output = [[NSString alloc] init];
    if (input != nil) {
        output = [input stringByReplacingOccurrencesOfString:@"\n" withString:@"&#10;"];
        output = [output stringByReplacingOccurrencesOfString:@"\r" withString:@"&#13;"];
        output = [output stringByReplacingOccurrencesOfString:@"\t" withString:@"&#09;"];
        output = [output stringByReplacingOccurrencesOfString:@"\"" withString:@"&#34;"];
        output = [output stringByReplacingOccurrencesOfString:@"\\" withString:@"&#92;"];
    }
    return output;
}

@end
