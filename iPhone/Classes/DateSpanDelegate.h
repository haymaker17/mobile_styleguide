//
//  DateSpan.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@protocol DateSpanDelegate

- (void)setDateSpanFrom:(NSDate*)startDate to:(NSDate*)endDate;

@end
