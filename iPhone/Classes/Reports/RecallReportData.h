//
//  RecallReportData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"
#import "ArchivedResponder.h"

@interface RecallReportData : MsgResponderCommon <ArchivedResponder>
{
	NSString				*rptKey;
	ActionStatus			*actionStatus;
}

@property (nonatomic, strong) ActionStatus		*actionStatus;
@property (nonatomic, strong) NSString			*rptKey;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end
