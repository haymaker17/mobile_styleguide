//
//  NavUtils.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

@interface NavUtils : NSObject

+ (void)gotoDateChooserWithDate:(NSDate *)date
                       andTitle:(NSString *)title
             fromViewController:(UIViewController *)viewController;

+ (void)gotoLineChooserWithTitle:(NSString *)title
                   forStationKey:(NSString *)stationKey
              fromViewController:(UIViewController *)viewController
            withNotificationName:(NSString *)notificationName;

+ (void)gotoNumberChooserWithNumber:(NSInteger)number
                           andTitle:(NSString *)title
                 fromViewController:(UIViewController *)viewController
               withNotificationName:(NSString *)notificationName;

+ (void)gotoSeatChooserWithTitle:(NSString *)title
                 fromViewController:(UIViewController *)viewController;

+ (void)gotoStationChooserWithTitle:(NSString *)title
                 fromViewController:(UIViewController *)viewController
               withNotificationName:(NSString *)notificationName;

+ (void)gotoTextChooserWithText:(NSString *)text
                       andTitle:(NSString *)title
             fromViewController:(UIViewController *)viewController
           withNotificationName:(NSString *)notificationName;

@end
