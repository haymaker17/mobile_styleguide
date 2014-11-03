//
//  SUPERHOMEViewController.h
//  ConcurMobile
//
//  Created by Shifan Wu on 11/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Home9VC.h"
#import "iPadHome9VC.h"
#import "GovHome9VC.h"
#import "GoviPadHome9VC.h"
#import "MessageCenterManager.h"

@interface HomeLoaderVC : UIViewController <MessageCenterListener>
@property (nonatomic, strong) Home9VC               *home9VC;
@property (nonatomic, strong) iPadHome9VC           *iPadHome9VC;
@property (nonatomic, strong) GovHome9VC            *govHome9VC;
@property (nonatomic, strong) GoviPadHome9VC        *goviPadHome9VC;

-(UIViewController *)getRootviewController;
-(UIViewController *)getHomeVC;

/*
 * this allow to show the more menu
 */
-(void)showMoreMenu:(id)sender;


@end
