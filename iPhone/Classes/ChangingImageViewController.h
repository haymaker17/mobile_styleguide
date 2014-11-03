//
//  ChangingImageViewController.h
//  ConcurMobile
//
//  Created by ernest cho on 11/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ChangingImageView.h"

@interface ChangingImageViewController : UIViewController
@property (nonatomic, readwrite, strong) IBOutlet ChangingImageView *imageView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coRotatingImageHeight;

@end
