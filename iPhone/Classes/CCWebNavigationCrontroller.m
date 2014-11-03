//
//  CCWebNavigationCrontroller.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 1/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCWebNavigationCrontroller.h"

@interface CCWebNavigationCrontroller ()

@end

@implementation CCWebNavigationCrontroller

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (instancetype)initWithTitle:(NSString*)title leftBarButtonItem:(UIBarButtonItem*)leftBarButtonItem {
    CCWebNavigationCrontroller *ctrl = [[UIStoryboard storyboardWithName:@"CCWebBrower" bundle:nil] instantiateViewControllerWithIdentifier:@"CCWebNavigationCrontroller"];
    ctrl.browser = ctrl.viewControllers[0];
    [ctrl.browser.navigationItem setTitle:title];
    [ctrl.browser.navigationItem setLeftBarButtonItem:leftBarButtonItem];
    
    [leftBarButtonItem setTitleTextAttributes:@{NSForegroundColorAttributeName:[leftBarButtonItem tintColor]} forState:UIControlStateNormal];
    return ctrl;
}

@end
