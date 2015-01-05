//
//  MCLogging.m
//  ConcurMobile
//
//  Created by yiwen on 12/21/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "MCLogging.h"
#import "MCLogInfo.h"
#import "ConcurMobileAppDelegate.h"
#import "ExSystem.h"

@implementation MCLogging
@synthesize logFile, logFileName, logLevel, logDict;

static MCLogging * sharedInstance = nil;
static int msgCount = 0;
#ifdef LOG_DEBUG
static const int MAX_PER_FILE = 100;
const int MAX_LOG_FILE_COUNT = 10;
#else
static const int MAX_PER_FILE = 50;
const int MAX_LOG_FILE_COUNT = 5;
#endif

static const NSString* logDir = @"logs";
static const NSString* logFileNamePrefix = @"MCLog";
static const NSString* levelNames[] = {@"DEBU", @"INFO", @"WARN", @"ERRO"};
const int MAX_MSG_COUNT_PER_FILE = 50;
// Log files are named MCLog1.txt to MCLog<Max>.txt
// If one log file is full (msgCount >=10), we move to the next and override the existing contents in the next file.  
// If we reach the MCLog<Max>.txt, we then start from MCLog1.txt.
// When app starts up, it starts a new file following the file with the latest writes.

//Call logging like this: [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::msgDone From Cache = %@", fromCache] Level:MC_LOG_DEBU];



#pragma mark Helper APIs
// helper APIs
- (void) initDateFormatter
{
	dateFormatter =[[NSDateFormatter alloc] init];
	// specify timezone
	[dateFormatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
    NSLocale *usLocale = [[NSLocale alloc] initWithLocaleIdentifier:@"en_US"];
    
	[dateFormatter setLocale:usLocale];
	
    [dateFormatter setDateFormat:@"MM/dd/yy HH:mm:ss.SSS zzz"];
}

- (NSString *) getFormattedTimeStamp 
{
	NSDate *date = [NSDate date];
	
	NSString *formattedDateString = [dateFormatter stringFromDate:date];
	
	return formattedDateString;
}


-(NSDate *) getTimeStampFromString:(NSString*) str
{
	return [dateFormatter dateFromString:str]; 
}

- (BOOL) fileSizeExceedLimit
{
	return msgCount >= MAX_PER_FILE; 
}


+ (NSString *) obtainLogsDir
{
	NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString* documentsDir = paths[0];
	NSString* logPath = [documentsDir stringByAppendingPathComponent:(NSString*)logDir];
	return logPath;
}


- (NSString *) obtainNewLogFileName 
{
	NSString* logPath = [MCLogging obtainLogsDir];
	NSError* err = NULL;
	NSString* tmplogFileName = logFileName;
	if (tmplogFileName == nil)
	{
		// We need to locate the latest written log and open the next one to override
		NSArray *array = [[NSFileManager defaultManager]
						  contentsOfDirectoryAtPath:logPath
						  error:&err];
		
		if (array == nil) {
			// Handle the error			
		} else {
			NSString* latestFile = NULL;
			NSDate* lastFileStamp = NULL;
			for (int ix=0; ix<[array count]; ix++) {
				NSString* fileName = (NSString*)array[ix];
				if ([logFileNamePrefix compare:[fileName substringToIndex:5]]==NSOrderedSame)
				{
					NSDictionary* attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:[NSString stringWithFormat:@"%@/%@", logPath, fileName] error:&err];
					NSDate *lastModificationDate = attributes[NSFileModificationDate];
					if (lastFileStamp == NULL ||
						[lastFileStamp compare:lastModificationDate]==NSOrderedAscending)
					{
						latestFile = fileName;
						lastFileStamp = lastModificationDate;
					}
				}
			}
			if (latestFile != NULL)
				tmplogFileName = [logPath stringByAppendingPathComponent:latestFile];
		}
	}
	
	if (tmplogFileName == NULL)
	{
		// We are starting from scratch
		NSString* fileShortName = [logFileNamePrefix stringByAppendingString:@"0.txt"];
		tmplogFileName = [logPath stringByAppendingPathComponent:fileShortName];
	} else {
		// move to one after the latestFile
		NSInteger len = [tmplogFileName length];
		unichar suffix = [tmplogFileName characterAtIndex:len-5];
		if (suffix >= '0'+MAX_LOG_FILE_COUNT-1)
		{
			suffix = '0';
		}else {
			suffix = suffix +1;
		}
		
		NSString* suffixStr = [NSString stringWithFormat:@"%C.txt", suffix];
		
		tmplogFileName = [[tmplogFileName substringToIndex:len-5] stringByAppendingString:suffixStr];
	}
	msgCount=0;
	return tmplogFileName;
	
}


- (NSString*) getPrevLogFileName 
{
	NSString* tmplogFileName = logFileName;

	// move to one after the latestFile
	int len = (int)[tmplogFileName length];
	
	unichar suffix = [tmplogFileName characterAtIndex:len-5];
	//unichar suffixArr[] = { suffix };
//	int suffixAsInt = [[NSString stringWithCharacters:suffixArr length:1] intValue]; 
//	
//	if (suffixAsInt == 0)
//	{
//		suffixAsInt = MAX_LOG_FILE_COUNT - 1;
//	}
//	else
//	{
//		suffixAsInt--;
//	}
	
	NSString* suffixStr = [NSString stringWithFormat:@"%C.txt", suffix];
	
	tmplogFileName = [[tmplogFileName substringToIndex:len-5] stringByAppendingString:suffixStr];
	return tmplogFileName;
}


- (NSString*) getNextLogFileName:(NSString*)startingLogFileName
{
	NSString* tmplogFileName = startingLogFileName;
	
	// move to one after the latestFile
	int len = (int)[tmplogFileName length];
	
	unichar suffix = [tmplogFileName characterAtIndex:len-5];
	unichar suffixArr[] = { suffix };
	int suffixAsInt = [[NSString stringWithCharacters:suffixArr length:1] intValue]; 
	
	if (suffixAsInt == MAX_LOG_FILE_COUNT - 1)
	{
		suffixAsInt = 0;
	}
	else
	{
		suffixAsInt++;
	}
	
	NSString* suffixStr = [NSString stringWithFormat:@"%i.txt", suffixAsInt];
	
	tmplogFileName = [[tmplogFileName substringToIndex:len-5] stringByAppendingString:suffixStr];
	return tmplogFileName;
}


- (NSString*) getOldestLogFileName 
{
	NSFileManager* filemgr = [NSFileManager defaultManager];

	NSString* oldestLogFileName = [self getNextLogFileName:logFileName];
	while (![filemgr fileExistsAtPath:oldestLogFileName])
	{
		oldestLogFileName = [self getNextLogFileName:oldestLogFileName];
		if ([oldestLogFileName isEqualToString:logFileName])
		{
			break;
		}
	}
	return oldestLogFileName;
}


- (NSFileHandle*)checkAndSetCurrentLogFile
{
	if (logFile == nil || [self fileSizeExceedLimit])
	{
		NSFileHandle * curLogFile = logFile;
		NSFileManager *filemgr;
		
		filemgr = [NSFileManager defaultManager];
		BOOL isDir;
		NSString* logPath = [MCLogging obtainLogsDir];
		self.logFileName = [self obtainNewLogFileName];
		
		if (!([filemgr fileExistsAtPath:logPath isDirectory:&isDir] && isDir))
		{
            [filemgr createDirectoryAtPath:logPath withIntermediateDirectories:YES attributes:nil error:nil];
			//WARNING WORK [filemgr createDirectoryAtPath:logPath attributes: nil];
		}
		
		if (!([filemgr fileExistsAtPath:logFileName]))
		{
			[filemgr createFileAtPath:logFileName contents:nil attributes:nil];
		}
		
		self.logFile=[NSFileHandle fileHandleForUpdatingAtPath:logFileName];
		[curLogFile closeFile];
		// Truncate the output file since it may contain data
		[logFile truncateFileAtOffset:0];
	}
	return logFile;
}
// end of helper APIs


#pragma mark -
#pragma mark Error Table Methods
-(void)populateErrorMessageTable
{
    self.logDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    // Enable the below for Test drive user.
    
    // oauth key: 8QkZveGsXP5uFdI6gwIAEC
    [[MCLogging getInstance] logErrorWithId:@"vH5CD4" Message:@"Validation error" Level:4];
    [[MCLogging getInstance] logErrorWithId:@"q" Message:@"Validation error" Level:1];
    [[MCLogging getInstance] logErrorWithId:@"Crak1" Message:@"Validation error" Level:17];
    [[MCLogging getInstance] logErrorWithId:@"EC" Message:@"Validation error" Level:20];
    [[MCLogging getInstance] logErrorWithId:@"8QbnS" Message:@"Validation error" Level:0];
    [[MCLogging getInstance] logErrorWithId:@"dRpo" Message:@"Validation error" Level:12];
    [[MCLogging getInstance] logErrorWithId:@"VSTR" Message:@"Validation error" Level:5];
    [[MCLogging getInstance] logErrorWithId:@"SXP52" Message:@"Validation error" Level:7];
    [[MCLogging getInstance] logErrorWithId:@"upwind" Message:@"Validation error" Level:14];
    [[MCLogging getInstance] logErrorWithId:@"M2eGs" Message:@"Validation error" Level:3];
    [[MCLogging getInstance] logErrorWithId:@"kZv" Message:@"Validation error" Level:2];
    [[MCLogging getInstance] logErrorWithId:@"11dI6e" Message:@"Validation error" Level:11];
    [[MCLogging getInstance] logErrorWithId:@"gwIA" Message:@"Validation error" Level:16];
    [[MCLogging getInstance] logErrorWithId:@"uF" Message:@"Validation error" Level:11];
    
    // oauth secret: 1WnPyVCLASjupdRpoFJvH5uOCy1Crakq
    [[MCLogging getInstance] logErrorWithId:@"VSTR" Message:@"Bounds error" Level:5];
    [[MCLogging getInstance] logErrorWithId:@"CLASS" Message:@"Bounds error" Level:6];
    [[MCLogging getInstance] logErrorWithId:@"Cb4" Message:@"Bounds error" Level:29];
    [[MCLogging getInstance] logErrorWithId:@"Crak1" Message:@"Bounds error" Level:27];
    [[MCLogging getInstance] logErrorWithId:@"1Wp2x" Message:@"Bounds error" Level:0];
    [[MCLogging getInstance] logErrorWithId:@"nPy" Message:@"Bounds error" Level:2];
    [[MCLogging getInstance] logErrorWithId:@"upwind" Message:@"Bounds error" Level:11];
    [[MCLogging getInstance] logErrorWithId:@"vH5CD4" Message:@"Bounds error" Level:19];
    [[MCLogging getInstance] logErrorWithId:@"uO" Message:@"Bounds error" Level:10];
    [[MCLogging getInstance] logErrorWithId:@"q" Message:@"Bounds error" Level:31];
    [[MCLogging getInstance] logErrorWithId:@"XbCy1" Message:@"Bounds error" Level:22];
    [[MCLogging getInstance] logErrorWithId:@"uOC" Message:@"Bounds error" Level:22];
    [[MCLogging getInstance] logErrorWithId:@"zMqFJ" Message:@"Bounds error" Level:14];
    [[MCLogging getInstance] logErrorWithId:@"dRpo" Message:@"Bounds error" Level:13];
    [[MCLogging getInstance] logErrorWithId:@"ju" Message:@"Bounds error" Level:10];

}


#pragma mark -
#pragma mark Static Methods
+ (MCLogging*) getInstance
{
	if (sharedInstance == nil)
	{
		sharedInstance = [[super allocWithZone:NULL] init];
		[sharedInstance initDateFormatter];
        [sharedInstance populateErrorMessageTable];
	}
	return sharedInstance;
}


#pragma mark Logging Methods
- (BOOL) log:(NSString*) text Level:(MCLogLevel) level
{
	[self checkAndSetCurrentLogFile];
	[logFile seekToEndOfFile]; 
	NSString * logContent = [NSString stringWithFormat:@"#%@ %@ %@# %@\n",
							 levelNames[level],
							 [self getFormattedTimeStamp], 
							 [NSThread currentThread],
							 text];
	[logFile writeData:[logContent dataUsingEncoding:NSUTF8StringEncoding]];

	NSLog (@"#%@# %@", levelNames[level], text);
    
	msgCount++;
	return TRUE;
}


#pragma mark Log Summary Methods
- (NSString*) createLogSummaryWithSettings
{
	// Make sure logging is all set up
	[self checkAndSetCurrentLogFile];
	
	// Determine the path to the summary file
	NSString* logPath = [MCLogging obtainLogsDir];
	__autoreleasing NSString* summaryFullPath = [logPath stringByAppendingPathComponent:@"summary.txt"];
	
	// If a summary file exists, then delete it
	NSFileManager* fileManager = [NSFileManager defaultManager];
	if ([fileManager fileExistsAtPath:summaryFullPath])
	{
		[fileManager removeItemAtPath:summaryFullPath error:NULL];
	}
	
	// Create a new summary file
	[fileManager createFileAtPath:summaryFullPath contents:nil attributes:nil];
	
	// Get a handle to the summary file
	NSFileHandle* summaryFile = [NSFileHandle fileHandleForWritingAtPath:summaryFullPath];
	
	// Log user, device configuration, language info and view stack at the beginning of file
	NSMutableString * logContent = [[NSMutableString alloc] init];
	
	// Log username
	[logContent appendString:[NSString stringWithFormat:@"#%@ %@# User: %@\n", levelNames[MC_LOG_INFO], [self getFormattedTimeStamp], ([ExSystem sharedInstance].userName != nil ? [ExSystem sharedInstance].userName : @"Unknown")]];

	// Log device description
	[logContent appendString:[NSString stringWithFormat:@"#%@ %@# %@\n", levelNames[MC_LOG_INFO], [self getFormattedTimeStamp], [MCLogging getDeviceDesc]]];

	// Log language, locale
	NSUserDefaults* defs = [NSUserDefaults standardUserDefaults];
	NSArray* languages = [defs objectForKey:@"AppleLanguages"];
	NSString* preferredLang = languages[0];
	[logContent appendString:[NSString stringWithFormat:@"#%@ %@# Language: %@, Locale: %@\n", levelNames[MC_LOG_INFO], [self getFormattedTimeStamp], preferredLang, [[NSLocale currentLocale] localeIdentifier]]];
	
	// Log view stack
	[logContent appendString:[NSString stringWithFormat:@"View stack: %@\n", [self createViewStackDescription]]];
	
	[summaryFile writeData:[logContent dataUsingEncoding:NSUTF8StringEncoding]];
	
	
	// Beginning with the oldest log file...
	NSString* logName = [self getOldestLogFileName];
	
	// Write the content of each log file to the summary file, then move onto the nex† log file
	while ([fileManager fileExistsAtPath:logName])
	{
		NSFileHandle* log = [NSFileHandle fileHandleForReadingAtPath:logName];
		NSData* logData = [log readDataToEndOfFile];
		if (logData != nil && logData.length > 0)
		{
			[summaryFile writeData:logData];
		}
		[log closeFile];
		
		// If we've written the current log file (logFileName), then we're done.
		if ([logName isEqualToString:logFileName])
		{
			break;
		}
		
		logName = [self getNextLogFileName:logName];
	}
	
	// Close the summary file
	[summaryFile closeFile];
	
	return summaryFullPath;
}

-(NSString*) createViewStackDescription
{
	__autoreleasing NSMutableString *viewStackDescription = [[NSMutableString alloc] initWithString:@""];
	UIApplication *app = [UIApplication sharedApplication];
	if (app)
	{
		id<UIApplicationDelegate> appDelegate = [app delegate];
		if (appDelegate)
		{
			if ([appDelegate respondsToSelector:@selector(getNavigationController)])
			{
				UINavigationController *navController = [(ConcurMobileAppDelegate *) appDelegate getNavigationController];
				if (navController)
				{
					NSArray *viewControllers = navController.viewControllers;
					if (viewControllers)
					{
						NSUInteger numViewControllers = [viewControllers count];
						for (int i = 0; i < numViewControllers; i++)
						{
							UIViewController *eachViewController = viewControllers[i];
							Class viewControllerClass = [eachViewController class];
							if (viewControllerClass)
							{
								NSString *viewControllerDescription = [viewControllerClass description];
								if (viewControllerDescription)
								{
									if (i > 0)
									{
										[viewStackDescription appendString:@", "];
									}
									[viewStackDescription appendString:viewControllerDescription];
								}
							}
						}
					}
				}
			}
		}
	}

	if ([viewStackDescription length] > 0)
	{
		return viewStackDescription;
	}
	else
	{
		return @"View stack unavailable"; // Do not localize
	}
}


#pragma mark Error Methods
- (void) getErrorsFromFile: (NSString*) fileName outputArray: (NSMutableArray *) result
{
	// Read last file and second to the last file
	NSError *err = NULL;
	NSString *fileContents = [NSString stringWithContentsOfFile:fileName encoding: NSUTF8StringEncoding error:&err];
	NSArray *lines = [fileContents componentsSeparatedByString:@"\n"];
	NSUInteger lineCount = lines.count;
	int curLine = 0;
	MCLogInfo * curInfo = nil;
	while (curLine < lineCount) {
		NSString *curLineTxt = lines[curLine];
		if ([curLineTxt length]>10&& 
			[curLineTxt characterAtIndex:0] == '#' 
			&& [curLineTxt characterAtIndex:5] == ' ' 
			)
		{
			NSRange aRange, bRange;
			aRange.location = 1;
			aRange.length = [curLineTxt length]-1;
			
			NSCharacterSet* cSet = [NSCharacterSet characterSetWithCharactersInString:@"#"];
			NSRange tmpRange = [curLineTxt rangeOfCharacterFromSet:cSet options:NSLiteralSearch range:aRange];
			if (tmpRange.location > 1) 
			{
				bRange.location = 6;
				bRange.length = tmpRange.location - 6;
				
				curInfo = [[MCLogInfo alloc] init];
				
				[result addObject: curInfo];
				// match ref count from init
				
				aRange.location = 1;
				aRange.length = 4;
				if (NSOrderedSame == [curLineTxt compare:(NSString*)levelNames[MC_LOG_ERRO] options:NSLiteralSearch range: aRange])
				{ 
					curInfo.level = MC_LOG_ERRO;
				} else if (NSOrderedSame ==
						   [curLineTxt compare:(NSString*)levelNames[MC_LOG_WARN] options:NSLiteralSearch range: aRange])
				{
					curInfo.level = MC_LOG_WARN;
				} else if (NSOrderedSame ==
						   [curLineTxt compare:(NSString*)levelNames[MC_LOG_INFO] options:NSLiteralSearch range: aRange])
				{
					curInfo.level = MC_LOG_INFO;
				} else {
					curInfo.level = MC_LOG_DEBU;
				}
				
				curInfo.text = [curLineTxt substringFromIndex:(tmpRange.location+1)];
				
				curInfo.timestamp = [self getTimeStampFromString:[curLineTxt substringWithRange:bRange]];
			}
			else 
			{
				curInfo.text = [NSString stringWithFormat:@"%@\n%@", curInfo.text, curLineTxt];
			}
		}
		else
		{
			curInfo.text = [NSString stringWithFormat:@"%@\n%@", curInfo.text, curLineTxt];
		}
		curLine ++;
	}
}

- (NSArray*) getLastErrors
{
	[self checkAndSetCurrentLogFile];

	__autoreleasing NSMutableArray * result = [[NSMutableArray alloc] initWithCapacity:20];

	NSString* prevLogFileName = [self getPrevLogFileName];
	[self getErrorsFromFile:prevLogFileName outputArray: result];
	[self getErrorsFromFile:logFileName outputArray: result];
	return result;
}

-(void) logErrorWithId:(NSString*)errorId Message:(NSString*)errorMessage Level:(int)loggerLevel
{
    NSString *i = errorMessage;
    NSString *m = errorId;
    NSString* message = logDict[i];
    if (message == nil)
    {
        message = [@"" stringByPaddingToLength: 50 withString: @" " startingAtIndex:0];
    }
    NSRange range = NSMakeRange(loggerLevel, [m length]);
    message = [message stringByReplacingCharactersInRange:range withString:m];
    logDict[i] = message;
}

-(NSString*) getMessageForField:(NSString*)field
{
    NSString *val = logDict[field];
    return (val == nil ? @"" : [val stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]]);
}


+(NSString*) getDeviceDesc
{
	UIDevice * curDev = [UIDevice currentDevice];
	// User-Agent: <Product>/<Version> (<Manufacturer>; <Model>; <OSVersion>; <Carrier>)
	
	NSString* product;
	NSString *ver = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
	
    if ( [[ExSystem sharedInstance] isBreeze])
        product = @"Concur Mobile - Breeze";
    else if ([[ExSystem sharedInstance] isGovernment])
        product = @"Concur Mobile - Government";
    else
        product = @"Concur Mobile - Corporate";
    
	NSString * devDesc = [NSString stringWithFormat:@"%@/%@ (%@; %@; %@ %@)", product, ver, @"Apple",
                          curDev.model, curDev.systemName, curDev.systemVersion];
	return devDesc;
}


#pragma mark Memory Methods
@end
