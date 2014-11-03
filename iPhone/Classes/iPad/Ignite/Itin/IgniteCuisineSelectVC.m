//
//  IgniteCuisineSelectVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/28/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteCuisineSelectVC.h"
#import "ImageUtil.h"

@interface IgniteCuisineSelectVC ()

@end

@implementation IgniteCuisineSelectVC
@synthesize tableList, navBar;

- (void)dealloc
{
    tableList.dataSource = nil;
    tableList.delegate = nil;
    _delegate = nil;
}

- (void)setSeedData:(id<UITableViewDataSource, UITableViewDelegate>) del
{
    _delegate = del;
}

-(void) configureNavBar
{
    // Show custom nav bar
    UIImage *imgNavBar = [ImageUtil getImageByName:@"bar_title_landscape"];
    navBar.tintColor = [UIColor clearColor];
    [navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsDefault];
    [navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsLandscapePhone];
    
    UINavigationItem *navItem = [UINavigationItem alloc];
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 360, 44)];
    label.backgroundColor = [UIColor clearColor];
    label.font = [UIFont boldSystemFontOfSize:16.0];
    label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
    label.textAlignment = NSTextAlignmentCenter;
    label.textColor =[UIColor whiteColor];
    label.text = @"Pick Cuisine";		
    navItem.titleView = label;
    
    [self.navBar pushNavigationItem:navItem animated:YES];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    if (_delegate != nil)
    {
        self.tableList.dataSource = _delegate;
        self.tableList.delegate = _delegate;
    }
    [self configureNavBar];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

@end
