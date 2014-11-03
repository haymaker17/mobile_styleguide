//
//  DataExtender.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/6/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@class NSString;

@interface NSData (DataExtender)

- (NSData *)AES256EncryptWithKey:(NSString *)key;
- (NSData *)AES256DecryptWithKey:(NSString *)key;
@end


