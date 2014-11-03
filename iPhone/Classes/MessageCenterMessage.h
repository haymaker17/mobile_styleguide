//
//  MessageCenterMessage.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, MessageAction) {
    MessageActionCreated,
    MessageActionDeleted,
    MessageActionPurchased,
    MessageActionUnknown
};

typedef NS_ENUM(NSUInteger, MessageStatus) {
    MessageStatusNew,
    MessageStatusRead,
    MessageStatusUnread,
    MessageStatusUnknown
};

typedef NS_ENUM(NSUInteger, MessageType) {
    MessageTypeAny,
    MessageTypeRead,
    MessageTypeUnread
};

@interface MessageCenterMessage : NSObject <NSCoding>

@property (assign, nonatomic) MessageType type;
@property (assign, nonatomic) MessageStatus status;
@property (copy, nonatomic) NSString *messageId;
@property (copy, nonatomic) NSString *iconName;
@property (copy, nonatomic) NSString *title;
@property (copy, nonatomic) NSString *message;
@property (copy, nonatomic) NSString *commandName;
@property (copy, nonatomic) NSString *stringExtra;
@property (assign) BOOL isSilent;

@end
