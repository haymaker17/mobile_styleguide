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

// old version - full screen wait view
+ (void)showWithText:(NSString*)text animated:(BOOL)animated;

// should use new version for wait view
+(void)showWithText:(NSString*)text animated:(BOOL)animated fullScreen:(BOOL)isFullScreen;


+ (void)hideAnimated:(BOOL)animated withCompletionBlock:(void(^)())completion;


@end
