//
//  ReportCreatedDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 6/6/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportData.h"

@protocol ReportCreatedDelegate <NSObject>
-(void) reportCreated:(ReportData*) rpt;
@end
