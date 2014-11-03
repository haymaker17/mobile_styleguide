//
//  ChooseNumberViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChooseNumberViewController : UIViewController

@property (weak, nonatomic) IBOutlet UITextField *textField;

@property (strong, nonatomic) NSString *notificationName;
@property (strong, nonatomic) NSString *contents;

@end
