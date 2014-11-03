//
//  ReportActionDelegate.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/11/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ReportData.h"

@protocol ReportActionDelegate <NSObject>

- (void)didChooseReport:(ReportData *)reportData;

@end
