//
//  CXConversationManager.m
//  FusionLab
//
//  Created by Richard Puckett on 4/14/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import "CXConversationManager.h"

@interface CXConversationManager ()

@property (strong, nonatomic) NSMutableArray *statements;

@end

@implementation CXConversationManager

__strong static id _sharedInstance = nil;

+ (CXConversationManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.statements = [[NSMutableArray alloc] init];
    }
    
    return self;
}

- (void)addStatement:(CXStatement *)statement {
//    CXStatement *newStatement = [[CXStatement alloc] init];
//    statement.sentence = sentence;
//    statement.participant = participant;
    
    [self.statements addObject:statement];
}

- (void)clear {
    [self.statements removeAllObjects];
}

- (NSUInteger)numStatements {
    return self.statements.count;
}

- (CXStatement *)statementAtIndex:(NSUInteger)index {
    return [self.statements objectAtIndex:index];
}

@end
