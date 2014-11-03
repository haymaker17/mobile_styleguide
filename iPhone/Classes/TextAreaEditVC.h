//
//  TextAreaEditVC.h
//  ConcurMobile
//
//  Created by yiwen on 10/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileViewController.h"
#import "FormFieldData.h"
#import "FieldEditDelegate.h"

@interface TextAreaEditVC : MobileViewController 
{
	UITextView					*textView;
	UITextField					*textBorder;
	UIButton					*grpBorder;
	
	UILabel						*label;
	UINavigationBar				*tBar;
	UIBarButtonItem				*cancelBtn;
	UIBarButtonItem				*saveBtn;
	
	CGRect						keyboardBounds;
	FormFieldData				*field;

	id<FieldEditDelegate>		_delegate;

}
@property (nonatomic, retain) IBOutlet UITextView			*textView;
@property (nonatomic, retain) IBOutlet UITextField			*textBorder;
@property (nonatomic, retain) IBOutlet UIButton				*grpBorder;

@property (nonatomic, retain) IBOutlet UILabel				*label;
@property (nonatomic, retain) IBOutlet UINavigationBar		*tBar;
@property (nonatomic, retain) IBOutlet UIBarButtonItem		*cancelBtn;
@property (nonatomic, retain) IBOutlet UIBarButtonItem		*saveBtn;

@property (nonatomic) CGRect								keyboardBounds;
@property (nonatomic, retain) FormFieldData					*field;

@property (assign, nonatomic) id<FieldEditDelegate>			delegate;

-(IBAction) saveButtonPressed:(id)sender;
-(IBAction) cancelButtonPressed:(id)sender;

-(void)keyboardDidShowNotification:(NSNotification*)notification;
-(void)keyboardWillHideNotification:(NSNotification*)notification;

-(void)adjustSizeOfCommentTextViewWithKeyboard;
-(void)adjustSizeOfCommentTextViewWithoutKeyboard;

@end
