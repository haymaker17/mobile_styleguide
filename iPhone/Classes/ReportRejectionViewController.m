//
//  ReportRejectionViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReportRejectionViewController.h"
#import "MobileAlertView.h"


@implementation ReportRejectionViewController


@synthesize tBar;
// Mob-2517,2518
@synthesize cancelBtn;
@synthesize sendBackBtn;

@synthesize commentTextView;
@synthesize keyboardBounds;
@synthesize reportRejectionDelegate;


#pragma mark -
#pragma mark Button Handler Methods

-(IBAction) cancelPressed:(id)sender
{
	[self.reportRejectionDelegate rejectionCancelled];
	[self dismissViewControllerAnimated:YES completion:nil];
}

-(IBAction) sendBackPressed:(id)sender
{
	if (commentTextView.text == nil || [[commentTextView.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] length]==0)
	{
		UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle: nil
							  message: [Localizer getLocalizedText:@"APPROVE_ENTER_COMMENT_FOR_SENDBACK"]
							  delegate:nil 
							  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
							  otherButtonTitles:nil];
		[alert show];
		return;
	}
	
	[self.reportRejectionDelegate rejectedWithComment:commentTextView.text];
    
    [Flurry logEvent:@"Reports: Reject Report"];
    
	[self dismissViewControllerAnimated:YES completion:nil];
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];
	
	if ([UIDevice isPad])
	{
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardDidShowNotification:) name:UIKeyboardDidShowNotification object:nil];
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHideNotification:) name:UIKeyboardWillHideNotification object:nil];
	}
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	// Mob-2517,2518
	cancelBtn.title = [Localizer getLocalizedText:@"APPROVE_CANCEL"];
	sendBackBtn.title = [Localizer getLocalizedText:@"APPROVE_SENDBACK_BUTTON_TITLE"];	
	tBar.topItem.title  = [Localizer getLocalizedText:@"APPROVE_REPORT_SUMMARY_COMMENTS"];
	[commentTextView becomeFirstResponder];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
	if ([UIDevice isPad])
	{
		if (keyboardBounds.size.width == 0)
			[self adjustSizeOfCommentTextViewWithoutKeyboard];
		else
			[self adjustSizeOfCommentTextViewWithKeyboard];
	}
}

-(void)keyboardDidShowNotification:(NSNotification*)notification
{
	NSDictionary *userInfo = [notification userInfo];
	NSValue *keyboardBoundsValue = userInfo[UIKeyboardFrameEndUserInfoKey];

	CGRect keyboardBoundsRect;
	[keyboardBoundsValue getValue:&keyboardBoundsRect];
	
	self.keyboardBounds = [self.view convertRect:keyboardBoundsRect fromView:nil];

	[self adjustSizeOfCommentTextViewWithKeyboard];
}

-(void)keyboardWillHideNotification:(NSNotification*)notification
{
	self.keyboardBounds = CGRectMake(0, 0, 0, 0);
	[self adjustSizeOfCommentTextViewWithoutKeyboard];
}

-(void)adjustSizeOfCommentTextViewWithKeyboard
{
	CGRect viewBounds = self.view.bounds;

	CGRect newCommentFrame = commentTextView.frame;
	newCommentFrame.size.width = viewBounds.size.width;

	if ([UIDevice isPad])
	{
		newCommentFrame.size.height = keyboardBounds.origin.y;
		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	else
	{
		newCommentFrame.size.height = viewBounds.size.height - keyboardBounds.size.height;
		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	
	self.commentTextView.frame = newCommentFrame;
}


-(void)adjustSizeOfCommentTextViewWithoutKeyboard
{
	CGRect viewBounds = self.view.bounds;
	
	CGRect newCommentFrame = commentTextView.frame;
	newCommentFrame.size.width = viewBounds.size.width;
	newCommentFrame.size.height = viewBounds.size.height;
	newCommentFrame.size.height -= tBar.frame.size.height;
	
	self.commentTextView.frame = newCommentFrame;
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload
{
	if ([UIDevice isPad])
	{
		[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
		[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
	}
}



@end

