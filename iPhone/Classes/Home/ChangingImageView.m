//
//  ChangingImageView.m
//
//  Created by Pavan Adavi on 10/24/13.
//  Copyright (c) 2013 Pavan Adavi. All rights reserved.
//

#import "ChangingImageView.h"
#import "Config.h"
#import <QuartzCore/QuartzCore.h>

@interface ChangingImageView ()
@end

@implementation ChangingImageView

/**
 Allows us to init with Interface Builder
 */
- (id)initWithCoder:(NSCoder *)aDecoder
{
    if ((self = [super initWithCoder:aDecoder])) {

    }
    return self;
}

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {

    }
    
    return self;
}

/**
 Static list of home screen image names
 */
+ (NSArray *)homeScreenImageNames
{
    static NSArray *imageNames= nil;
    
    if (!imageNames)
    {
        if ([Config isGov])
        {
            imageNames = @[@"homeCity11.jpg"];
        }
        else
        {
            imageNames = @[ @"homeCity1.jpg",
                            @"homeCity2.jpg",
                            @"homeCity3.jpg",
                            @"homeCity4.jpg",
                            @"homeCity5.jpg",
                            @"homeCity6.jpg",
                            @"homeCity7.jpg",
                            @"homeCity8.jpg",
                            @"homeCity9.jpg",
                            @"homeCity10.jpg"
                            ];
        }
    }
    return imageNames;
}

/**
 Gets the next image index number
 */
- (int)getNextImageIndex
{
    // get last image shown and increment
    NSUserDefaults* standardUserDefaults = [NSUserDefaults standardUserDefaults];
    NSString* ChangeHomeImageFlag = (NSString*)[standardUserDefaults objectForKey:@"HomeImageCount"];
    int count = [ChangeHomeImageFlag intValue] + 1;

    // reset to 0, if we're over the number of images
    if (count >= [[ChangingImageView homeScreenImageNames] count]) {
        count = 0;
    }

    // save so we dont start over when app is closed
    [standardUserDefaults setObject:[NSString stringWithFormat:@"%d",count] forKey:@"HomeImageCount"];
    [standardUserDefaults synchronize];

    return count;
}

/**
 Updates image to the next image in the image list
 */
- (void)updateImage
{
    int next = [self getNextImageIndex];

    NSString *imageName = [ChangingImageView homeScreenImageNames][next];
    NSString *newFile = [NSString stringWithFormat:@"%@/%@", [[NSBundle mainBundle] resourcePath], imageName];
    UIImage *image =  [UIImage imageWithContentsOfFile:newFile];

    [UIView transitionWithView:self duration:1.0f options:UIViewAnimationOptionTransitionCrossDissolve animations:^{
        if(image != nil) {
            self.image = image;
        }
    } completion:nil];
}

@end
