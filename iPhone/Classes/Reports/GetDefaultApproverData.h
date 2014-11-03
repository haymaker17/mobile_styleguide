//
//  GetDefaultApproverData.h
//  ConcurMobile
//
//  Created by yiwen on 12/6/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"
#import "ApproverInfo.h"


@interface GetDefaultApproverData : MsgResponderCommon
{
    ApproverInfo        *approver;
}

@property (strong, nonatomic) ApproverInfo                  *approver;

@end
