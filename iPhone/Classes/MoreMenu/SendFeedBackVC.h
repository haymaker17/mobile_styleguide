//
//  SendFeedBackVC.h
//  ConcurMobile
//
//  Created by Ray Chi on 9/8/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <MessageUI/MessageUI.h>
#import <MessageUI/MFMailComposeViewController.h>

@interface SendFeedBackVC : MFMailComposeViewController <MFMailComposeViewControllerDelegate>

-(void)sendLogAction;

@end
