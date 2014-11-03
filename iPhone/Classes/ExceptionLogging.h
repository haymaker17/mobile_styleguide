//
//  ExceptionLogging.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ExceptionLogging : NSObject {

}

+ (NSString*) exceptionFilePath;
+ (void)logException:(NSException *)exception;
+ (NSFileHandle*) fileHandleForWritingToExceptionLog;
+ (NSData*) getSummaryData;
+ (NSData*) getExceptionData:(NSException*)exception;
+ (NSString*) getCurrentDateTimeAsString;

@end
