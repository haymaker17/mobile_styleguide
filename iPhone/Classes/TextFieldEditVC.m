//
//  TextFieldVC.m
//  ConcurMobile
//
//  Created by yiwen on 10/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TextFieldEditVC.h"
#import "RootViewController.h"

@implementation TextFieldEditVC
@synthesize commentTextView, fDef, fromView, keyboardBounds;

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return TEXT_FIELD_EDIT;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}

-(void) updateTitle
{
	self.title = fDef == nil? @"": fDef.label;
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ([msg.parameterBag objectForKey:@"FROM_VIEW"] != nil &&
			[msg.parameterBag objectForKey:@"FIELD_DEF"] != nil)
		{
			self.fromView = (NSString*)[msg.parameterBag objectForKey:@"FROM_VIEW"];
			self.fDef = (FormFieldData*)[msg.parameterBag objectForKey: @"FIELD_DEF"];
			if (self.fDef.fieldValue != nil)
				self.commentTextView.text = self.fDef.fieldValue;
			[self updateTitle];

/*			if ([msg.parameterBag objectForKey:@"PREFERRED_FONT_SIZE"] != nil)
			{
				self.preferredFontSize = [(NSNumber*)[msg.parameterBag objectForKey:@"PREFERRED_FONT_SIZE"] intValue];
			}
			else
			{
				self.preferredFontSize = 0;
			}
*/			
		}
	}
}


-(void) makeSaveButton
{
	UIBarButtonItem *btnSave = [[UIBarButtonItem alloc] initWithTitle:@"Save" style:UIBarButtonItemStyleBordered target:self action:@selector(saveButtonPressed:)];
	self.navigationItem.rightBarButtonItem = btnSave;
	[btnSave release];
}


#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
	[self makeSaveButton];
    [super viewDidLoad];
	if ([RootViewController is4] || [RootViewController isPad])
	{
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardDidShowNotification:) name:UIKeyboardDidShowNotification object:nil];
		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHideNotification:) name:UIKeyboardWillHideNotification object:nil];
	}
	
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	[self updateTitle];
	[commentTextView becomeFirstResponder];
}

- (void)viewDidUnload 
{
	if ([RootViewController is4] || [RootViewController isPad])
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
	if ([RootViewController is4] || [RootViewController isPad])
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
	
	CGRect newCommentFrame = commentTextView.frame;
	newCommentFrame.size.width = viewBounds.size.width;
	
	if ([RootViewController isPad])
	{
		newCommentFrame.size.height = keyboardBounds.origin.y;
//		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	else
	{
		newCommentFrame.size.height = viewBounds.size.height - keyboardBounds.size.height;
//		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	
	self.commentTextView.frame = newCommentFrame;
}


-(void)adjustSizeOfCommentTextViewWithoutKeyboard
{
	CGRect viewBounds = self.view.bounds;
	
	CGRect newCommentFrame = commentTextView.frame;
	newCommentFrame.size.width = viewBounds.size.width;
	newCommentFrame.size.height = viewBounds.size.height;
//	newCommentFrame.size.height -= tBar.frame.size.height;
	
	self.commentTextView.frame = newCommentFrame;
}


#pragma mark -
#pragma mark Button Action Handler

- (IBAction) saveButtonPressed:(id)sender;
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.fDef, @"FIELD_DEF", self.commentTextView.text, @"INPUT_TEXT", @"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", nil];
	[rootViewController switchToView:fromView viewFrom:[self getViewIDKey] ParameterBag:pBag];
	[pBag release];
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
	[fromView release];
	[commentTextView release];
    [super dealloc];
}

@end
