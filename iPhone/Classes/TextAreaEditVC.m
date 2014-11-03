//
//  TextAreaEditVC.m
//  ConcurMobile
//
//  Created by yiwen on 10/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TextAreaEditVC.h"
#import "ExSystem.h" 
#import "RootViewController.h"

@implementation TextAreaEditVC
@synthesize delegate = _delegate;

@synthesize textView, textBorder, grpBorder, field, label, keyboardBounds, tBar, cancelBtn, saveBtn;

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return TEXT_FIELD_EDIT;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_MODAL;
}

-(void) updateCell
{
	self.label.text = field.label;
	self.textView.text = field.fieldValue == nil? @"" : field.fieldValue;
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ([msg.parameterBag objectForKey:@"FIELD_DEF"] != nil)
		{
			self.field = (FormFieldData*)[msg.parameterBag objectForKey: @"FIELD_DEF"];
			[self updateCell];

		}
	}
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	if ([ExSystem is4] || [ExSystem isPad])
	{
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardDidShowNotification:) name:UIKeyboardDidShowNotification object:nil];
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHideNotification:) name:UIKeyboardWillHideNotification object:nil];
	}
	self.textView.font = [UIFont systemFontOfSize:12];
	self.cancelBtn.title = [Localizer getLocalizedText:@"LABEL_CANCEL_BTN"];
	self.saveBtn.title = [Localizer getLocalizedText:@"LABEL_DONE_BTN"];
	self.tBar.topItem.title = [Localizer getLocalizedText:@"Edit"];
	
	// Show soft gray background
	[self.view setBackgroundColor:[UIColor colorWithRed:0.882871 green:0.887548 blue:0.892861 alpha:1]];	

}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self updateCell];
	[textView becomeFirstResponder];
}

- (void)viewDidUnload 
{
	if ([ExSystem is4] || [ExSystem isPad])
	{
		[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
		[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
	}
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	return YES;
}

- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
	if ([ExSystem is4] || [ExSystem isPad])
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
	NSValue *keyboardBoundsValue = [userInfo objectForKey:UIKeyboardFrameEndUserInfoKey];
	
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
	
	CGRect newCommentFrame = textView.frame;
	//newCommentFrame.size.width = viewBounds.size.width;
	
	if ([ExSystem isPad])
	{
		newCommentFrame.size.height = keyboardBounds.origin.y-textView.frame.origin.y - 16;
//		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	else
	{
		newCommentFrame.size.height = viewBounds.size.height - keyboardBounds.size.height - textView.frame.origin.y- 16;
//		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	
	self.textView.frame = newCommentFrame;
	CGRect newTextBorderFrame = textBorder.frame;
	newTextBorderFrame.size.height = newCommentFrame.size.height +6;
	self.textBorder.frame = newTextBorderFrame;

	CGRect newGrpBorderFrame = grpBorder.frame;
	newGrpBorderFrame.size.height = newCommentFrame.size.height +35;
	self.grpBorder.frame = newGrpBorderFrame;
	
}


-(void)adjustSizeOfCommentTextViewWithoutKeyboard
{
	CGRect viewBounds = self.view.bounds;
	
	CGRect newCommentFrame = textView.frame;
//	newCommentFrame.size.width = viewBounds.size.width - 23;
	newCommentFrame.size.height = viewBounds.size.height - textView.frame.origin.y-16;
//	newCommentFrame.size.height -= tBar.frame.size.height;
	
	self.textView.frame = newCommentFrame;

	CGRect newTextBorderFrame = textBorder.frame;
	newTextBorderFrame.size.height = newCommentFrame.size.height+6;
	self.textBorder.frame = newTextBorderFrame;

	CGRect newGrpBorderFrame = grpBorder.frame;
	newGrpBorderFrame.size.height = newCommentFrame.size.height +35;
	self.grpBorder.frame = newGrpBorderFrame;
	
}


#pragma mark -
#pragma mark Button Action Handler

- (IBAction) saveButtonPressed:(id)sender
{
	self.field.fieldValue = self.textView.text;

	if (self.delegate!= nil)
		[self.delegate fieldUpdated:self.field];
	[self.parentViewController dismissModalViewControllerAnimated:YES];
	
	//	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.fDef, @"FIELD_DEF", self.commentTextView.text, @"INPUT_TEXT", @"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", nil];
//	[[ConcurMobileAppDelegate findRootViewController] switchToView:fromView viewFrom:[self getViewIDKey] ParameterBag:pBag];
//	[pBag release];
}

-(IBAction) cancelButtonPressed:(id)sender
{
	if (self.delegate!= nil)
		[self.delegate fieldCanceled:self.field];
	[self.parentViewController dismissModalViewControllerAnimated:YES];
}


#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)dealloc
{
	[textView release];
	[textBorder release];
	[grpBorder release];
	[field release];
	[label release];
	[tBar release];
	[cancelBtn release];
	[saveBtn release];
    [super dealloc];
}

@end
