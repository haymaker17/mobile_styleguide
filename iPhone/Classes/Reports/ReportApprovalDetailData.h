//
//  ReportApprovalDetailData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportDetailDataBase.h"

@interface ReportApprovalDetailData : ReportDetailDataBase 
{
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

//- (void) flushData;
@end
