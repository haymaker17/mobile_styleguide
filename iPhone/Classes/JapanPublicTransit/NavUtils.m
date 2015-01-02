//
//  NavUtils.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChooseDateViewController.h"
#import "ChooseLineViewController.h"
#import "ChooseNumberViewController.h"
#import "ChooseSeatViewController.h"
#import "ChooseStationViewController.h"
#import "ChooseTextViewController.h"
#import "Localizer.h"
#import "NavUtils.h"

@implementation NavUtils

+ (void)gotoDateChooserWithDate:(NSDate *)date
                       andTitle:(NSString *)title
             fromViewController:(UIViewController *)viewController {
    
    [NavUtils setBackButtonLabel:viewController];
    
    ChooseDateViewController *vc = [[ChooseDateViewController alloc] init];
    
    vc.date = date;
    vc.title = title;
    
    [viewController.navigationController pushViewController:vc animated:YES];
}

+ (void)gotoLineChooserWithTitle:(NSString *)title
                   forStationKey:(NSString *)stationKey
              fromViewController:(UIViewController *)viewController
            withNotificationName:(NSString *)notificationName {
    
    [NavUtils setBackButtonLabel:viewController];
    
    ChooseLineViewController *vc = [[ChooseLineViewController alloc] init];
    
    vc.notificationName = notificationName;
    vc.stationKey = stationKey;
    vc.title = title;
    
    [viewController.navigationController pushViewController:vc animated:YES];
}

+ (void)gotoNumberChooserWithNumber:(NSInteger)number
                           andTitle:(NSString *)title
                 fromViewController:(UIViewController *)viewController
               withNotificationName:(NSString *)notificationName {
    
    [NavUtils setBackButtonLabel:viewController];
    
    ChooseNumberViewController *vc = [[ChooseNumberViewController alloc] init];
    
    if (number != 0) {
        vc.contents = [NSString stringWithFormat:@"%ld", (long)number];
    }
    
    vc.notificationName = notificationName;
    vc.title = title;
    
    [viewController.navigationController pushViewController:vc animated:YES];
}

+ (void)gotoSeatChooserWithTitle:(NSString *)title
              fromViewController:(UIViewController *)viewController {
    
    [NavUtils setBackButtonLabel:viewController];
    
    ChooseSeatViewController *vc = [[ChooseSeatViewController alloc] init];
    
    vc.title = title;
    
    [viewController.navigationController pushViewController:vc animated:YES];
}

+ (void)gotoStationChooserWithTitle:(NSString *)title
                 fromViewController:(UIViewController *)viewController
               withNotificationName:(NSString *)notificationName {
    
    [NavUtils setBackButtonLabel:viewController];
    
    ChooseStationViewController *vc = [[ChooseStationViewController alloc] init];
    
    vc.notificationName = notificationName;
    vc.title = title;
    
    [viewController.navigationController pushViewController:vc animated:YES];
}

+ (void)gotoTextChooserWithText:(NSString *)text
                       andTitle:(NSString *)title
             fromViewController:(UIViewController *)viewController
           withNotificationName:(NSString *)notificationName {
    
    [NavUtils setBackButtonLabel:viewController];
    
    ChooseTextViewController *vc = [[ChooseTextViewController alloc] init];
    
    vc.contents = text;
    vc.notificationName = notificationName;
    vc.title = title;
    
    [viewController.navigationController pushViewController:vc animated:YES];
}

+ (void)setBackButtonLabel:(UIViewController *)viewController {
    viewController.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                                                       style:UIBarButtonItemStylePlain
                                                                                      target:nil
                                                                                      action:nil];
}

@end
