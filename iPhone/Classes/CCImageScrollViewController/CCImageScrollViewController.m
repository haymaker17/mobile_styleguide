//
//  CCImageScrollViewController.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/9/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CCImageScrollViewController.h"


@interface CCImageScrollViewController ()

@property (strong,nonatomic) UIImageView *imageView;
@end

@implementation CCImageScrollViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (CGSize)contentSizeForViewInPopover {
    // Currently no way to obtain the width dynamically before viewWillAppear.
    if ( UIDeviceOrientationIsLandscape([[UIApplication sharedApplication] statusBarOrientation])) {
        return (CGSize){480, 320};
    }else{
        return (CGSize){320, 480};
    }
}

- (void)setTitle:(NSString *)title{
    [super setTitle:title];
    [self.titleItem setTitle:self.title];
}

-(void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    [self.titleItem setTitle:self.title];
    self.actionBar.items = self.actionItems;
    //float yOrigin = [[UIScreen mainScreen] bounds].size.height - 58;
    //[self.actionBar setFrame:CGRectMake(0, yOrigin, 320, 58)];
}

- (void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
//    NSLog(@"viewWillAppear %@",NSStringFromCGRect(self.view.bounds));
    float yOrigin = self.view.bounds.size.height - 58;
    float viewWidth = self.view.bounds.size.width;
    [self.actionBar setFrame:CGRectMake(0, yOrigin, viewWidth, 58)];
//    [self.actionBar setFrame:CGRectMake(0, yOrigin, 320, 58)];
    
    if (self.navigationController) {
        self.titleBar.hidden = YES;
    }else {
        self.titleBar.hidden = NO;
        [self.titleBar setFrame:CGRectMake(0, 0, viewWidth, 44)];
//        [self.titleBar setFrame:CGRectMake(0, 0, 329, 44)];
    }
    
    if (self.image) {
        self.imageView = [[UIImageView alloc] initWithImage:self.image];
        CGSize imageSize = self.image.size;
        [self.scrollView setContentSize:imageSize];
        self.scrollView.delegate = self;
        [self.scrollView addSubview:self.imageView];
        [self.scrollView setMinimumZoomScale:0.5*MIN(self.view.bounds.size.width/imageSize.width, self.view.bounds.size.height/imageSize.height)];
        [self.scrollView setZoomScale:MIN(self.view.bounds.size.width/imageSize.width, self.view.bounds.size.height/imageSize.height) animated:NO];
    } else{
        NSLog(@"CCImageScrollViewController ---- image is nil");
    }
}


- (UIView*)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    return self.imageView;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}



@end
