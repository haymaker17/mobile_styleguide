//
//  SettingsButton.m
//  ConcurMobile
//
//  Created by AJ Cram on 10/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "SettingsButton.h"

@implementation SettingsButton 

-(id) initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame])
    {
        if( [ExSystem is7Plus] )
        {
        	// Load image from asset catalog 
            UIImage *icon = [UIImage imageNamed:@"ic_menu_settings"];
            [self setBackgroundImage:icon forState:UIControlStateNormal];
            self.frame = CGRectMake(0, 0, 24, 24);
        }
        else
        {
            [self setBackgroundImage:[UIImage imageNamed:@"icon_settings"] forState:UIControlStateNormal];
            self.frame = CGRectMake(0, 0, 24, 24);
        }
    }
    return self;
}

- (UIEdgeInsets)alignmentRectInsets
{
    UIEdgeInsets insets;
    if( [ExSystem is7Plus] )
    {
        insets = UIEdgeInsetsMake(-3, 10, 0, 0);
    }
    else
    {
        insets = UIEdgeInsetsMake(0,0,0,0);
    }
    return insets;
}

@end
