//
//  TextFieldVC.h
//  ConcurMobile
//
//  Created by yiwen on 10/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileViewController.h"
#import "FormFieldData.h"

@interface TextFieldEditVC : MobileViewController 
{
	UITextView					*commentTextView;
	CGRect						keyboardBounds;
	FormFieldData				*fDef;
	NSString					*fromView;

}
@property (nonatomic, retain) IBOutlet UITextView			*commentTextView;
@property (nonatomic) CGRect								keyboardBounds;
@property (nonatomic, retain) FormFieldData					*fDef;
@property (nonatomic, retain) NSString						*fromView;

-(IBAction) saveButtonPressed:(id)sender;

-(void)keyboardDidShowNotification:(NSNotification*)notification;
-(void)keyboardWillHideNotification:(NSNotification*)notification;

-(void)adjustSizeOfCommentTextViewWithKeyboard;
-(void)adjustSizeOfCommentTextViewWithoutKeyboard;

@end
