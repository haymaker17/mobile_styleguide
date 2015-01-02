//
//  ReportManager.m
//  ConcurMobile
//
//  Created by yiwen on 6/9/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReportManager.h"
#import "ListDataBase.h"
#import "ReportData.h"

static ReportManager *sharedInstance;
@implementation ReportManager


+(ReportManager*)sharedInstance
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
				sharedInstance = [[ReportManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(EntityReport *) makeNew
{
    return ((EntityReport *)[super makeNew:@"EntityReport"]);
}


-(void) clearAll
{
    NSArray *aHomeData = [self fetchAll:@"EntityReport"];
    
    for(EntityReport *entity in aHomeData)
    {
        [self deleteObj:entity];
    }
}

-(NSArray *) searchReportByPrefix:(NSString *)prefix
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityReport" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(reportName beginswith %@)", prefix];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSString*) generateDefaultReportName:(NSString*) rptDate
{
    NSString* rptStr = [Localizer getLocalizedText:@"Report"];
    if ([rptStr length]>20 || rptStr == nil) // MOB-6168
        rptStr = @"Report";
    
    NSString* newRptNamePrefix = [NSString stringWithFormat:@"%@ %@", 
                                  [CCDateUtilities formatDateShortStyle:rptDate],
                                  rptStr];
    newRptNamePrefix = [newRptNamePrefix stringByReplacingOccurrencesOfString:@"/" withString:@"."];
    
    NSArray *rptList = [self searchReportByPrefix:newRptNamePrefix];
    if (rptList != nil && [rptList count] >0)
    {
        newRptNamePrefix = [NSString stringWithFormat:@"%@ #%lu", newRptNamePrefix, [rptList count]+1];
    }
    return newRptNamePrefix;
}

-(NSManagedObject *) fetchOrMake:(NSString *) entityName key:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(rptKey = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return [self makeNew:entityName];
}


-(void)saveUnsubmittedReportList:(NSArray*)rptList
{
    [self clearAll];
    for (ReportData *rpt in rptList) 
    {
        EntityReport *entity = (EntityReport *)[self fetchOrMake:@"EntityReport" key:rpt.rptKey];
        entity.rptKey = rpt.rptKey;
        entity.reportName = rpt.reportName;
        entity.apsKey = rpt.apsKey;
        entity.apvStatusName = rpt.apvStatusName;
        entity.totalPostedAmount = rpt.totalPostedAmount;
        entity.crnCode = rpt.crnCode;
        entity.hasException = rpt.hasException;
        entity.employeeName = rpt.employeeName;
        entity.reportDate = [CCDateUtilities formatDateToNSDateYYYYMMddTHHmmss:rpt.reportDate];
        [self saveIt:entity];
    }
}

@end
