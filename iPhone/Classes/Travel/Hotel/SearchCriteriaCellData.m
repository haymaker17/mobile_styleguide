//
//  SearchCriteriaCellData.m
//  PastDestinations
//
//  Created by Pavan Adavi on 6/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SearchCriteriaCellData.h"

@implementation SearchCriteriaCellData

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"SearchCriteriaCell";
        self.cellHeight = 60.0;
    }
    return self;
}

@end
