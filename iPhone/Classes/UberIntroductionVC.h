//
//  TestViewController.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 19/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UberIntroductionVC : UIViewController

@property (weak, nonatomic) IBOutlet UIView         *contentView;
@property (weak, nonatomic) IBOutlet UIScrollView   *scrollView;
@property (weak, nonatomic) IBOutlet UIButton       *uberButton;

-(IBAction)didPressUberButton:(id)sender;

@end
