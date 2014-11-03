//
//  NSString+Additions.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 8/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NSString (Additions)

- (NSInteger)lengthIgnoreWhitespace;

+ (BOOL) isEmpty:(NSString *)string;
+ (BOOL) isEmptyIgnoreWhitespace:(NSString *)string;

/*
 * return a string with XML entities encoded or @"" if nil
 * quot  "
 * amp   &
 * apos  '
 * lt    <
 * gt    >
 */

+ (NSString *)stringByEncodingXmlEntities:(NSString *)string;

@end
