//
//  FakeGroupSectionHeader.m
//  ConcurMobile
//
//  Created by ernest cho on 10/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "FakeGroupSectionHeader.h"

@implementation FakeGroupSectionHeader

@synthesize title;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        self = [[NSBundle mainBundle] loadNibNamed:@"FakeGroupSectionHeader" owner:nil options:nil][0];
    }
    return self;
}

@end
