//
//  TravelBookingAlertView.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 15/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TravelBookingAlertController : UIAlertController

- (id)initWithNavigationController:(UINavigationController *)navigationController;
- (void)showInViewController:(UIViewController *)vc;
- (void)showInRect:(CGRect )rect withViewController:(UIViewController *)vc withSender:(id)sender;

@end
