//
//  SearchApproverData.h
//  ConcurMobile
//
//  Created by yiwen on 8/26/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ApproverInfo.h"

@interface SearchApproverData : MsgResponderCommon 
{
    NSString        *rptKey;
    NSString        *searchField;
    NSString        *query;
    NSMutableArray  *approverList;
    ApproverInfo    *approver;
}

@property (strong, nonatomic) NSString          *rptKey;
@property (strong, nonatomic) NSString          *searchField;
@property (strong, nonatomic) NSString          *query;
@property (strong, nonatomic) NSMutableArray	*approverList;
@property (strong, nonatomic) ApproverInfo      *approver;

-(Msg *)newMsg: (NSMutableDictionary *)parameterBag;
-(NSString *)getMsgIdKey;


@end
