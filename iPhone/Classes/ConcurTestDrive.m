//
//  ConcurTestDrive.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 12/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ConcurTestDrive.h"
#import "MCLogging.h"
#import "UIDevice+Additions.h"
#import "UniversalTourVC.h"
#import "UniversalTourVC_ipad.h"

@implementation ConcurTestDrive

static ConcurTestDrive *sharedInstance;

// MOB-18179 : Make this class singleton so all showing and closing storyboard is handled from one place
+(ConcurTestDrive*)sharedInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[ConcurTestDrive alloc] init];
    });
    return sharedInstance;
}

+ (BOOL)isAvailable{
    NSString *countryCode = [[NSLocale currentLocale] objectForKey: NSLocaleCountryCode];
    return [countryCode isEqualToString:@"US"] || [countryCode isEqualToString:@"GB"] || [countryCode isEqualToString:@"CA"] || [countryCode isEqualToString:@"AU"];
}

/**
 Sets the callback block to close the storyboard
 */
-(void)setupDismissBlock
{
    // Set the close call back. So that the calling class can handle closing the TestDrive Storyboard.
    if ([UIDevice isPad]) {
        UINavigationController *navi = (UINavigationController *)self.controller;
        UniversalTourVC_ipad *ctrl = (UniversalTourVC_ipad *)navi.topViewController;
        [ctrl setOnCloseTestDriveTapped:^{
            [self removeAnimated:YES];
        }];
    } else {
        UINavigationController *navi = (UINavigationController *)self.controller;
        UniversalTourVC *ctrl = (UniversalTourVC *)navi.topViewController;
        [ctrl setOnCloseTestDriveTapped:^{
            [self removeAnimated:YES];
        }];
    }
}

- (void)showTestDriveAnimated:(BOOL)animated{
    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurTestDrive::showTestDriveAnimated"] Level:MC_LOG_DEBU];
    //    MOB-16593: Test Drive is only currently supported in four countries
    if (![ConcurTestDrive isAvailable]) {
        //no need to show TestDrive Storyboard over the login screen
        return;
    }
    [[MCLogging getInstance] log:@"Home9vc::showManualLoginView: Showing TestDrive storyboard." Level:MC_LOG_INFO];
    
    UIStoryboard* storyboard = [UIStoryboard storyboardWithName:[@"TestDrive" storyboardName] bundle:nil];
    self.controller = [storyboard instantiateInitialViewController];
   
    // Set the close call back. So that the calling class can handle closing the TestDrive Storyboard.
     [self setupDismissBlock];
    
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.window.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.window setRootViewController:self.controller];

    self.window.windowLevel = 1.2;
    [self.window makeKeyAndVisible];
    if (animated) {
        self.window.alpha = 0;
        [UIView animateWithDuration:0.3 animations:^{
            self.window.alpha = 1;
        }];
    }
}

- (void)popTestDriveAnimated:(BOOL)animated{
    
     [[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurTestDrive::popTestDriveAnimated"] Level:MC_LOG_DEBU];
    //    MOB-16593: Test Drive is only currently supported in four countries
    if (![ConcurTestDrive isAvailable]) {
        //no need to show TestDrive Storyboard over the login screen
        return;
    }
    [[MCLogging getInstance] log:@"Home9vc::showManualLoginView: Showing TestDrive storyboard." Level:MC_LOG_INFO];
    
    UIStoryboard* storyboard = [UIStoryboard storyboardWithName:[@"TestDrive" storyboardName] bundle:nil];
    self.controller = [storyboard instantiateInitialViewController];

    // Set the close call back. So that the calling class can handle closing the TestDrive Storyboard.
    [self setupDismissBlock];
    
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.window.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    [self.window setRootViewController:self.controller];
    self.window.windowLevel = 1.2;
    [self.window makeKeyAndVisible];

    if (animated) {
        switch ([[UIApplication sharedApplication] statusBarOrientation]) {
            case UIDeviceOrientationPortrait:
                self.window.transform = CGAffineTransformMakeTranslation(-640,0);
                break;
            case UIDeviceOrientationPortraitUpsideDown:
                self.window.transform = CGAffineTransformMakeTranslation(+640,0);
                break;
            case UIDeviceOrientationLandscapeRight:
                self.window.transform = CGAffineTransformMakeTranslation(0,+940);
                break;
            case UIDeviceOrientationLandscapeLeft:
                self.window.transform = CGAffineTransformMakeTranslation(0,-940);
                break;
            default:
                break;
        }
        
        [UIView animateWithDuration:0.4 animations:^{
            self.window.transform = CGAffineTransformIdentity;
            
        }];
    }
}

- (void)removeAnimated:(BOOL)animated{
    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"ConcurTestDrive::removeAnimated"] Level:MC_LOG_DEBU];
    if (animated) {
        self.window.alpha = 1;
        [UIView animateWithDuration:0.3 animations:^{
            self.window.alpha = 0;
        }completion:^(BOOL finished) {
            [self.window removeFromSuperview];
            self.window = nil;
        }];
    } else {
        [self.window removeFromSuperview];
        self.window = nil;
    }

}
@end
