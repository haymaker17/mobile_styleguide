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

@interface TextAreaEditVC : MobileViewController <UITextViewDelegate>
{
	UITextView					*textView;
	UIButton					*grpBorder;
	
	UILabel						*lblTip;
	
	CGRect						keyboardBounds;
	FormFieldData				*field;

	id<FieldEditDelegate>		__weak _delegate;

}
@property (nonatomic, strong) IBOutlet UITextView			*textView;
@property (nonatomic, strong) IBOutlet UIButton				*grpBorder;

@property (nonatomic, strong) IBOutlet UILabel				*lblTip;

@property (nonatomic) CGRect								keyboardBounds;
@property (nonatomic, strong) FormFieldData					*field;

@property (weak, nonatomic) id<FieldEditDelegate>			delegate;

- (void)textViewDidEndEditing:(UITextView *)textView;

-(void)keyboardDidShowNotification:(NSNotification*)notification;
-(void)keyboardWillHideNotification:(NSNotification*)notification;

-(void)adjustSizeOfCommentTextViewWithKeyboard;
-(void)adjustSizeOfCommentTextViewWithoutKeyboard;

@end
