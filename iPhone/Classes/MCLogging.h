//
//  MCLogging.h
//  ConcurMobile
//
//  Created by yiwen on 12/21/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


#define MC_LOG_ERRO 3
#define MC_LOG_WARN 2
#define MC_LOG_INFO 1
#define MC_LOG_DEBU 0

typedef unsigned int MCLogLevel;

// Mobile Client logging
@interface MCLogging : NSObject 
{
	NSFileHandle* logFile;
	NSString* logFileName;
	MCLogLevel logLevel;
	NSDateFormatter *dateFormatter;
    NSMutableDictionary *logDict;
};
// need to be dynamic
@property (strong, nonatomic) NSFileHandle *logFile;
@property (strong, nonatomic) NSString*logFileName;
@property MCLogLevel logLevel;
@property (strong, nonatomic) NSMutableDictionary *logDict;

+ (MCLogging*) getInstance;
- (BOOL) log :(NSString*) text Level:(MCLogLevel) level;
- (void) initDateFormatter;
- (NSString*) createLogSummaryWithSettings;
-(NSString*) createViewStackDescription;
- (NSArray*) getLastErrors;
+(NSString*) getDeviceDesc;
+ (NSString *) obtainLogsDir;
-(void) logErrorWithId:(NSString*)errorId Message:(NSString*)errorMessage Level:(int)loggerLevel;
-(NSString*) getMessageForField:(NSString*)field;

@end
