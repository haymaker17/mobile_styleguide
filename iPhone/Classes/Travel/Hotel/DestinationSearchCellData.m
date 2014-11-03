//
//  DestinationSearchCell.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 07/08/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "DestinationSearchCellData.h"

@implementation DestinationSearchCellData

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"DestinationSearchCell";
        self.cellHeight = 50.0;
    }
    return self;
}
@end
