//
//  MessageCenterMessage.m
//  ConcurMobile
//
//  Created by Richard Puckett on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MessageCenterMessage.h"

@implementation MessageCenterMessage

- (id)init {
    self = [super init];
    
    if (self) {
        self.type = MessageTypeUnread;
    }
    
    return self;
}

- (NSString *)description {
    NSString *format = @"message: { id: %@, type: %d, status: %d, icon: %@, title: %@, message: %@ command: %@ }";
    
    return [NSString stringWithFormat: format,
            self.messageId, self.type, self.status, self.iconName, self.title, self.message, self.commandName];
}

#pragma mark - NSCoding

-(void)encodeWithCoder:(NSCoder *)encoder {
    [encoder encodeInteger:self.type forKey:@"type"];
    [encoder encodeInteger:self.status forKey:@"status"];
    [encoder encodeObject:self.iconName forKey:@"icon_name"];
    [encoder encodeObject:self.title forKey:@"title"];
    [encoder encodeObject:self.message forKey:@"message"];
    [encoder encodeObject:self.commandName forKey:@"command_name"];
    [encoder encodeObject:self.messageId forKey:@"messageId"];
    [encoder encodeObject:self.stringExtra forKey:@"string_extra"];
    [encoder encodeBool:self.isSilent forKey:@"is_silent"];
}

-(id)initWithCoder:(NSCoder *)decoder {
    self.type = [decoder decodeIntegerForKey:@"type"];
    self.status = [decoder decodeIntegerForKey:@"status"];
    self.iconName = [decoder decodeObjectForKey:@"icon_name"];
    self.title  = [decoder decodeObjectForKey:@"title"];
    self.message = [decoder decodeObjectForKey:@"message"];
    self.commandName = [decoder decodeObjectForKey:@"command_name"];
    self.messageId = [decoder decodeObjectForKey:@"messageId"];
    self.stringExtra = [decoder decodeObjectForKey:@"string_extra"];
    self.isSilent = [decoder decodeBoolForKey:@"is_silent"];
    
    return self;
}

@end
