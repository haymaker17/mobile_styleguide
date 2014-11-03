//
//  IgniteChatterConversationVCDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/10/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityChatterFeedEntry.h"

@protocol IgniteChatterConversationVCDelegate <NSObject>
- (void)replyToConversationForFeedEntry:(EntityChatterFeedEntry*)entry;
@end
