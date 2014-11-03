//
//  ApproverTAViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 4/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ApproverTAViewController : UIViewController<UITabBarControllerDelegate>

@property NSMutableDictionary *paramBag;

@property (strong, nonatomic) NSString	*role;

@property (weak, nonatomic) IBOutlet UIView *embedTabContainer;
@property (weak, nonatomic) UITabBarController *tabBarController;

@property BOOL hasCloseButton;


@property (strong, nonatomic) IBOutlet UISegmentedControl *segmentedController;

- (void)actionBack:(id)sender;
@end
