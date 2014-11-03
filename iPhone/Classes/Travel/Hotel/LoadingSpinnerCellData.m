//
//  LoadingSpinnerCellData.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 8/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "LoadingSpinnerCellData.h"

@implementation LoadingSpinnerCellData

- (instancetype)initWithCaption:(NSString *)loadingCaption
{
    self = [super init];
    if (self) {
        self.cellIdentifier = @"LoadingSpinnerCell";
        self.cellHeight = 60.0;
        self.loadingCaption = loadingCaption;
        
 
    }
    return self;
}


-(instancetype)init
{
    self = [self initWithCaption:nil];
    return self;
}

@end
