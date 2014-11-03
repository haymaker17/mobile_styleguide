//
//  MessageCenterManager.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AnalyticsManager.h"
#import "MessageCenterManager.h"


@implementation MessageCenterManager

__strong static id _sharedInstance = nil;

+ (MessageCenterManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.listeners = [[NSMutableArray alloc] init];
        self.messages = [self loadMessages];
    }
    
    return self;
}

- (void)addListener:(id<MessageCenterListener>)listener; {
    if (![self.listeners containsObject:listener]) {
        [self.listeners addObject:listener];
    }
}

- (void)addMessage:(MessageCenterMessage *)message {
    [self addMessage:message silently:NO];
}

- (void)addMessage:(MessageCenterMessage *)message silently:(BOOL)silently {
    if ([self numMessagesForType:MessageTypeUnread] == 0) {
        [[AnalyticsManager sharedInstance] logCategory:@"Message Center" withName:@"Badged"];
    }
    
    [self.messages addObject:message];
    
    [self storeMessages];
    
    if (!silently) {
        [self notifyListenersOfMessageAdded:message];
    }
}

- (BOOL)hasMessage:(NSString *)messageId {
    for (MessageCenterMessage *message in self.messages) {
        if ([message.messageId isEqualToString:messageId]) {
            return YES;
        }
    }
    
    return NO;
}

- (NSMutableArray *)loadMessages {
    NSMutableArray *messages = [[NSMutableArray alloc] init];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSArray *storedMessages = [defaults arrayForKey:@"message_center_messages"];
    
    for (NSData *messageData in storedMessages) {
        MessageCenterMessage *message = [NSKeyedUnarchiver unarchiveObjectWithData:messageData];
        [messages addObject:message];
    }
    
    return messages;
}

- (MessageCenterMessage *)messageAtIndex:(NSUInteger)index {
    return [self.messages objectAtIndex:index];
}

- (MessageCenterMessage *)messageForId:(NSString *)messageId {
    for (MessageCenterMessage *message in self.messages) {
        if ([message.messageId isEqualToString:messageId]) {
            return message;
        }
    }
    
    return nil;
}

- (void)notifyListenersOfMessageAdded:(MessageCenterMessage *)message {
    for (id<MessageCenterListener> listener in self.listeners) {
        if ([listener respondsToSelector:@selector(messageCenter:didAddMessage:)]) {
            [listener messageCenter:self didAddMessage:message];
        }
    }
}

- (void)notifyListenersOfMessageRemoved:(MessageCenterMessage *)message {
    for (id<MessageCenterListener> listener in self.listeners) {
        if ([listener respondsToSelector:@selector(messageCenter:didRemoveMessage:)]) {
            [listener messageCenter:self didRemoveMessage:message];
        }
    }
}

- (void)notifyListenersOfMessageTypeChanged:(MessageCenterMessage *)message {
    for (id<MessageCenterListener> listener in self.listeners) {
        if ([listener respondsToSelector:@selector(messageCenter:didChangeMessageType:)]) {
            [listener messageCenter:self didChangeMessageType:message];
        }
    }
}

- (NSUInteger)numMessagesForType:(MessageType)type {
    NSUInteger numMessages = 0;
    
    switch (type) {
        case MessageTypeAny:
            numMessages = [self.messages count];
            break;
        case MessageTypeRead:
        case MessageTypeUnread:
            numMessages = [self internalNumMessagesForType:type];
            break;
    }
    
    return numMessages;
}

- (NSUInteger)internalNumMessagesForType:(MessageType)type {
    NSUInteger numMessages = 0;
    
    for (MessageCenterMessage *message in self.messages) {
        if (message.type == type) {
            numMessages++;
        }
    }
    
    return numMessages;
}

- (void)removeListener:(id<MessageCenterListener>)listener; {
    [self.listeners removeObject:listener];
}

- (void)removeMessage:(MessageCenterMessage *)message {
    [self.messages removeObject:message];
    
    [self storeMessages];
    
    [self notifyListenersOfMessageRemoved:message];
}

- (void)removeMessageAtIndex:(NSUInteger *)index {
    MessageCenterMessage *message = [self.messages objectAtIndex:index];
    
    [self.messages removeObjectAtIndex:index];
    
    [self storeMessages];
    
    [self notifyListenersOfMessageRemoved:message];
}

- (void)removeMessageForId:(NSString *)messageId {
    for (MessageCenterMessage *message in self.messages) {
        if (message.messageId == messageId) {
            [self removeMessage:message];
            return;
        }
    }
}

- (void)storeMessages {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSMutableArray *messageData = [[NSMutableArray alloc] init];
    
    for (MessageCenterMessage *message in self.messages) {
        NSData *data = [NSKeyedArchiver archivedDataWithRootObject:message];
        
        [messageData addObject:data];
    }
    
    [defaults setObject:messageData forKey:@"message_center_messages"];
    
    [defaults synchronize];
}

- (void)setType:(MessageType)type forMessage:(MessageCenterMessage *)message {
    message.type = type;
    
    [self storeMessages];
    
    [self notifyListenersOfMessageTypeChanged:message];
    
    if ([self numMessagesForType:MessageTypeUnread] == 0) {
        [[AnalyticsManager sharedInstance] logCategory:@"Message Center" withName:@"Unbadged"];
    }
}

@end
