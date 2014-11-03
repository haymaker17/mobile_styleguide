//
//  DateTimeOneDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol DateTimeOneDelegate <NSObject>

-(void) dateSelected:(NSObject *)context withDate:(NSDate *)date;

@end
