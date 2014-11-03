//
//  RecallReportData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "RecallReportData.h"
#import "DataConstants.h"
#import "ExSystem.h"
#import "ReportData.h"

@implementation RecallReportData
@synthesize rptKey, actionStatus;

-(NSString *)getMsgIdKey
{
	return RECALL_REPORT_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    
	self.rptKey = parameterBag[@"RPT_KEY"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/RecallExpenseReport/%@", 
                 [ExSystem sharedInstance].entitySettings.uri, self.rptKey];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	
	return msg;
}




- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.actionStatus = nil;
		actionStatus = [[ActionStatus alloc] init];
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.actionStatus.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.actionStatus.errMsg = buildString;
	}
}	


-(void)saveToLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
	NSString *msgId = ACTIVE_REPORT_DETAIL_DATA;
	// Make sure a valid rpt object is stored in cache
	if ([ self.rptKey length])
	{
		@synchronized(cacheData)
		{
			NSString *theRptKey = self.rptKey;
			// TODO - synchronize on this report meta data only
			//NSString *cacheKey = [NSString stringWithFormat:@"%@_%@_%@", msgId, recordKey, uId];
			NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@_%@_%@", msgId, theRptKey, uId]];
			ReportData* obj = [NSKeyedUnarchiver unarchiveObjectWithFile:archivePath];
            obj.apsKey = @"A_RESU";
            obj.apvStatusName = [@"A_RESU" localize]; // Sent back to employee

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
