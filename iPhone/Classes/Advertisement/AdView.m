//
//  AdView.m
//  ConcurMobile
//
//  Created by ernest cho on 3/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AdView.h"
#import "AppsUtil.h"
#import "Config.h"

#define AD_TAG 1

// This is just a UIView.  It can be added to any viewcontroller
@implementation AdView

- (id)init
{
    // if the ad layout changes just adjust the nib
    self = [[NSBundle mainBundle] loadNibNamed:@"AdView" owner:nil options:nil][0];
    return self;
}

- (BOOL)shouldShowAd
{
    // we do not show this ad thing anymore. it's been moved into the apps list.
    return NO;
}

// handle ad imageview touches
- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event
{
    UITouch *touch = [touches anyObject];
    if (touch.view.tag == AD_TAG) {
        [AppsUtil launchTravelTextApp];
    }
}

// Convenience methods to help a viewcontroller put this ad on a toolbar
- (NSMutableArray *)getToolbarItemsForIPhone
{
    UIBarButtonItem *space = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
    if ([ExSystem is7Plus]) {
        space.width = -16;
    } else {
        space.width = -12;
    }

    UIBarButtonItem *button = [[UIBarButtonItem alloc] initWithCustomView:self];
    
    return [[NSMutableArray alloc]initWithObjects:space, button, nil];
}

// hopefully not necessary
- (NSMutableArray *)getToolbarItemsForIPadModal
{
    UIBarButtonItem *space = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFixedSpace target:nil action:nil];
    space.width = -12;
    
    UIImage *image = [UIImage imageNamed:@"traveltext_ad_modal"];
    [self.adImage setImage:image];
   
    // have to set the image and frame size for touches to work.  maybe i could have done this in inteface builder...
    self.frame = CGRectMake(0, 0, image.size.width, image.size.height);
    self.adImage.frame = CGRectMake(0, 0, image.size.width, image.size.height);
    
    UIBarButtonItem *button = [[UIBarButtonItem alloc] initWithCustomView:self];
    
    return [[NSMutableArray alloc]initWithObjects:space, button, nil];
}

@end
