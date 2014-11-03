//
//  IgniteChatterCommentView.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/3/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterCommentView.h"

@implementation IgniteChatterCommentView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

+(IgniteChatterCommentView*) makeViewWithOwner:(id)owner
{
    IgniteChatterCommentView *view = nil;
    
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"IgniteChatterCommentView" owner:owner options:nil];
    for (id oneObject in nib)
    {
        if ([oneObject isKindOfClass:[IgniteChatterCommentView class]])
        {
            view = (IgniteChatterCommentView *)oneObject;
            break;
        }
    }
	
	return view;
}

@end
