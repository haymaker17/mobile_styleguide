//
//  NSStringAdditions.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/10.
//  Copyright 2010 Concur. All rights reserved.
//
// Updated by Pavan 10/23/13
// Removed the conflicting method stringByEncodingXmlEntitites, this is moved to NSStrings+Additions.h

#import <Foundation/NSString.h>

@interface NSString (NSStringAdditions)

+ (NSString *) hexStringFromData:(NSData *)data length:(int)length;
+ (BOOL) isStringNullOrEmpty:(NSString*) text __attribute__ ((deprecated("use ![aString length] instead")));
+ (BOOL) isStringNullEmptyOrAllSpaces:(NSString*) text __attribute__ ((deprecated("use ![aString lengthIgnoreWhitespace] instead")));
+ (NSUInteger) findAllOccurrences:(NSString*) text ofString:(NSString*) token;
+ (NSInteger) findFirstOccurrence:(NSString*) text ofString:(NSString*) token;
+ (BOOL)isValidConcurUserId:(NSString *)concurUserId ;
- (NSString *) localize;

@end


