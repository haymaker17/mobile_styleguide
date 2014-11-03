//
//  ConditionalFieldsActionList.h
//  ConcurMobile
//
//  Created by Concur on 3/15/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ConditionalFieldAction : NSObject

@property (nonatomic) NSInteger field;
@property (nonatomic, strong) NSString * action;
@property (nonatomic, strong) NSString * access;

@end


@interface ConditionalFieldsActionList : NSObject

@property (nonatomic, strong) NSMutableArray    *fieldActions;

- (void) add: (ConditionalFieldAction *) value;
- (id) objectAtIndexedSubscript: (NSUInteger) index;
- (void)setObject:(id) obj atIndexedSubscript:(NSUInteger)idx;
- (void) removeAll;

@end


