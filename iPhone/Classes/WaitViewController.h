//
//  WaitViewController.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CCActivityIndicatorView.h"

@interface WaitViewController : UIViewController
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *spinnerView;
@property (weak, nonatomic) IBOutlet UITextView *textView;

+ (void)showWithText:(NSString*)text animated:(BOOL)animated;
+ (void)hideAnimated:(BOOL)animated withCompletionBlock:(void(^)())completion;


@end
