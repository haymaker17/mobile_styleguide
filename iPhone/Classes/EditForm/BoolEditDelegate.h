//
//  BoolEditDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 5/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@protocol BoolEditDelegate <NSObject>

-(void) boolUpdated:(NSObject*) context withValue:(BOOL) val;

@end
