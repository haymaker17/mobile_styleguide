//
//  TravelWaitViewController.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/30/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/**
 the code below is copied from Richard Puckett's work CXDistractor.m and WaitViewController.m by Wanny. I only modified the window frame size that allows the navigation bar to show up.
 */

#import "TravelWaitViewController.h"

@interface TravelWaitViewController ()

@property (strong,nonatomic) UIWindow *window;
+ (void)showWithText:(NSString*)text fullScreen:(BOOL)isFullScreen animated:(BOOL)animated ;

@end

@implementation TravelWaitViewController

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

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        // Empty.
    }
    
    return self;
}
+ (void)showFullScreeWithText:(NSString*)text animated:(BOOL)animated
{
    [TravelWaitViewController showWithText:text fullScreen:YES animated:animated isTransparent:NO];
}

+ (void)showWithText:(NSString*)text animated:(BOOL)animated {
    [TravelWaitViewController showWithText:text fullScreen:NO animated:animated isTransparent:NO];
}

+ (void)showTransparentWithText:(NSString*)text animated:(BOOL)animated
{
    [TravelWaitViewController showWithText:text fullScreen:NO animated:animated isTransparent:YES];
}

+ (void)showWithText:(NSString*)text fullScreen:(BOOL)isFullScreen animated:(BOOL)animated isTransparent:(BOOL)isTransparent {
    
    TravelWaitViewController *wait = [TravelWaitViewController sharedInstance];
    CGRect bounds = [[UIScreen mainScreen] bounds];
    if (isFullScreen) {
        wait.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    }
    else {
        wait.window = [[UIWindow alloc] initWithFrame:CGRectMake(0, 64, bounds.size.width, (bounds.size.height - 64))];
    }
    wait.window.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight;
    wait.window.opaque = YES;
    wait.window.windowLevel = 1.3;
    TravelWaitViewController *ctrl = [[UIStoryboard storyboardWithName:@"TravelWaitView" bundle:nil] instantiateViewControllerWithIdentifier:@"TravelWaitViewController"];
    [ctrl view]; // this will hookup the IBOutlet
    [ctrl.caption setText:text];
    [ctrl.caption sizeToFit];
    [ctrl.distractor startAnimating];

    [wait.window setRootViewController:ctrl];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [[TravelWaitViewController sharedInstance].window makeKeyAndVisible];
        
        if(animated){
            [UIView animateWithDuration:0.3 animations:^{
                if (isTransparent)
                {
                    [[TravelWaitViewController sharedInstance].window.rootViewController.view setBackgroundColor:[[UIColor whiteColor] colorWithAlphaComponent: 0.8]];
                }
            }];
            
            [UIView animateWithDuration:0.2 animations:^{
                [TravelWaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 1.1, 1.1);
            } completion:^(BOOL finished) {
                [UIView animateWithDuration:0.1 delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
                    [TravelWaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 1, 1);
                } completion:^(BOOL finished2) {
                }];
            }];
        }
    });
    
    
}

+ (void)hideAnimated:(BOOL)animated withCompletionBlock:(void(^)())completion{
    if(!animated){
        [[TravelWaitViewController sharedInstance] cleanup];
        if(completion){
            completion();
        }
        return;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIView animateWithDuration:0.3 animations:^{
            [TravelWaitViewController sharedInstance].window.rootViewController.view.alpha = 0;
        }];
        
        [TravelWaitViewController sharedInstance].window.layer.shouldRasterize = YES;
        [UIView animateWithDuration:0.1 animations:^{
            [TravelWaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 1.1, 1.1);
        } completion:^(BOOL finished){
            [UIView animateWithDuration:0.2 delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^{
                [TravelWaitViewController sharedInstance].window.alpha = 0;
                [TravelWaitViewController sharedInstance].window.transform = CGAffineTransformScale(CGAffineTransformIdentity, 0.4, 0.4);
            } completion:^(BOOL finished2){
                [[TravelWaitViewController sharedInstance] cleanup];
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

- (IBAction)didDismiss:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

@end



