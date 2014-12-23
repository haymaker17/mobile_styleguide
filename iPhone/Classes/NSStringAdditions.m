//
//  Base64.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "NSStringAdditions.h"
#import "Localizer.h"
#import "CountryPhoneFormatDict.h"

static char base64EncodingTable[64] = {
	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
	'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
	'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
	'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
};

@implementation NSString (NSStringAdditions)

+ (NSString *) base64StringFromData: (NSData *)data length: (int)length 
{
	unsigned long ixtext, lentext;
	long ctremaining;
	unsigned char input[3], output[4];
	short i, charsonline = 0, ctcopy;
	const unsigned char *raw;
	NSMutableString *result;
	
	lentext = [data length]; 
	if (lentext < 1)
		return @"";
	result = [NSMutableString stringWithCapacity: lentext];
	raw = [data bytes];
	ixtext = 0; 
	
	while (true) 
	{
		ctremaining = lentext - ixtext;
		if (ctremaining <= 0) 
			break;        
		for (i = 0; i < 3; i++) 
		{ 
			unsigned long ix = ixtext + i;
			if (ix < lentext)
				input[i] = raw[ix];
			else
				input[i] = 0;
		}
		output[0] = (input[0] & 0xFC) >> 2;
		output[1] = ((input[0] & 0x03) << 4) | ((input[1] & 0xF0) >> 4);
		output[2] = ((input[1] & 0x0F) << 2) | ((input[2] & 0xC0) >> 6);
		output[3] = input[2] & 0x3F;
		ctcopy = 4;
		switch (ctremaining) 
		{
			case 1: 
				ctcopy = 2; 
				break;
			case 2: 
				ctcopy = 3; 
				break;
		}
		
		for (i = 0; i < ctcopy; i++)
			[result appendString: [NSString stringWithFormat: @"%c", base64EncodingTable[output[i]]]];
		
		for (i = ctcopy; i < 4; i++)
			[result appendString: @"="];
		
		ixtext += 3;
		charsonline += 4;
		
		if ((length > 0) && (charsonline >= length))
			charsonline = 0;
		
		
	}
	return result;
}


+ (NSString *) hexStringFromData:(NSData *)data length:(int)length
{
    const unsigned char *bytes = (const unsigned char*)[data bytes];
    
    NSMutableString *result = [NSMutableString stringWithCapacity: length*2];
    for (int i=0; i<length; i++)
    {
        [result appendString:[NSString stringWithFormat:@"%02X", (unsigned int)bytes[i]]];
    }
    
    return result;
}

+ (BOOL) isStringNullOrEmpty:(NSString*) text
{
	return text == nil || ([text length]==0);
}

+ (BOOL) isStringNullEmptyOrAllSpaces:(NSString*) text
{
	if (![text length])
		return YES;
	
	NSCharacterSet *wsCharSet = [NSCharacterSet whitespaceCharacterSet];
	NSString* trimmedText = [text stringByTrimmingCharactersInSet:wsCharSet];

	return [trimmedText length]==0;
}

// Search a given string to count how many times a second given string can be found
+ (NSUInteger) findAllOccurrences:(NSString*) text ofString:(NSString*) token
{
    NSUInteger count = 0, length = [text length];
    NSRange range = NSMakeRange(0, [text length]);
    while(range.location != NSNotFound)
    {
        range = [text rangeOfString: token options:0 range:range];
        if(range.location != NSNotFound)
        {
            range = NSMakeRange(range.location + range.length, length - (range.location + range.length));
            count++; 
        }
    }
    return count;
}

// Search a given string for the first occurrnce of a second given string
+ (NSInteger) findFirstOccurrence:(NSString*) text ofString:(NSString*) token
{
    NSRange range = NSMakeRange(0, [text length]);
    range = [text rangeOfString: token options:0 range:range];
    if(range.location != NSNotFound)
    {
        return (NSInteger)range.location;
    }
    return -1;
}

/**
 * Check if the user id is in format of xxxx@yyyy
 */
+ (BOOL)isValidConcurUserId:(NSString *)concurUserId
{
    /*
    NSUInteger occurs = [NSString findAllOccurrences:concurUserId ofString:@"@"];
    NSUInteger firstOccurrence = [NSString findFirstOccurrence:concurUserId ofString:@"@"];
    
    if (occurs != 1 || firstOccurrence < 1)
        return NO;
    else
        return YES;
     */
    
    // Simplify the logic
    NSArray *tmp = [concurUserId componentsSeparatedByString:@"@"];
    if ([tmp count]!=2) {
        return NO;
    }
    else if(![tmp[0] lengthIgnoreWhitespace] || ![tmp[1] lengthIgnoreWhitespace]){
        return NO;
    }
    else
        return YES;
}

+(NSString *)formatPhoneNo:(NSString *)phoneNumber withLocale:(NSString *)countryCode {
    
    CountryPhoneFormatDict *predefinedFormats = [[CountryPhoneFormatDict alloc] init];
    
    NSArray *localeFormats = [predefinedFormats objectForKey:countryCode];
    // If no such country code in dictionary, return the input string
    if(localeFormats == nil){
        return phoneNumber;
    }
    /*
     Other wise, follow the patterns in dictionary
        1. delete the useless character which user input
        2. For each patterns in dictionary, get the pattern string "phoneFormat"
        3. for each character in "phoneFormat"
            1)delete the useless character
            2)if input is the same as character in formatter, continue
              if not, append the character string to "temp"
        4. use the index "i" to track the length, if meet the end of input string, return the
           appended string we created
     */
    NSString *input = [self strip:phoneNumber];
    for(NSString *phoneFormat in localeFormats) {
        int i = 0;
        NSMutableString *temp = [[NSMutableString alloc] init];
        for(int p = 0; temp != nil && i < [input length] && p < [phoneFormat length]; p++) {
            char c = [phoneFormat characterAtIndex:p];
            BOOL required = [self canBeInputByPhonePad:c];      //if the format character is needed
            char next = [input characterAtIndex:i];
            switch(c) {
                case '$':
                    p--;
                    [temp appendFormat:@"%c", next]; i++;
                    break;
                case '#':
                    if(next < '0' || next > '9') {
                        temp = nil;
                        break;
                    }
                    [temp appendFormat:@"%c", next]; i++;
                    break;
                default:
                    if(required){
                        if(next != c) {
                            temp = nil;
                            break;
                        }
                        [temp appendFormat:@"%c", next]; i++;
                    }else{
                        [temp appendFormat:@"%c", c];
                        if(next == c) i++;
                    }
                    break;
            }
        }
        if(i == [input length]) {       //if meet the length limit
            return temp;
        }
    }
    return input;
}

+ (NSString *)strip:(NSString *)phoneNumber {
    NSMutableString *res = [[NSMutableString alloc] init];
    for(int i = 0; i < [phoneNumber length]; i++) {
        char next = [phoneNumber characterAtIndex:i];
        if([self canBeInputByPhonePad:next])
            [res appendFormat:@"%c", next];
    }
    return res;
}

+ (BOOL)canBeInputByPhonePad:(char)c {
    if(c == '+' || c == '*' || c == '#' || (c >= '0' && c <= '9'))
        return YES;
    return NO;
}

- (NSString *) localize
{
    return [Localizer getLocalizedText:self];
}
@end
	

