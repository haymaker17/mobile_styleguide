//
//  CustomBackButton.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 12/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CustomBackButton.h"

@implementation CustomBackButton

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        NSString *back = [NSString stringWithFormat:@"%@", [Localizer getLocalizedText:@"Back"] ];
        [self  setImage:[UIImage imageNamed:@"BackButtonArrow"] forState:UIControlStateNormal];
        [self setTitle:back forState:UIControlStateNormal];
        [self setTitleColor:[UIColor concurBlueColor] forState:UIControlStateNormal];
        [self sizeToFit];
        self.alignLeft = YES;   // Align left by default. 
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

- (UIEdgeInsets)alignmentRectInsets {
    UIEdgeInsets insets;
    if (self.alignLeft) {
        insets = UIEdgeInsetsMake(0, 12.0f, 0, 0);
    }
    else { // IF_ITS_A_RIGHT_BUTTON
        insets = UIEdgeInsetsMake(0, 0, 0, 9.0f);
    }
    return insets;
}


@end
