//
//  DeleteReportData.h
//  ConcurMobile
//
//  Created by yiwen on 3/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"
@interface DeleteReportData : MsgResponderCommon 
{
	NSString				*rptKey;
	ActionStatus			*actionStatus;
}

@property (nonatomic, strong) ActionStatus		*actionStatus;
@property (nonatomic, strong) NSString			*rptKey;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end
