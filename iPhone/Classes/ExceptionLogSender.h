//
//  ExceptionLogSender.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 10/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <MessageUI/MFMailComposeViewController.h>

@interface ExceptionLogSender : NSObject<MFMailComposeViewControllerDelegate>
{
	UIViewController *parentVC;
}

@property (nonatomic, strong) UIViewController* parentVC;

-(void)showExceptionAlert;
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex;
-(void)finished;

+(void)offerToSendExceptionLogFromViewController:(UIViewController*)viewController;

@end
