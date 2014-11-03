//
//  GoGoNotificationHandler.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AnalyticsManager.h"
#import "GoGoMessage.h"
#import "GoGoNotificationHandler.h"
#import "GoGoOfferViewController.h"
#import "MessageCenterViewController.h"

@implementation GoGoNotificationHandler

#pragma mark - NotificationHandler Protocol

// Called when app is invoked directly from notification.
//
- (void)processNotificationEvent:(NotificationEvent *)event
             forApplicationState:(UIApplicationState)applicationState {
    
    NSString *actionParam = event.data[@"action"];
    NSString *messageId = event.data[@"messageId"];
    BOOL isSilent = [event.data[@"s"] boolValue];
    
    MessageAction messageAction = [self actionFromString:actionParam];
    
    if (messageAction == MessageActionUnknown) {
        NSLog(@"Skipping unknown message action: %@", actionParam);
    } else {
        [self handleMessageForId:messageId
             forApplicationState:applicationState
                andMessageAction:messageAction
                          silent:isSilent];
    }
}

// Called when app is launched from app icon and an offer was pulled
// from the inter-clouds.
//
- (void)processOffer:(GoGoOffer *)offer
 forApplicationState:(UIApplicationState)applicationState {
    
    MessageStatus status = [self statusFromString:offer.status];
    
    if (status == MessageStatusNew) {
        [self handleMessageForId:offer.messageId
             forApplicationState:applicationState
                andMessageAction:MessageActionCreated
                          silent:offer.isSilent];
    }
}

#pragma mark - Business Logic

- (MessageAction)actionFromString:(NSString *)actionString {
    MessageAction messageAction = MessageActionUnknown;
    
    if ([actionString isEqualToString:@"CREATED"]) {
        messageAction = MessageActionCreated;
    } else if ([actionString isEqualToString:@"DELETED"]) {
        messageAction = MessageActionDeleted;
    } else if ([actionString isEqualToString:@"PURCHASED"]) {
        messageAction = MessageActionPurchased;
    }
    
    return messageAction;
}

- (void)handleMessageForId:(NSString *)messageId
       forApplicationState:(UIApplicationState)applicationState
          andMessageAction:(MessageAction)messageAction
                    silent:(BOOL)isSilent {
    
    switch (messageAction) {
        case MessageActionCreated:
            [self populateMessageCenterForMessageId:messageId
                                forApplicationState:(UIApplicationState)applicationState
                                   andMessageAction:messageAction
                                             silent:isSilent];
            break;
        case MessageActionDeleted:
            [[MessageCenterManager sharedInstance] removeMessageForId:messageId];
            break;
        default:
            break;
    }
}

- (void)populateMessageCenterForMessageId:(NSString *)messageId
                      forApplicationState:(UIApplicationState)applicationState
                         andMessageAction:(MessageAction)messageAction
                                   silent:(BOOL)isSilent {
    
    //NSLog(@"GoGoNotificationHandler");
    
    MessageCenterManager *messageCenterManager = [MessageCenterManager sharedInstance];
    
    if (![messageCenterManager hasMessage:messageId]) {
        MessageCenterMessage *message = [[GoGoMessage alloc] initWithMessageId:messageId];
        
        [messageCenterManager addMessage:message];
    }
    
    if (applicationState != UIApplicationStateActive) {
        NSLog(@"App state not active");
        
        UIApplication *application = [UIApplication sharedApplication];
        
        if (application.applicationIconBadgeNumber > 0) {
            
            //NSLog(@"App badge count = %d", application.applicationIconBadgeNumber);
            
            [[AnalyticsManager sharedInstance] logCategory:@"Offer" withName:@"Banner Tapped"];
            
            GoGoOfferViewController *c = [[GoGoOfferViewController alloc] initWithMessageId:messageId];
            
            ConcurMobileAppDelegate *d = (ConcurMobileAppDelegate *) [application delegate];
            
            UINavigationController *nc = d.navController;
            
            [ConcurMobileAppDelegate unwindToRootView];
            
            //NSLog(@"Pushing offer view controller");
            
            [nc pushViewController:c animated:YES];
        }
    }
}

- (MessageStatus)statusFromString:(NSString *)statusString {
    MessageStatus messageStatus = MessageStatusUnknown;
    if ([statusString isEqualToString:@"NEW"]) {
        messageStatus = MessageStatusNew;
    } else if ([statusString isEqualToString:@"READ"]) {
        messageStatus = MessageStatusRead;
    } else if ([statusString isEqualToString:@"UNREAD"]) {
        messageStatus = MessageStatusUnread;
    }
    
    return messageStatus;
}

@end
