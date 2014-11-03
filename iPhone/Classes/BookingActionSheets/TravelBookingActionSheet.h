//
//  IPhoneBookingActionSheet.h
//  ConcurMobile
//
//  Created by ernest cho on 3/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TravelBookingActionSheet : UIActionSheet <UIActionSheetDelegate>

- (id)initWithNavigationController:(UINavigationController *)navigationController;

- (void)showActionSheetFromToolBar:(UIToolbar *)toolBar;
- (void)showActionSheetInView:(UIView *)view;
- (void)showActionSheetFromBarButtonItem:(UIBarButtonItem *)item;
- (void)showActionSheetFromRect:(CGRect)rect inView:(UIView *)view;

@end
