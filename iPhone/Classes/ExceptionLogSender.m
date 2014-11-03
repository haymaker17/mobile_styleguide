//
//  ExceptionLogSender.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 10/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExceptionLogSender.h"
#import "ExceptionLogging.h"
#import "LabelConstants.h"
#import "ExSystem.h" 

#import "MobileAlertView.h"

#define kOfferToSendExceptionLogAlert	67114

@implementation ExceptionLogSender

@synthesize parentVC;
+(NSString*) getLogData
{
    NSData* logData = nil;
    
    NSString* logFileName = [ExceptionLogging exceptionFilePath];
    if (logFileName != nil)
    {
        logData = [NSData dataWithContentsOfFile:logFileName];
    }
    
    if (logData != nil || logData.length > 0)
    {
        NSString *strLog = [[NSString alloc] initWithData:logData encoding:NSStringEncodingConversionAllowLossy];
        NSString *str = [NSString stringWithFormat:@"---------------- Crash Log ----------------\n\n%@", strLog];
        return str;
    }
    else {
        return nil;
    }
}

+(NSString*) getSubject
{
    NSString *ver = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
    NSString *appVersion = @"";
    if (ver != nil && [ver length] > 0)
    {
        appVersion = [NSString stringWithFormat:@", %@", ver];
    }
    
    return [NSString stringWithFormat:@"%@ Crash%@", ([UIDevice isPad] ? @"iPad" : @"iPhone"), appVersion];
}

+(void)sendExceptionLogToServer
{
    NSString *path = [NSString stringWithFormat:@"%@/mobile/log/PostClientCrashLog", [ExSystem sharedInstance].entitySettings.uri];
    
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    RequestController *rc = [RequestController alloc];	
    Msg *msg = [[Msg alloc] initWithData:@"PostClientCrashLog" State:@"" Position:nil MessageData:nil URI:path MessageResponder:nil ParameterBag:pBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
    [msg setContentType:@"application/xml"];
    [msg setMethod:@"POST"];
    msg.skipCache = YES;

    NSString *text =[self getLogData];
    if (text == nil) // If no crash log is captured, move on.
        return;
    
    NSString *subject = [self getSubject];
    
	NSMutableString *bodyXML = [[NSMutableString alloc] 
                                 initWithString:@"<CrashLog xmlns=\"\">"];
    [bodyXML appendString:[NSString stringWithFormat:@"<ProductVersion>%@</ProductVersion>", [NSString stringByEncodingXmlEntities:subject]]];
	[bodyXML appendString:[NSString stringWithFormat:@"<Text>%@</Text>", [NSString stringByEncodingXmlEntities:text]]];
    [bodyXML appendString:[NSString stringWithFormat:@"<UserName>%@</UserName>", [ExSystem sharedInstance].userName]];
    [bodyXML appendString:@"</CrashLog>"];
	[msg setBody:bodyXML];

    [rc initDirect:msg MVC:nil];				

}

+(void)offerToSendExceptionLogFromViewController:(UIViewController*)viewController
{
    //MOB-10443 automatically send log to server
    [self sendExceptionLogToServer];
    return;

// Keep the old code, in case it needs to be resurrected
//	// If email is enabled, then ask the user if they want to send the crash log
//	if ([MFMailComposeViewController canSendMail])
//	{
//		ExceptionLogSender *sender = [[ExceptionLogSender alloc] init];
//		sender.parentVC = viewController;
//		[sender showExceptionAlert];
//        [sender release];
//	}
}

-(id)init
{
	self = [super init];
	return self;
}

-(void)showExceptionAlert
{
	UIAlertView *alert = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Crash Detected"]
													message:[Localizer getLocalizedText:@"This application crashed the last time you ran it.  Please help us determine the cause of the crash by sending us the application log."]
												   delegate:self
										  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
										  otherButtonTitles:[Localizer getLocalizedText:@"Send Log"], nil];
	alert.tag = kOfferToSendExceptionLogAlert;
	[alert show];
}

#pragma mark -
#pragma mark Alert Methods
-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if (kOfferToSendExceptionLogAlert == alertView.tag && buttonIndex == 1)
	{
		NSData* logData = nil;
		
		NSString* logFileName = [ExceptionLogging exceptionFilePath];
		if (logFileName != nil)
		{
			logData = [NSData dataWithContentsOfFile:logFileName];
		}
		
		if (logData != nil || logData.length > 0)
		{
			NSString *ver = [NSString stringWithFormat:@"%@",[[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];
			NSString *appVersion = @"";
			if (ver != nil && [ver length] > 0)
			{
				appVersion = [NSString stringWithFormat:@", version %@", ver];
			}

			MFMailComposeViewController *mailComposer = [[MFMailComposeViewController alloc] init];
			NSArray* recipients = @[@"mobilesupport@concur.com"];
			
			mailComposer.mailComposeDelegate = self;
			[mailComposer setToRecipients:recipients];
			[mailComposer setSubject:[NSString stringWithFormat:@"Mobile Log: %@ Crash%@", ([UIDevice isPad] ? @"iPad" : @"iPhone"), appVersion]];
            NSString *strLocalized = [Localizer getLocalizedText:@"Please type a description into this email message describing what you were doing when the application crashed.  For example: I was uploading an expense receipt."];
            NSString *strLog = [[NSString alloc] initWithData:logData encoding:NSStringEncodingConversionAllowLossy];
            NSString *str = [NSString stringWithFormat:@"%@\n\n\n---------------- Crash Log ----------------\n\n%@", strLocalized, strLog];
			[mailComposer setMessageBody:str isHTML:NO];
            
//			[mailComposer addAttachmentData:logData mimeType:@"text/plain" fileName:@"MobileLog.txt"];
			
			UIViewController* viewController = parentVC.presentedViewController;
			if (viewController == nil)
				viewController = parentVC;
			
			[viewController presentViewController:mailComposer animated:YES completion:nil];
			
			
			return;
		}
	}
	
	alertView.delegate = nil;
	[self finished];
}


#pragma mark -
#pragma mark MFMailComposeViewControllerDelegate Methods
- (void)mailComposeController:(MFMailComposeViewController*)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError*)err
{
	NSString* alertTitle = nil;
	NSString* alertMessage = nil;
	
	switch (result)
	{
		case MFMailComposeResultSent:
			alertTitle = [Localizer getLocalizedText:@"Thank You"];
			alertMessage = [Localizer getLocalizedText:@"An email containing the application's log has been placed in your outbox."];
			break;
		case MFMailComposeResultSaved:
			alertTitle = [Localizer getLocalizedText:@"Log Saved"];
			alertMessage = [Localizer getLocalizedText:@"An email containing the application's log has been saved in your Drafts folder."];
			break;
		case MFMailComposeResultFailed:
			alertTitle = [Localizer getLocalizedText:@"Send Failed"];
			if (err != nil)
			{
				alertMessage = [err localizedFailureReason];
			}
			else
			{
				alertMessage = [Localizer getLocalizedText:@"The log could not be sent."];
			}
			break;
		default:
			break;
	}
	
	[controller dismissViewControllerAnimated:YES completion:nil];

	if (alertTitle != nil && alertMessage != nil)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: alertTitle
							  message: alertMessage
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:LABEL_CLOSE_BTN]
							  otherButtonTitles:nil];
		[alert show];
	}
	[self finished];
}

-(void)finished
{
}


@end
