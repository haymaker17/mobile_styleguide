//
//  CarMileageData.h
//  ConcurMobile
//
//  Created by ernest cho on 3/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SelectReportViewController.h"

@interface CarMileageData : NSObject<ExMsgRespondDelegate>

- (void)userSelectedReport:(NSString*) rptKey rpt:(ReportData*) rpt inView:(SelectReportViewController *)view;

@end
