//
//  ReportSelectedDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/13/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportData.h"

@protocol ReportSelectedDelegate <NSObject>
-(void) reportSelected:(ReportData*) rpt;
@end
