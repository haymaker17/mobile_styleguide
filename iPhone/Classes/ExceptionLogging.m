//
//  ExceptionLogging.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExceptionLogging.h"
#import "MCLogging.h"
#import "UncaughtExceptionHandler.h"
#import "ExSystem.h" 


static NSString* exceptionLogFileName = @"exceptionLog.txt";

static const int maxExceptionLogFileSize = 100 * 1024;	// 100 kilobytes

@implementation ExceptionLogging

+ (NSString*) exceptionFilePath
{
	NSString *exceptionDirPathStr = [MCLogging obtainLogsDir];
	NSString *exceptionFilePathStr = [exceptionDirPathStr stringByAppendingPathComponent:exceptionLogFileName];
	return exceptionFilePathStr;
}

+ (void)logException:(NSException *)exception
{
	if (exception == nil)
		return;
	
	NSFileHandle* exceptionFile = [ExceptionLogging fileHandleForWritingToExceptionLog];
	if (exceptionFile != nil)
	{
		NSData* summaryData = [ExceptionLogging getSummaryData];
		if (summaryData != nil)
		{
			[exceptionFile writeData:summaryData];
		}
		
		NSData* exceptionData = [ExceptionLogging getExceptionData:exception];
		if (exceptionData != nil)
		{
			[exceptionFile writeData:exceptionData];
		}
		
		[exceptionFile closeFile];
	}
}

+ (NSFileHandle*) fileHandleForWritingToExceptionLog
{
	NSFileManager* fileManager = [NSFileManager defaultManager];
	NSString* exceptionDirPath = [MCLogging obtainLogsDir];
	
	if (fileManager == nil || exceptionDirPath == nil)
	{
		return nil;
	}
	
	if (![fileManager fileExistsAtPath:exceptionDirPath])
	{
		if (![fileManager createDirectoryAtPath:exceptionDirPath withIntermediateDirectories:TRUE attributes:nil error:nil])
		{
			return nil;
		}
	}
	
	NSString *exceptionFilePath = [exceptionDirPath stringByAppendingPathComponent:exceptionLogFileName];
	if (![fileManager fileExistsAtPath:exceptionFilePath])
	{
		NSData* data = [NSData data];
		if (![fileManager createFileAtPath:exceptionFilePath contents:data attributes:nil])
		{
			return nil;
		}
	}
	
	NSFileHandle* exceptionFile = [NSFileHandle fileHandleForWritingAtPath:exceptionFilePath];
	if (exceptionFile == nil)
	{
		return nil;
	}
	
	unsigned long long fileSize = [exceptionFile seekToEndOfFile];
	if (fileSize > maxExceptionLogFileSize)
	{
		[exceptionFile truncateFileAtOffset:0];
	}
	
	return exceptionFile;
}

+ (NSData*) getSummaryData
{
	NSData* summaryData = nil;
	NSString* summaryFileName = [[MCLogging getInstance] createLogSummaryWithSettings];
	if (summaryFileName != nil)
	{
		summaryData = [NSData dataWithContentsOfFile:summaryFileName];
	}
	return summaryData;
}

+ (NSData*) getExceptionData:(NSException*)exception
{
	NSArray *callStackArray = nil;
	
	if (exception.userInfo != nil)
		callStackArray = (exception.userInfo)[UncaughtExceptionHandlerAddressesKey];

#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 40000
    if (callStackArray == nil)
        callStackArray = [exception callStackSymbols];
#endif
	
	NSString *callStack = (callStackArray != nil ? [callStackArray description] : @"No callstack"); // Do not localize
	NSString *name = exception.name != nil ? exception.name : @"unknown name"; // Do not localize
	NSString *reason = exception.reason != nil ? exception.reason : @"unknown reason"; // Do not localize
	
	NSString *dateTimeStamp = [ExceptionLogging getCurrentDateTimeAsString];
	NSString *exceptionDataAsString = [NSString stringWithFormat:@"Unhandled exception at %@: name=%@, reason=%@, callstack:\n%@", dateTimeStamp, name, reason, callStack];
	NSData* exceptionData = [exceptionDataAsString dataUsingEncoding:NSUTF8StringEncoding];
	return exceptionData;
}

+ (NSString *) getCurrentDateTimeAsString
{
	NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
	// specify timezone
	[dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
	// Localizing date
	[dateFormatter setLocale:[NSLocale currentLocale]];
	
	[dateFormatter setDateStyle:NSDateFormatterShortStyle];
	[dateFormatter setTimeStyle:NSDateFormatterShortStyle];
	NSString *formattedDateString = [dateFormatter stringFromDate:[NSDate date]];
	return formattedDateString;
}

@end
