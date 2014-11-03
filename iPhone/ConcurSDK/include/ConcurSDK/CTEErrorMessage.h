//
//  CTEErrorMessage.h
//  ConcurSDK
//
//  Created by ernest cho on 3/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

/*
 Data container for the standard concur error message.  This is not used by all endpoints yet.

 <Error>
    <Code>error.unable_to_sign_in</Code>
    <SystemMessage>We are unable to sign you in with this information</SystemMessage>
    <UserMessage>We are unable to sign you in with this information</UserMessage>
 </Error>

 The parser is in the private class CTEErrorUtilities just so clients won't have a dependency on the RXMLElement parser.
 */
@interface CTEErrorMessage : NSObject

@property (nonatomic, readwrite, strong) NSString *code;
@property (nonatomic, readwrite, strong) NSString *systemMessage;
@property (nonatomic, readwrite, strong) NSString *userMessage;

- (id)initWithCode:(NSString *)code systemMessage:(NSString *)systemMessage userMessage:(NSString *)userMessage;

@end
