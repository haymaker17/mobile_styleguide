//
//  HotelDetailSegmentsCellData.m
//  ConcurMobile
//
//  Created by Sally Yan on 9/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelDetailSegmentsCellData.h"

@implementation HotelDetailSegmentsCellData

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"hotelDetailSegmentsCell";
        self.cellHeight = 55.0;
        self.selectedIndex = 1;
    }
    return self;
}

@end
