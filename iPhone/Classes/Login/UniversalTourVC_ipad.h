//
//  UniversalTourVC_ipad.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UniversalTourVC_ipad : UIViewController

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coFormAreaLea;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coLogoAreaWidth;

@property BOOL didSegueFromOtherScreen ;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *nextbarBtn;
@property (nonatomic, copy) void (^onCloseTestDriveTapped)(void);

@end
