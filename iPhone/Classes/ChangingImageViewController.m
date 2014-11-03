//
//  ChangingImageViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 11/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChangingImageViewController.h"

@interface ChangingImageViewController ()
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coRotatingImageTopMargin;
@end

@implementation ChangingImageViewController

- (id)init
{
    self = [super initWithNibName:@"ChangingImageViewController" bundle:nil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Set margin on top of image
    [self setImageMargin];
    [self setImageHeight];
    
    // Updates image whenever the app becomes active
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateChangingImage) name:UIApplicationDidBecomeActiveNotification object:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)dealloc{
    // Stop listening for application became active notifications
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationDidBecomeActiveNotification object:nil];
}

/**
 Update the image
 */
- (void)updateChangingImage
{
    [self.imageView updateImage];
}

/**
 This programmatically adjusts the height of the rotating image bar. Only for iPhone 5 set the image height
 */
-(void)setImageHeight
{
    if([ExSystem is5])
    {
        self.coRotatingImageHeight.constant = 120;
    }
    else
    {
        self.coRotatingImageHeight.constant = 85;
    }
}

/**
 This programmatically adjusts the height of the rotating image bar.  Required to handle iOS6 the way the UI team wants.
 */
- (void)setImageMargin
{
    if ([ExSystem is7Plus]) {
        self.coRotatingImageTopMargin.constant = 64;
    } else {
        self.coRotatingImageTopMargin.constant = 0;
    }
}

@end
