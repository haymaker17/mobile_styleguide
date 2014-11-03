//
//  IgniteSegmentEditDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntitySegment.h"

@protocol IgniteSegmentEditDelegate <NSObject>
- (void) segmentUpdated:(EntitySegment*) segment;
@end
