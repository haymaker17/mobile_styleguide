//
//  SendFeedBackVC.m
//  ConcurMobile
//
//  Created by Ray Chi on 9/8/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SendFeedBackVC.h"
#import "ExceptionLogging.h"

@interface SendFeedBackVC ()

@end

@implementation SendFeedBackVC

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.

}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)sendLogAction
{
	{
        //
        // Collecting Data for the Log File
		NSData* regularLogData = nil;
		NSData* exceptionLogData = nil;
		
		NSString* logFileName = [[MCLogging getInstance] createLogSummaryWithSettings];
		if (logFileName != nil)
		{
			regularLogData = [NSData dataWithContentsOfFile:logFileName];
		}
		
		NSString *exceptionFileName = [ExceptionLogging exceptionFilePath];
		if (exceptionFileName != nil)
		{
			exceptionLogData = [NSData dataWithContentsOfFile:exceptionFileName];
		}
        
		self.mailComposeDelegate = self; // Delegate must before setting others
        
        // Mail subject, receiver, attach files
        NSArray* recipients = @[@"mobilealphafeedbackios@concur.com"];
		[self setToRecipients:recipients];
        NSString *subJect = [NSString stringWithFormat:@"Hotel Alpha build %@",	[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
		[self setSubject:subJect];
		[self setMessageBody:@"" isHTML:NO];
		
        // Attach Log files
		if (regularLogData != nil && regularLogData.length > 0)
			[self addAttachmentData:regularLogData mimeType:@"text/plain" fileName:@"MobileLog.txt"];
		// Attach Exception files
		if (exceptionLogData != nil && exceptionLogData.length > 0)
			[self addAttachmentData:exceptionLogData mimeType:@"text/plain" fileName:@"ExceptionLog.txt"];
		
	}
}

#pragma mark -
#pragma mark MFMailComposeViewControllerDelegate Methods
-(void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
    NSString* alertTitle = nil;
	NSString* alertMessage = nil;
	
	switch (result)
	{
		case MFMailComposeResultSent:
			alertTitle = [Localizer getLocalizedText:@"Mail Queued"];
			alertMessage = [Localizer getLocalizedText:@"The log mail has been placed in your outbox."];
			break;
		case MFMailComposeResultSaved:
			alertTitle = [Localizer getLocalizedText:@"Mail Saved"];
			alertMessage = [Localizer getLocalizedText:@"The log mail has been saved in your Drafts folder."];
			break;
		case MFMailComposeResultFailed:
			alertTitle = [Localizer getLocalizedText:@"Send Failed"];
			if (error != nil)
			{
				alertMessage = [error localizedFailureReason];
			}
			else
			{
				alertMessage = [Localizer getLocalizedText:@"The log could not be sent."];
			}
			break;
		default:
			break;
	}
	
	if (alertTitle != nil && alertMessage != nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc]
							  initWithTitle: alertTitle
							  message: alertMessage
							  delegate:nil
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
	}
	
	[self dismissViewControllerAnimated:YES completion:nil];
}

@end
