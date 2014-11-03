//
//  SaveReportData.h
//  ConcurMobile
//
//  Created by yiwen on 11/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SubmitReportData.h"

// Responses for SaveReportData and SubmitReportData have the same format and contains a ReportDetail object
// B/c copy down, save report header can alter entries/itemizations
@interface SaveReportData : SubmitReportData 
{
	ReportData			*report;
	NSArray				*fields;
}

@property(nonatomic, strong) ReportData		*report;
@property(nonatomic, strong) NSArray		*fields;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(BOOL) isFieldEmpty:(NSString*)val;

@end
