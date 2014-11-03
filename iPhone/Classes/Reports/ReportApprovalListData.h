//
//  ReportApprovalListData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportListDataBase.h"
#import "Msg.h"

@interface ReportApprovalListData : ReportListDataBase 
{
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

- (NSString*) getReportElementName;

@end
