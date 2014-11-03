//
//  ReportManager.h
//  ConcurMobile
//
//  Created by yiwen on 6/9/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntityReport.h"


@interface ReportManager : BaseManager 
{
    
}

+(ReportManager*)sharedInstance;
-(EntityReport *) makeNew;

-(void) clearAll;
-(NSArray *) searchReportByPrefix:(NSString *)prefix;
-(void)saveUnsubmittedReportList:(NSArray*)rptList;
-(NSString*) generateDefaultReportName:(NSString*) rptDateStr;

@end
