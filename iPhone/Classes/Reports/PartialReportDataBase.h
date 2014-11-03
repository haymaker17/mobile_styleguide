//
//  PartialReportDataBase.h
//  ConcurMobile
//
//  Created by yiwen on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportDetailDataBase.h"
#import "ArchivedResponder.h"
#import "ReportData.h"

@interface PartialReportDataBase : ReportDetailDataBase<ArchivedResponder> {

}
// Subclass overrides this API to update a portion of the report
-(ReportData*) updateReportObject:(ReportData*) rpt;

@end
