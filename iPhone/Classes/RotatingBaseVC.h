//
//  RotatingBaseVC.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 3/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

// This is a Base class for all the rotating iPad views which look like the Login view.
// Note - This class is not intended to be a base class for any other viewcontroller other than the iPad Sign in type viewcontroller

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSUInteger, SignInUserType) {
    kPasswordUser,
    kMobilePasswordUser,
    kSSOUser
};


@interface RotatingBaseVC : UIViewController  <UITextFieldDelegate>

#pragma mark - New ipad storyboard constraint handlers

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaLea;
@property AlertTag alertTag;

@end
