//
//  SelectOneEmailCell.h
//  ConcurMobile
//
//  Created by yiwen on 12/1/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AddressBook/AddressBook.h>
#import <AddressBookUI/AddressBookUI.h>
#import "SelectOneEmailDelegate.h"
#import "MobileViewController.h"

@interface SelectOneEmailCell : UITableViewCell <UITextFieldDelegate>
{
    UIButton                    *btnContacts;
    UITextField                 *txtContacts;
    NSString                    *email;
    id<SelectOneEmailDelegate>  __weak _delegate;
}

@property (strong, nonatomic) IBOutlet UIButton *btnContacts;
@property (strong, nonatomic) IBOutlet UITextField *txtContacts;
@property (strong, nonatomic) NSString *email;
@property (weak, nonatomic) id<SelectOneEmailDelegate>     delegate;

- (IBAction)showPicker:(id)sender;
#pragma mark - Set the text
-(BOOL) textFieldShouldReturn:(UITextField *)textField;
-(IBAction)editingText:(UITextField *)textField;
-(void)textFieldDidEndEditing:(UITextField *)textField;

@end
