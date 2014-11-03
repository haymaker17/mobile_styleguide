//
//  NSString+Additions.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 8/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "NSString+Additions.h"

@implementation NSString (Additions)

- (NSInteger)lengthIgnoreWhitespace{
    return [[self stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] length];
}


+ (BOOL) isEmpty:(NSString *)string{
    return !string.length;
}
+ (BOOL) isEmptyIgnoreWhitespace:(NSString *)string{
    return !string.lengthIgnoreWhitespace;
}


+ (NSString *)stringByEncodingXmlEntities:(NSString *)string{
    if (string) {
        NSMutableString *escapedString = [string mutableCopy];
        
        [escapedString replaceOccurrencesOfString:@"&" withString:@"&amp;" options:0 range:NSMakeRange(0, [escapedString length])];
        [escapedString replaceOccurrencesOfString:@"<" withString:@"&lt;" options:0 range:NSMakeRange(0, [escapedString length])];
        [escapedString replaceOccurrencesOfString:@">" withString:@"&gt;" options:0 range:NSMakeRange(0, [escapedString length])];
        [escapedString replaceOccurrencesOfString:@"\"" withString:@"&quot;" options:0 range:NSMakeRange(0, [escapedString length])];
        [escapedString replaceOccurrencesOfString:@"'" withString:@"&apos;" options:0 range:NSMakeRange(0, [escapedString length])];
        return escapedString;
        
    }else{
        return @"";
    }
}


@end
