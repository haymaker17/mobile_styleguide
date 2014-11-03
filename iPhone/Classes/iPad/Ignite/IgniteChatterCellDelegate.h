//
//  IgniteChatterCellDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class IgniteChatterCell;

@protocol IgniteChatterCellDelegate <NSObject>
- (void)replyButtonPressedForCell:(IgniteChatterCell*)cell;
- (void)conversationButtonPressedForCell:(IgniteChatterCell*)cell;
- (void)tripButtonPressedForCell:(IgniteChatterCell*)cell;
@end
