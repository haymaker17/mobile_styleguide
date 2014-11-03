//
//  CustomFieldTextEditor.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "CustomFieldTextEditor.h"
#import "TravelCustomFieldsManager.h"

#define NUMERIC_INPUT_FILTER @"0123456789"

@implementation CustomFieldTextEditor
@synthesize tcf;

#pragma mark - View lifecycle
-(void) viewDidLoad
{
    [super viewDidLoad];
    self.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[@"Back" localize] style:UIBarButtonItemStylePlain target:self action:@selector(closeView)];
    
    self.textField.delegate = self;
    // MOB-18480 allow exiting screen when field is not a required field
    // Overwrite existing value with empty value
    if (self.tcf.attributeValue != nil)
    {
        self.textField.text = self.tcf.attributeValue;
    }
}


-(void)closeView
{
    self.pressedDone = YES;
    
	NSString* text = (textField.text == nil) ? @"" : textField.text;
    int min = [tcf.minLength integerValue];
    int max = [tcf.maxLength integerValue];
    int length = (int)text.length;
    
    // MOB-9174, MOB-9182
    if ((([tcf.required boolValue] == YES && length > 0 && [text lengthIgnoreWhitespace]) || [tcf.required boolValue] == NO ) && ((min <= max && length >= min && length <= max) || min > max  || ([tcf.dataType isEqualToString:@"number"] || (min < 0 && max < 0))))
    {
        self.tcf.attributeValue = text;
        self.tcf.selectedAttributeOptionText = text;
        
        [[TravelCustomFieldsManager sharedInstance] saveIt:tcf];
        [self.textField resignFirstResponder];
        
        [self.navigationController popViewControllerAnimated:YES];
    }
    // MOB-18480 allow exiting screen when field is not a required field
    // Overwrite existing value with empty value
    else if ([text length] == 0 && [tcf.required boolValue] == NO)
    {
        self.tcf.attributeValue = text;
        self.tcf.selectedAttributeOptionText = text;
        
        [[TravelCustomFieldsManager sharedInstance] saveIt:tcf];
        [self.textField resignFirstResponder];

        [self.navigationController popViewControllerAnimated:YES];
    }
    else
    {
        NSString *msg = nil;
        
        if ((min <= 0 && max <= 0) || (min > max))
            msg =  [@"VALUE_RANGE_MSG_NO_BOUNDS" localize];
        else if (min <= 0)
            msg = [NSString stringWithFormat:[@"VALUE_RANGE_MSG_UPPER_BOUND" localize], max];
        else if (max <= 0)
            msg = [NSString stringWithFormat:[@"VALUE_RANGE_MSG_LOWER_BOUND" localize], min];
        else if (min == max && min >= 0)
            msg = [NSString stringWithFormat:[@"VALUE_RANGE_MSG_FIXED_LENGTH" localize],max];           
        else
            msg = [NSString stringWithFormat:[@"VALUE_RANGE_MSG" localize], min, max];
        
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:msg delegate:nil cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles: nil];
        [alert show];
    }
}

#pragma mark - Text input filter
- (BOOL)textField:(UITextField *)textCField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{    
    if (self.textField.keyboardType == UIKeyboardTypeNumberPad) 
    {
        NSCharacterSet *set = [[NSCharacterSet characterSetWithCharactersInString:NUMERIC_INPUT_FILTER] invertedSet];
        NSString *filteredInputString = [[string componentsSeparatedByCharactersInSet:set] componentsJoinedByString:@""];
        return [string isEqualToString:filteredInputString];
    }
    else 
        return YES;
}

- (BOOL)textFieldShouldEndEditing:(UITextField *)textCField
{
    [textCField resignFirstResponder];
    return YES;
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    return YES;
}
@end
