//
//  Base64.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "NSStringAdditions.h"
#import "Localizer.h"

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
    NSUInteger occurs = [NSString findAllOccurrences:concurUserId ofString:@"@"];
    NSUInteger firstOccurrence = [NSString findFirstOccurrence:concurUserId ofString:@"@"];
    
    if (occurs != 1 || firstOccurrence < 1)
        return NO;
    else
        return YES;
}

- (NSString *) localize
{
    return [Localizer getLocalizedText:self];
}
@end
	

