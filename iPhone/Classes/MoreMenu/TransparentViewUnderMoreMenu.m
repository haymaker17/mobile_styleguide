//
//  TransparentViewUnderMoreMenu.m
//  ConcurMobile
//
//  Created by Sally Yan on 11/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TransparentViewUnderMoreMenu.h"

@implementation TransparentViewUnderMoreMenu

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        //self = [[NSBundle mainBundle] loadNibNamed:@"TransparentViewUnderMoreMenu" owner:nil options:nil][0];
    }
    
    return self;
}

-(void)didMoveToSuperview{
    [super didMoveToSuperview];
    [self addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(removeWithEffect:)]];
}

- (void)removeWithEffect:(id )sender
{
    [UIView beginAnimations:@"removeWithEffect" context:nil];
    [UIView setAnimationDuration:0.25f];
    
    self.frame = self.frame;
    self.alpha = 0.0f;
    [UIView commitAnimations];
    
    if (self.dismiss)
    {
        self.dismiss();
    }
//    [self setHidden:YES];
}

@end
