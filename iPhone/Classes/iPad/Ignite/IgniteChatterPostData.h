//
//  IgniteChatterPostData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/3/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MsgResponder.h"

@interface IgniteChatterPostData : MsgResponder
@property (nonatomic, readwrite, copy) NSString *postId;
@end
