//
//  ActiveReportListData.h
//  ConcurMobile
//
//  Created by yiwen on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportListDataBase.h"
#import "Msg.h"

@interface ActiveReportListData : ReportListDataBase {
	NSArray* unsubmittedRpts;
}

@property (nonatomic, strong) NSArray			*unsubmittedRpts;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

- (NSArray *) getUnsubmittedReports;

@end
