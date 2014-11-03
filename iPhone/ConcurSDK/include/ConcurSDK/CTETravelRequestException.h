//
//  CTETravelRequestException.h
//  ConcurSDK
//
//  Created by laurent mery on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//


#import <Foundation/Foundation.h>

@interface CTETravelRequestException : NSObject

@property (copy, nonatomic) NSString *ExceptionCode;
@property (copy, nonatomic) NSString *ExceptionMessage;
@property (copy, nonatomic) NSString *ExceptionLevel;

- (id)valueForUndefinedKey:(NSString *)key;

@end
