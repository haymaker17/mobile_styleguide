//
//  ReportReceiptInfoManager.m
//  ConcurMobile
//
//  Created by yiwen on 11/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "ReportReceiptInfoManager.h"

static ReportReceiptInfoManager *sharedInstance;

@implementation ReportReceiptInfoManager

+(ReportReceiptInfoManager*)sharedInstance
{
 	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[ReportReceiptInfoManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(EntityReportReceiptInfo *) makeNew
{
    return ((EntityReportReceiptInfo *)[super makeNew:@"EntityReportReceiptInfo"]);
}


-(void) clearAll
{
    NSArray *aHomeData = [self fetchAll:@"EntityReportReceiptInfo"];
    
    for(EntityReportReceiptInfo *entity in aHomeData)
    {
        [self deleteObj:entity];
    }
}

-(NSArray*) getEntryReceiptInfoForRpt:(NSString*) rptKey
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityReportReceiptInfo" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(rptKey = %@)", rptKey];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(EntityReportReceiptInfo *) fetch:(NSString *) rpeKey withRptKey:(NSString *)rptKey
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityReportReceiptInfo" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(rpeKey = %@)", rpeKey];
    if (![rpeKey length] && [rptKey length])
        [NSPredicate predicateWithFormat:@"(rptKey == %@ and rpeKey == <null>)", rptKey];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}

-(EntityReportReceiptInfo *) fetchOrMake:(NSString *) rpeKey withRptKey:(NSString *)rptKey
{
    EntityReportReceiptInfo* result = [self fetch:rpeKey withRptKey:rptKey];
    if(result == nil)
    {
        result = [self makeNew];
        result.rpeKey = rpeKey;
        result.rptKey = rptKey;
    }
    return result;
}

-(EntityReportReceiptInfo *)getEntryReceiptInfo:(NSString*) rpeKey withRptKey:(NSString *)rptKey
{
    return [self fetch:rpeKey withRptKey:rptKey];  
}

-(void) updateEntryReceiptInfo:(NSString*) rpeKey withRptKey:(NSString *)rptKey withPath:(NSString*)path withReceiptId:(NSString*) receiptId
{
    EntityReportReceiptInfo* receipt = [self fetchOrMake:rpeKey withRptKey:rptKey];
    if (receipt != nil)
    {
        receipt.imagePath = path;
        receipt.dateLastModified = [NSDate date];
        if (![receiptId length])
            receipt.receiptId = receiptId;
    
        [self saveIt:receipt];
    }
    
}

@end
