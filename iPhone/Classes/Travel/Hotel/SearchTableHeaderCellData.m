//
//  SearchTableHeaderCellData.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/1/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SearchTableHeaderCellData.h"

@implementation SearchTableHeaderCellData

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"SearchCriteriaTableHeaderCell";
        self.cellHeight = 70.0;
    }
    return self;
}


@end
