//
//  IgniteItinDetailSocialFeedDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class EntityChatterFeedEntry;

@protocol IgniteItinDetailSocialFeedDelegate <NSObject>
- (void)replyToChatterEntry:(EntityChatterFeedEntry*)entry;
- (void)displayConversationForChatterEntry:(EntityChatterFeedEntry*)entry fromRect:(CGRect)rect inView:(UIView*)view;
@end
