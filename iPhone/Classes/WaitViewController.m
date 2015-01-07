//
//  WaitViewController.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 12/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "WaitViewController.h"

@interface WaitViewController ()

@property (strong,nonatomic) UIWindow *window;

@end

@implementation WaitViewController

+ (instancetype)sharedInstance{
    static id sharedInstance;
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        sharedInstance = [[[self class] alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init{
    if(self = [super init]){
        // nothing for now
    }
    return self;
}

+(void)showWithText:(NSString *)text animated:(BOOL)animated
{
    [self showWithText:text animated:animated fullScreen:YES];
}

+(void)showWithText:(NSString*)text animated:(BOOL)animated fullScreen:(BOOL)isFullScreen
{
    WaitViewController *wait = [WaitViewController sharedInstance];
    CGRect bounds = [[UIScreen mainScreen] bounds];
    
    if (isFullScreen) {
        wait.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    }
    else {
        wait.window = [[UIWindow alloc] initWithFrame:CGRectMake(0, 64, bounds.size.width, (bounds.size.height - 64))];
    }

    wait.window.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight;
    wait.window.opaque = NO;
    wait.window.windowLevel = 1.3;
    WaitViewController *ctrl = [[UIStoryboard storyboardWithName:[@"Wait" storyboardName] bundle:nil] instantiateViewControllerWithIdentifier:@"WaitViewController"];
    [ctrl view]; // this will hookup the IBOutlet
    [ctrl.textView setText:text];
    [ctrl.spinnerView startAnimating];
    
    [wait.window setRootViewController:ctrl];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [[WaitViewController sharedInstance].window makeKeyAndVisible];
        
        if(animated){
            [WaitViewController sharedInstance].window.rootViewController.view.alpha = 0;
            [UIView animateWithDuration:0.3 animations:^{
                [WaitViewController sharedInstance].window.rootViewController.view.alpha = 1;
            }];
            
            [UIView animateWithDuration:0.2 animations:^{
                [WaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 1.1, 1.1);
            } completion:^(BOOL finished) {
                [UIView animateWithDuration:0.1 delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
                    [WaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 1, 1);
                } completion:^(BOOL finished2) {
                }];
            }];
        }
    });

    
}

+ (void)hideAnimated:(BOOL)animated withCompletionBlock:(void(^)())completion{
    if(!animated){
        [[WaitViewController sharedInstance] cleanup];
        if(completion){
            completion();
        }
        return;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIView animateWithDuration:0.3 animations:^{
            [WaitViewController sharedInstance].window.rootViewController.view.alpha = 0;
        }];
        
        [WaitViewController sharedInstance].window.layer.shouldRasterize = YES;
        [UIView animateWithDuration:0.1 animations:^{
            [WaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 1.1, 1.1);
        } completion:^(BOOL finished){
            [UIView animateWithDuration:0.2 delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
                [WaitViewController sharedInstance].window.alpha = 0;
                [WaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 0.4, 0.4);
            } completion:^(BOOL finished2){
                [[WaitViewController sharedInstance] cleanup];
                if(completion){
                    completion();
                }
            }];
        }];
    });
}

- (void)cleanup{
    [[[[UIApplication sharedApplication] delegate] window] makeKeyWindow];
    [self.window removeFromSuperview]; // in case we allow to reference the wait view directly
    self.window = nil;
}

@end
