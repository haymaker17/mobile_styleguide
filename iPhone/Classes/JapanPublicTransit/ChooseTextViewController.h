//
//  ChooseTextViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChooseTextViewController : UIViewController

@property (weak, nonatomic) IBOutlet UITextView *textView;

@property (strong, nonatomic) NSString *notificationName;
@property (strong, nonatomic) NSString *contents;

@end
