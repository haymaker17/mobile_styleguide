//
//  ReportSummaryViewController.h
//  ConcurMobile
//
//  Created by yiwen on 5/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportHeaderViewControllerBase.h"
#import "ReportCreatedDelegate.h"

@interface ReportSummaryViewController : ReportHeaderViewControllerBase 
{
    id<ReportCreatedDelegate>       __weak _delegate;
}

@property (weak, nonatomic) id<ReportCreatedDelegate>     delegate;

// Init data
- (void)setSeedData:(NSDictionary*)pBag;
- (void)setSeedData:(ReportData*)report role:(NSString*) curRole;
- (void)loadReport:(ReportData*) report;
-(NSDictionary*) getComments;

-(BOOL) isNewReport;

@end
