//
//  SelectOneEmailCell.m
//  ConcurMobile
//
//  Created by yiwen on 12/1/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "SelectOneEmailCell.h"
#import "FormatUtils.h"
#import "MobileViewController.h"
#import "WeakReference.h"

@implementation SelectOneEmailCell
@synthesize btnContacts, txtContacts, email;
@synthesize delegate = _delegate;



#pragma mark - Button Handler
- (IBAction)showPicker:(id)sender {
    if (self.delegate != nil)
        [self.delegate selectFromContacts];
}

#pragma mark - Set the text
#pragma mark - Text Field 
-(BOOL) textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    self.email = textField.text;
    if (self.delegate != nil)
        [self.delegate emailSelected:textField.text];
    return YES;
}


-(IBAction)editingText:(UITextField *)textField
{
    self.email = textField.text;
    if (self.delegate != nil)
        [self.delegate emailSelected:textField.text];
}

-(void)textFieldDidEndEditing:(UITextField *)textField
{
    self.email = textField.text;
    if (self.delegate != nil)
        [self.delegate emailSelected:textField.text];    
}

@end
