//
//  TextAreaEditVC.m
//  ConcurMobile
//
//  Created by yiwen on 10/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TextAreaEditVC.h"
#import "ExSystem.h" 


@implementation TextAreaEditVC
@synthesize delegate = _delegate;

@synthesize textView, grpBorder, field, keyboardBounds, lblTip;

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
	self.title = field.label;
	self.textView.text = field.fieldValue == nil? @"" : field.fieldValue;
    self.lblTip.text = field.tip;
}

-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		if ((msg.parameterBag)[@"FIELD_DEF"] != nil)
		{
			self.field = (FormFieldData*)(msg.parameterBag)[@"FIELD_DEF"];
			[self updateCell];

		}
	}
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
//	if ([ExSystem is4] || [UIDevice isPad])
//	{
//		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardDidShowNotification:) name:UIKeyboardDidShowNotification object:nil];
//		[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHideNotification:) name:UIKeyboardWillHideNotification object:nil];
//	}
	self.textView.font = [UIFont systemFontOfSize:12];
	self.title = [self.field getFullLabel];
	
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
	if ([UIDevice isPad])
	{
		[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
		[[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
	}
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
	
	CGRect newCommentFrame = textView.frame;
	//newCommentFrame.size.width = viewBounds.size.width;
	
	if ([UIDevice isPad])
	{
		newCommentFrame.size.height = keyboardBounds.origin.y-textView.frame.origin.y - 36;
//		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	else
	{
		newCommentFrame.size.height = viewBounds.size.height - keyboardBounds.size.height - textView.frame.origin.y- 36;
//		newCommentFrame.size.height -= tBar.frame.size.height;
	}
	
	self.textView.frame = newCommentFrame;
//	CGRect newTextBorderFrame = textBorder.frame;
//	newTextBorderFrame.size.height = newCommentFrame.size.height +6;
//	self.textBorder.frame = newTextBorderFrame;

	CGRect newGrpBorderFrame = grpBorder.frame;
	newGrpBorderFrame.size.height = newCommentFrame.size.height +35;
	self.grpBorder.frame = newGrpBorderFrame;
	
}


-(void)adjustSizeOfCommentTextViewWithoutKeyboard
{
	CGRect viewBounds = self.view.bounds;
	
	CGRect newCommentFrame = textView.frame;
//	newCommentFrame.size.width = viewBounds.size.width - 23;
	newCommentFrame.size.height = viewBounds.size.height - textView.frame.origin.y-36;
//	newCommentFrame.size.height -= tBar.frame.size.height;
	
	self.textView.frame = newCommentFrame;

//	CGRect newTextBorderFrame = textBorder.frame;
//	newTextBorderFrame.size.height = newCommentFrame.size.height+6;
//	self.textBorder.frame = newTextBorderFrame;

	CGRect newGrpBorderFrame = grpBorder.frame;
	newGrpBorderFrame.size.height = newCommentFrame.size.height +35;
	self.grpBorder.frame = newGrpBorderFrame;
	
}


#pragma mark -
#pragma mark TextView Delegate 
- (void)textViewDidEndEditing:(UITextView *)txtView
{
    NSString* newText = txtView.text;
    NSString* oldText = self.field.fieldValue;
    if (([newText length] || 
        [oldText length]) && 
        ![newText isEqualToString:oldText])
    {
        self.field.fieldValue = txtView.text;
    
        if (self.delegate!= nil)
            [self.delegate fieldUpdated:self.field];
    }
}
#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}


@end
