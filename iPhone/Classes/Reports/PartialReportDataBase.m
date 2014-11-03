//
//  PartialReportDataBase.m
//  ConcurMobile
//
//  Created by yiwen on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "PartialReportDataBase.h"
#import "DataConstants.h"
#import "CacheData.h"

@implementation PartialReportDataBase

#pragma mark -
#pragma mark ArchivedResponder Methods
-(ReportData*) updateReportObject:(ReportData*) obj
{
	return obj;
}

-(void)saveToLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
	NSString *msgId = ACTIVE_REPORT_DETAIL_DATA;
	// Make sure a valid rpt object is stored in cache
	if (self.rpt != nil && (self.rpt.rptKey != nil||(self.rpt.entry != nil && self.rpt.entry.rpeKey!=nil)))
	{
		@synchronized(cacheData)
		{
			NSString *theRptKey = self.rptKey != nil? self.rptKey: (self.rpt.rptKey == nil? self.rpt.entry.rptKey : self.rpt.rptKey);
			// TODO - synchronize on this report meta data only
			//NSString *cacheKey = [NSString stringWithFormat:@"%@_%@_%@", msgId, recordKey, uId];
			NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@_%@", msgId, theRptKey, uId]];
			ReportData* obj = [NSKeyedUnarchiver unarchiveObjectWithFile:archivePath];
			obj = [self updateReportObject:obj];
			// Save the updated report
			if (obj != nil)
			{
				[NSKeyedArchiver archiveRootObject:obj toFile: archivePath];
				[cacheData saveCacheMetaData:msgId UserID:uId RecordKey:theRptKey];
			}
		}
	}
}

-(void)loadFromLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
}

@end
