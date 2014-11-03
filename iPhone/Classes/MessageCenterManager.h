//
//  MessageCenterManager.h
//  ConcurMobile
//
//  Created by Richard Puckett on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Parse/Parse.h>
#import "MessageCenterMessage.h"

@protocol MessageCenterListener;

@interface MessageCenterManager : NSObject

@property (strong, nonatomic) NSMutableArray *listeners;
@property (strong, nonatomic) NSMutableArray *messages;

+ (MessageCenterManager *)sharedInstance;

- (void)addListener:(id<MessageCenterListener>)listener;
- (void)addMessage:(MessageCenterMessage *)message;
- (void)addMessage:(MessageCenterMessage *)message silently:(BOOL)silently;
- (BOOL)hasMessage:(NSString *)messageId;
- (MessageCenterMessage *)messageAtIndex:(NSUInteger)index;
- (MessageCenterMessage *)messageForId:(NSString *)messageId;
- (NSUInteger)numMessagesForType:(MessageType)type;
- (void)removeListener:(id<MessageCenterListener>)listener;
- (void)removeMessage:(MessageCenterMessage *)message;
- (void)removeMessageAtIndex:(NSUInteger *)index;
- (void)removeMessageForId:(NSString *)messageId;
- (void)setType:(MessageType)type forMessage:(MessageCenterMessage *)message;

@end

@protocol MessageCenterListener <NSObject>
@optional
- (void)messageCenter:(MessageCenterManager *)manager didAddMessage:(MessageCenterMessage *)message;
- (void)messageCenter:(MessageCenterManager *)manager didRemoveMessage:(MessageCenterMessage *)message;
- (void)messageCenter:(MessageCenterManager *)manager didChangeMessageType:(MessageCenterMessage *)message;
@end