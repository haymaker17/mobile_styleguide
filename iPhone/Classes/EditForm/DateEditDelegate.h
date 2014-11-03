//
//  DateEditDelegate.h
//  ConcurMobile
//
//  Created by yiwen on 5/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@protocol DateEditDelegate <NSObject>

-(void) dateSelected:(NSObject*) context withValue:(NSDate*) date;

@end
