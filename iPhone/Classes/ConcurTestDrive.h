//
//  ConcurTestDrive.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 12/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ConcurTestDrive : NSObject

@property (strong,nonatomic) UIViewController *controller;
@property (strong,nonatomic) UIWindow *window;

+(ConcurTestDrive*)sharedInstance;
+ (BOOL)isAvailable;
- (void)showTestDriveAnimated:(BOOL)animated;
- (void)popTestDriveAnimated:(BOOL)animated;
- (void)removeAnimated:(BOOL)animated;

@end
