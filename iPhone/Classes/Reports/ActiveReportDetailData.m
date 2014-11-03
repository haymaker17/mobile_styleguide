//
//  ActiveReportDetailData.m
//  ConcurMobile
//
//  Created by yiwen on 4/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ActiveReportDetailData.h"
#import "DataConstants.h"
#import "CacheData.h"

@implementation ActiveReportDetailData


-(NSString *)getMsgIdKey
{
	return ACTIVE_REPORT_DETAIL_DATA;
}

-(void) flushData
{
	[super flushData];
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rptKey = parameterBag[@"ID_KEY"];

    NSString* role = parameterBag[@"ROLE_CODE"];
    if (role == nil)
        role = ROLE_EXPENSE_TRAVELER;
    
// MOB-10862 V4 is same as V3 + tax forms if present
        self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetReportDetailV4/%@/%@",
                     [ExSystem sharedInstance].entitySettings.uri,  parameterBag[@"ID_KEY"], role ];
    
        
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}


#pragma mark -
#pragma mark ArchivedResponder Methods
-(void)saveToLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
	// Make sure a valid rpt object is stored in cache
	if (self.rpt != nil && self.rpt.rptKey != nil)
	{
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		
		NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@_%@", ACTIVE_REPORT_DETAIL_DATA, self.rpt.rptKey, uId]];
		@synchronized(cacheData)
		{
			[NSKeyedArchiver archiveRootObject:self.rpt toFile: archivePath];
			if (![[self getMsgIdKey] isEqualToString:ACTIVE_REPORT_DETAIL_DATA])
            {
				[cacheData saveCacheMetaData:ACTIVE_REPORT_DETAIL_DATA UserID:uId RecordKey:self.rpt.rptKey];
            }
            
            //-(CacheMetaData *)saveCacheMetaData:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;
            //-(CacheMetaData *)saveCacheMetaData:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey//
		}
	}
}

-(void)loadFromLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
    NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@_%@", ACTIVE_REPORT_DETAIL_DATA, self.rptKey, uId]];
	@synchronized(cacheData)
	{
		ReportData* obj = [NSKeyedUnarchiver unarchiveObjectWithFile:archivePath];
		self.rpt = obj;
	}
}



@end
