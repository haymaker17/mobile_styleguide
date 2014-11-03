//
//  ActiveReportDetailData.h
//  ConcurMobile
//
//  Created by yiwen on 4/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportDetailDataBase.h"
#import "ArchivedResponder.h"

@interface ActiveReportDetailData : ReportDetailDataBase <ArchivedResponder>
{
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(void) flushData;

@end
