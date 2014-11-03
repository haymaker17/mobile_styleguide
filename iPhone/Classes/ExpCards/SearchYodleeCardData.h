//
//  SearchYodleeCardData.h
//  ConcurMobile
//
//  Created by yiwen on 11/7/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"
#import "YodleeCardProvider.h"

@interface SearchYodleeCardData : MsgResponderCommon {

}

@property (strong, nonatomic) NSString          *query;
@property (strong, nonatomic) NSMutableArray	*cardList;
@property (strong, nonatomic) YodleeCardProvider      *card;
@property BOOL  isPopular;

-(Msg *)newMsg: (NSMutableDictionary *)parameterBag;
-(NSString *)getMsgIdKey;

@end
