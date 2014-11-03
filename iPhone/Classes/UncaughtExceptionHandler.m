//
//  UncaughtExceptionHandler.m
//  UncaughtExceptions
//
//  Created by Matt Gallagher on 2010/05/25.
//  Copyright 2010 Matt Gallagher. All rights reserved.
//
//  Permission is given to use this source code file, free of charge, in any
//  project, commercial or otherwise, entirely at your risk, with the condition
//  that any redistribution (in part or whole) of source code must retain
//  this copyright and permission notice. Attribution in compiled projects is
//  appreciated but not required.
//

#import "UncaughtExceptionHandler.h"
#include <libkern/OSAtomic.h>
#include <execinfo.h>
#import "Flurry.h"
#import "ExceptionLogging.h"

NSString* detectedUncaughtExceptionKey = @"detectedUncaughtExceptionKey";

NSString * const UncaughtExceptionHandlerSignalExceptionName = @"UncaughtExceptionHandlerSignalExceptionName";
NSString * const UncaughtExceptionHandlerSignalKey = @"UncaughtExceptionHandlerSignalKey";
NSString * const UncaughtExceptionHandlerAddressesKey = @"UncaughtExceptionHandlerAddressesKey";

volatile int32_t UncaughtExceptionCount = 0;
const int32_t UncaughtExceptionMaximum = 10;

@implementation UncaughtExceptionHandler

+ (NSArray *)backtrace
{
    return [NSThread callStackSymbols];
}

- (void)validateAndSaveCriticalApplicationData:(NSException *)exception
{
	[ExceptionLogging logException:exception];
}

- (void)handleException:(NSException *)exception
{
	[self validateAndSaveCriticalApplicationData:exception];

	NSSetUncaughtExceptionHandler(NULL);
	signal(SIGABRT, SIG_DFL);
	signal(SIGILL, SIG_DFL);
	signal(SIGSEGV, SIG_DFL);
	signal(SIGFPE, SIG_DFL);
	signal(SIGBUS, SIG_DFL);
	signal(SIGPIPE, SIG_DFL);
	
	if ([[exception name] isEqual:UncaughtExceptionHandlerSignalExceptionName])
	{
		kill(getpid(), [[exception userInfo][UncaughtExceptionHandlerSignalKey] intValue]);
	}
	else
	{
		[exception raise];
	}
}

@end


BOOL DetectedUncaughtException()
{
	return [[NSUserDefaults standardUserDefaults] boolForKey:detectedUncaughtExceptionKey];
}

void SetDetectedUncaughtException(BOOL newValue)
{
	[[NSUserDefaults standardUserDefaults] setBool:newValue forKey:detectedUncaughtExceptionKey];
	[[NSUserDefaults standardUserDefaults] synchronize];
}


void HandleException(NSException *exception)
{
	SetDetectedUncaughtException(YES);
    [Flurry logError:@"Uncaught" message:@"Crash!" exception:exception];
	
	int32_t exceptionCount = OSAtomicIncrement32(&UncaughtExceptionCount);
	if (exceptionCount > UncaughtExceptionMaximum)
	{
		return;
	}
	
	NSArray *callStack = [UncaughtExceptionHandler backtrace];
	
	NSMutableDictionary *userInfo = ([exception userInfo] != nil ? [NSMutableDictionary dictionaryWithDictionary:[exception userInfo]] : [NSMutableDictionary dictionary]);
	userInfo[UncaughtExceptionHandlerAddressesKey] = callStack;
    
    // TODO - Convert to using block/strong ref
    __autoreleasing UncaughtExceptionHandler* handler = [[UncaughtExceptionHandler alloc] init];
	[handler
		performSelectorOnMainThread:@selector(handleException:)
		withObject:
			[NSException
				exceptionWithName:[exception name]
				reason:[exception reason]
				userInfo:userInfo]
		waitUntilDone:YES];
}

void SignalHandler(int signal)
{
	SetDetectedUncaughtException(YES);
	
	int32_t exceptionCount = OSAtomicIncrement32(&UncaughtExceptionCount);
	if (exceptionCount > UncaughtExceptionMaximum)
	{
		return;
	}
	
	NSMutableDictionary *userInfo =
		[NSMutableDictionary
			dictionaryWithObject:@(signal)
			forKey:UncaughtExceptionHandlerSignalKey];

	NSArray *callStack = [UncaughtExceptionHandler backtrace];
	userInfo[UncaughtExceptionHandlerAddressesKey] = callStack;
	
	[[[UncaughtExceptionHandler alloc] init]
		performSelectorOnMainThread:@selector(handleException:)
		withObject:
			[NSException
				exceptionWithName:UncaughtExceptionHandlerSignalExceptionName
				reason:
					[NSString stringWithFormat:
						NSLocalizedString(@"Signal %d was raised.", nil),
						signal]
				userInfo:
					userInfo]
		waitUntilDone:YES];
}

void InstallUncaughtExceptionHandler()
{
	NSSetUncaughtExceptionHandler(&HandleException);
	signal(SIGABRT, SignalHandler);
	signal(SIGILL, SignalHandler);
	signal(SIGSEGV, SignalHandler);
	signal(SIGFPE, SignalHandler);
	signal(SIGBUS, SignalHandler);
	signal(SIGPIPE, SignalHandler);
}

