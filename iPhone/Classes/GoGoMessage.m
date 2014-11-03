//
//  GoGoMessage.m
//  ConcurMobile
//
//  Created by Richard Puckett on 4/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "GoGoMessage.h"

@implementation GoGoMessage

- (id)initWithMessageId:(NSString *)messageId {
    self = [super init];
    
    if (self) {
        self.iconName = @"gogo.png";
        self.title = @"Gogo Wifi is available on your upcoming flight.";
        self.message = @"All-Day Pass is $14.00 when you buy now through Concur.";
        self.commandName = @"GoGoOfferViewController";
        self.messageId = messageId;
        self.status = MessageStatusUnread;
        self.isSilent = NO;
    }
    
    return self;
}

@end
