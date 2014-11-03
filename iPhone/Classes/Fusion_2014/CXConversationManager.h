//
//  CXConversationManager.h
//  FusionLab
//
//  Created by Richard Puckett on 4/14/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import "CXSpeechBubbleView.h"
#import "CXStatement.h"

@interface CXConversationManager : NSObject

+ (CXConversationManager *)sharedInstance;

- (void)addStatement:(CXStatement *)statement;
- (void)clear;
- (NSUInteger)numStatements;
- (CXStatement *)statementAtIndex:(NSUInteger)index;

@end
