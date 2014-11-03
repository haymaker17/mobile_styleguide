//
//  StationTableCellView.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "StationTableCellView.h"

@implementation StationTableCellView

- (id)initWithNib:(NSString *)nibName {
    self = [super init];
    
    if (self) {
        [[[NSBundle mainBundle] loadNibNamed:nibName owner:self options:nil] objectAtIndex:0];
        [self addSubview:self.view];
    }
    
    return self;
}

@end
