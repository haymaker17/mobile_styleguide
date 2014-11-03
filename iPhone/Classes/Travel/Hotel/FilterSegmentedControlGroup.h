//
//  FilterSegmentedControlGroup.h
//  ConcurMobile
//
//  Created by Ray Chi on 9/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface FilterSegmentedControlGroup : UIControl

@property (nonatomic) NSInteger selectIndex;

@property (nonatomic,copy) void (^onSelected)(NSInteger index);

/**
 *  Change type of the control group
 *
        type 1----- Rating
             2----- Miles
 */
- (void)changeType:(NSInteger)type;

@end
