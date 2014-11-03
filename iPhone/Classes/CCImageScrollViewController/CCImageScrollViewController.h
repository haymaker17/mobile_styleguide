//
//  CCImageScrollViewController.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/9/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CCCenteredScrollView.h"

@interface CCImageScrollViewController : UIViewController<UIScrollViewDelegate>


@property (strong,nonatomic) IBOutlet CCCenteredScrollView *scrollView;
@property (strong,nonatomic) IBOutlet UIToolbar *titleBar;
@property (strong,nonatomic) IBOutlet UIBarButtonItem *titleItem;

@property (strong,nonatomic) IBOutlet UIToolbar *actionBar;

@property (strong,nonatomic) IBOutlet NSArray *actionItems;

@property (strong,nonatomic) UIImage *image;

@end
