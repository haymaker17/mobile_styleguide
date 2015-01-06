//
//  CTEException.h
//  ConcurSDK
//
//  Created by laurent mery on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//


#import <Foundation/Foundation.h>
#import "CTEDataTypes.h"

@interface CTEException : NSObject

@property (copy, nonatomic, readonly) CTEDataTypes *ExceptionCode;
@property (copy, nonatomic, readonly) CTEDataTypes *ExceptionMessage;
@property (copy, nonatomic, readonly) CTEDataTypes *ExceptionLevel;

- (id)valueForUndefinedKey:(NSString *)key;

@end
