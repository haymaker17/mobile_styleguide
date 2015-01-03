//
//  EmergencyContactVC.m
//  ConcurMobile
//
//  Created by Ray Chi on 12/22/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "EmergencyContactVC.h"
#import "ProfileTelCell2.h"
#import "CityCell.h"
#import "SwitchCell.h"
#import "RPFloatingPlaceholderTextField.h"
#import <AddressBook/AddressBook.h>
#import <AddressBookUI/AddressBookUI.h>


#define kTagName        1181
#define kTagRelation    1182
#define kTagTelePhone   1183
#define kTagAddress     1184
#define kTagCity        1185
#define kTagState       1186
#define kTagZipCode     1187
#define kTagCountry     1188
#define kTagSwitch      1189            // switch control tag for same address

@interface EmergencyContactVC () <ABPeoplePickerNavigationControllerDelegate,ABPersonViewControllerDelegate>

@property (nonatomic,strong) UISwitch *addressSwitch;
@property (nonatomic,assign) ABAddressBookRef addressBook;     //Address Book
@property (nonatomic,assign) ABMultiValueRef phoneNumbers;     //group of phone numbers
@property (nonatomic,strong) NSMutableDictionary *dict;        //dictionary for saving the user input, internal use

@end

@implementation EmergencyContactVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableView.contentInset = UIEdgeInsetsMake(-33, 0, 0, 0);
    
    //
    // Chose from iphone's phone book preparation
    _addressBook = ABAddressBookCreateWithOptions(NULL, NULL);
    self.dict = [[NSMutableDictionary alloc] init];
    
    //
    // Hide Keyboard once touch anywhere else
    UITapGestureRecognizer *tapGr = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewTapped:)];
    tapGr.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tapGr];

}

-(void)viewTapped:(UITapGestureRecognizer*)tapGr
{
    [self.view endEditing:YES];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSInteger *ret = 0;
    if(section==0) {
        ret = 3;
    }
    else if(section==1){
        ret = 5;
    }
    return ret;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    
    if(section==0){                 // Name, Relationship and Phone number
        switch(row){
            case 0:{        //name
                ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell2"];
                cell.textField.delegate = self;
                cell.textField.placeholder = @"Name";
                cell.textField.tag = kTagName;
                cell.textField.text = [self.dict objectForKey:@"name"];
                return cell;
                break;
            }
            case 1:{        //relationship
                ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell2"];
                cell.textField.delegate = self;
                cell.textField.placeholder = @"Relationship";
                cell.textField.tag = kTagRelation;
                cell.textField.text = [self.dict objectForKey:@"relation"];
                return cell;
                break;
            }
            case 2:{        //tel
                ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell2"];
                cell.textField.delegate = self;
                cell.textField.placeholder = @"Phone Number";
                cell.textField.keyboardType = UIKeyboardTypePhonePad;
                cell.textField.tag = kTagTelePhone;
                cell.textField.text = [self.dict objectForKey:@"phone"];
                return cell;
                break;
            }
            default:
                break;
        }
        
        
    }
    else if(section==1){            // Address
        switch (row) {
            case 0:{        //switch button
                SwitchCell *cell = (SwitchCell*)[tableView dequeueReusableCellWithIdentifier: @"addressSwitch"];
                cell.label.text = @"Same As Empolyee";
                self.addressSwitch = cell.addressSwitch;
                return cell;
                break;
            }
            case 1:{        //address
                ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell2"];
                cell.textField.delegate = self;
                cell.textField.placeholder = @"Address";
                cell.textField.tag = kTagAddress;
                //
                // Set Address is switch is on
                if(self.addressSwitch.on){
                    cell.textField.text = [self.emergencyDict valueForKey:@"address"];
                    cell.userInteractionEnabled = NO;
                }
                else{
                    cell.textField.text = [self.dict objectForKey:@"address"];
                    cell.userInteractionEnabled = YES;
                }
                
                return cell;
                break;
            }
            case 2:{        //city & state
                CityCell *cell = (CityCell*)[tableView dequeueReusableCellWithIdentifier:@"CityCell"];
                cell.textField1.delegate = self;
                cell.textField2.delegate = self;
                cell.textField1.tag = kTagCity;
                cell.textField2.tag = kTagState;
                //
                // Set Address is switch is on
                if(self.addressSwitch.on){
                    cell.textField1.text = [self.emergencyDict valueForKey:@"city"];
                    cell.textField2.text = [self.emergencyDict valueForKey:@"state"];
                    cell.userInteractionEnabled = NO;
                }
                else{
                    cell.textField1.text = [self.dict objectForKey:@"city"];
                    cell.textField2.text = [self.dict objectForKey:@"state"];
                    cell.userInteractionEnabled = YES;
                }
                
                return cell;
                break;
            }
            case 3:{        //zipcode & country
                CityCell *cell = (CityCell*)[tableView dequeueReusableCellWithIdentifier:@"CityCell"];
                cell.textField1.delegate = self;
                cell.textField2.delegate = self;
                cell.textField1.placeholder = @"Zip Code";
                cell.textField2.placeholder = @"Country";
                cell.textField1.tag = kTagZipCode;
                cell.textField2.tag = kTagCountry;
                cell.textField2.returnKeyType = UIReturnKeyDone;
                //
                // Set Address is switch is on
                if(self.addressSwitch.on){
                    cell.textField1.text = [self.emergencyDict valueForKey:@"zipcode"];
                    cell.textField2.text = [self.emergencyDict valueForKey:@"country"];
                    cell.userInteractionEnabled = NO;
                }
                else{
                    cell.textField1.text = [self.dict objectForKey:@"zipcode"];
                    cell.textField2.text = [self.dict objectForKey:@"country"];
                    cell.userInteractionEnabled = YES;
                }
                
                return cell;
                break;
            }
            case 4:{        //choose from address book
                UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"AddressBookCell"];
                return cell;
                break;
            }
            default:
                break;
        }
    }
    
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    if(indexPath.section == 1 && indexPath.row == 4)
        return 60;
    return 44;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    if(section==0){
        ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView cellForRowAtIndexPath:indexPath];
        [cell.textField becomeFirstResponder];
        
    }
    else if(section == 1){
        switch (row) {
            case 0:{
                SwitchCell *cell = (SwitchCell*)[tableView cellForRowAtIndexPath:indexPath];
                cell.addressSwitch.on = !cell.addressSwitch.on;
                [self.tableView reloadData];
                break;
            }
            case 1:{
                ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView cellForRowAtIndexPath:indexPath];
                [cell.textField becomeFirstResponder];
                break;
            }
            case 2:case 3:{
                CityCell *cell = (CityCell*)[tableView cellForRowAtIndexPath:indexPath];
                [cell.textField1 becomeFirstResponder];
                break;
            }
            default:
                break;
        }

    }
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}

#pragma mark - Button Click
- (IBAction)btnClose:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)btnDone:(id)sender {
    
    [self.view endEditing:YES];
    
    if(self.addressSwitch.on == YES){
        [self.dict setValue:[self.emergencyDict valueForKey:@"address"] forKey:@"address"];
        [self.dict setValue:[self.emergencyDict valueForKey:@"city"] forKey:@"city"];
        [self.dict setValue:[self.emergencyDict valueForKey:@"state"] forKey:@"state"];
        [self.dict setValue:[self.emergencyDict valueForKey:@"zipcode"] forKey:@"zipcode"];
        [self.dict setValue:[self.emergencyDict valueForKey:@"country"] forKey:@"country"];
    }
    
    if(self.onSelected){
        self.onSelected([self.dict objectForKey:@"name"]);
    }
    
    //TODO: 1.creat CTEProfileEmergencyContact object
    //TODO: 2.save to database

    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)switchToggle:(id)sender {
    [self.tableView reloadData];
}

- (IBAction)btnChooseClick:(id)sender {
    [self checkAddressBookAccess];
}

#pragma mark - Text Field Delegate
/**
 *  keyboard hit Next/Done, go to next textfiled or hide keyboard
 */
-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if(textField.returnKeyType == UIReturnKeyNext){
        NSUInteger *nextIndex = textField.tag+1;
        UITextField *nextTextFiled = (UITextField*)[self.tableView viewWithTag:nextIndex];
        [nextTextFiled becomeFirstResponder];
        return NO;
    }
    else if(textField.returnKeyType == UIReturnKeyDone){
        [textField resignFirstResponder];
        return NO;
    }
    return YES;
}

-(void)textFieldDidBeginEditing:(UITextField *)textField
{
    CGRect frame = textField.frame;
    int offset = frame.origin.y + 32 - (self.view.frame.size.height - 216.0);
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:.2f];
    
    if(offset > 0){
        self.view.frame = CGRectMake(0.0f, -offset, self.view.frame.size.width, self.view.frame.size.height);
    }
    
    [UIView commitAnimations];
}

-(void)textFieldDidEndEditing:(UITextField *)textField
{
    switch (textField.tag) {
        case kTagName:
            [self.dict setValue:textField.text forKey:@"name"];
            break;
        case kTagRelation:
            [self.dict setValue:textField.text forKey:@"relation"];
            break;
        case kTagTelePhone:
            [self.dict setValue:textField.text forKey:@"phone"];
            break;
        case kTagAddress:
            [self.dict setValue:textField.text forKey:@"address"];
            break;
        case kTagCity:
            [self.dict setValue:textField.text forKey:@"city"];
            break;
        case kTagState:
            [self.dict setValue:textField.text forKey:@"state"];
            break;
        case kTagZipCode:
            [self.dict setValue:textField.text forKey:@"zipcode"];
            break;
        case kTagCountry:
            [self.dict setValue:textField.text forKey:@"country"];
            break;
        default:
            break;
    }
    
    self.view.frame =CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);
}

-(BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
    if(textField.tag==kTagTelePhone){
        NSMutableString *newString = [NSMutableString stringWithString:textField.text];
        [newString replaceCharactersInRange:range withString:string];
        
        NSString *countryCode = [[NSLocale currentLocale] objectForKey:NSLocaleCountryCode];
        NSString *phoneNo = [NSString formatPhoneNo:newString withLocale:countryCode];
        textField.text = phoneNo;
        
        RPFloatingPlaceholderTextField *curTextfiled = (RPFloatingPlaceholderTextField *)textField;
        curTextfiled.floatingLabel.textColor = [UIColor colorWithRed:0 green:122/255.0 blue:1 alpha:1];
        return NO;
    }
    return YES;
}

#pragma mark - Address Book Access
/**
 *  Check the authorization status of our application for Address Book
 */
-(void)checkAddressBookAccess
{
    switch (ABAddressBookGetAuthorizationStatus())
    {
            // Update our UI if the user has granted access to their Contacts
        case  kABAuthorizationStatusAuthorized:
            [self showPeoplePickerController];
            break;
            // Prompt the user for access to Contacts if there is no definitive answer
        case  kABAuthorizationStatusNotDetermined :
            [self requestAddressBookAccess];
            break;
            // Display a message if the user has denied or restricted access to Contacts
        case  kABAuthorizationStatusDenied:
        case  kABAuthorizationStatusRestricted:
        {
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Privacy Warning"
                                                            message:@"Permission was not granted for Contacts."
                                                           delegate:nil
                                                  cancelButtonTitle:@"OK"
                                                  otherButtonTitles:nil];
            [alert show];
        }
            break;
        default:
            break;
    }
}

/**
 *  Prompt the user for access to their Address Book data
 */
-(void)requestAddressBookAccess
{
    EmergencyContactVC * __weak weakSelf = self;
    
    ABAddressBookRequestAccessWithCompletion(self.addressBook, ^(bool granted, CFErrorRef error)
                                             {
                                                 if (granted)
                                                 {
                                                     dispatch_async(dispatch_get_main_queue(), ^{
                                                         [weakSelf showPeoplePickerController];
                                                         
                                                     });
                                                 }
                                             });
}


#pragma mark ABPeople Functions and Delegate
-(void)showPeoplePickerController
{
    ABPeoplePickerNavigationController *picker = [[ABPeoplePickerNavigationController alloc] init];
    picker.peoplePickerDelegate = self;
    
    NSArray *displayedItems = [NSArray arrayWithObjects:[NSNumber numberWithInt:kABPersonFirstNameProperty],
                                                        [NSNumber numberWithInt:kABPersonLastNameProperty],
                                                        [NSNumber numberWithInt:kABPersonMiddleNameProperty],
                                                        [NSNumber numberWithInt:kABPersonPrefixProperty],
                                                        [NSNumber numberWithInt:kABPersonPhoneProperty],
                                                        [NSNumber numberWithInt:kABPersonRelatedNamesProperty], nil];
    picker.displayedProperties = displayedItems;
    // Show the picker
    [self presentViewController:picker animated:YES completion:nil];
}


// Save the emergency contact chosed from phone book to dictionary
-(void)peoplePickerNavigationController:(ABPeoplePickerNavigationController *)peoplePicker didSelectPerson:(ABRecordRef)person
{
    
    self.dict = [[NSMutableDictionary alloc]
                                            initWithObjects:@[@"", @"", @"", @"", @"", @"", @""]
                                            forKeys:@[@"name", @"phone", @"address",@"city",@"state",@"zipcode",@"country"]];
    
    // Get the data.
    // name
    NSString *preFix = (__bridge_transfer NSString *)ABRecordCopyValue(person, kABPersonPrefixProperty);
    NSString *firstName = (__bridge_transfer NSString *)ABRecordCopyValue(person, kABPersonFirstNameProperty);
    NSString *middleName = (__bridge_transfer NSString *)ABRecordCopyValue(person, kABPersonMiddleNameProperty);
    NSString *lastName = (__bridge_transfer NSString *)ABRecordCopyValue(person, kABPersonLastNameProperty);
    
    if(![preFix isEqualToString:@""]){
        preFix = [preFix stringByAppendingString:@" "];
    }
    if(![firstName isEqualToString:@""]){
        firstName = [firstName stringByAppendingString:@" "];
    }
    if(![middleName isEqualToString:@""]){
        middleName = [middleName stringByAppendingString:@" "];
    }
    
    NSString *name = [NSString stringWithFormat:@"%@%@%@%@",preFix ?: @"",
                                                         firstName ?: @"",
                                                        middleName ?: @"",
                                                          lastName ?: @""];
    [self.dict setValue:name forKey:@"name"];
    
    //
    // phone number
    self.phoneNumbers = ABRecordCopyValue(person,kABPersonPhoneProperty);
    if(ABMultiValueGetCount(self.phoneNumbers) == 1){
        NSString *phoneNo = (__bridge_transfer NSString*)ABMultiValueCopyValueAtIndex(self.phoneNumbers, 0);
        [self.dict setValue:phoneNo forKey:@"phone"];
    }
    else if(ABMultiValueGetCount(self.phoneNumbers) > 1){
        
        UIAlertView *av = [[UIAlertView alloc] initWithTitle:@"Pick A Number"
                                                     message:@"Which number would you like to use as Emegency Contact?"
                                                    delegate:self
                                           cancelButtonTitle:@"Cancel"
                                           otherButtonTitles:nil];
        
        for (int i=0; i < ABMultiValueGetCount(self.phoneNumbers); i++)
            [av addButtonWithTitle:(__bridge_transfer NSString*)ABMultiValueCopyValueAtIndex(self.phoneNumbers, i)];
        [av show];
    }
    
    //
    // address --- just use the first address, make it simple.
    ABMultiValueRef addressRef = ABRecordCopyValue(person, kABPersonAddressProperty);
    if (ABMultiValueGetCount(addressRef) > 0) {
        
        NSDictionary *addressDict = (__bridge NSDictionary *)ABMultiValueCopyValueAtIndex(addressRef, 0);
        
        [self.dict setValue:[addressDict objectForKey:(NSString *)kABPersonAddressStreetKey] forKey:@"address"];
        [self.dict setValue:[addressDict objectForKey:(NSString *)kABPersonAddressCityKey] forKey:@"city"];
        [self.dict setValue:[addressDict objectForKey:(NSString *)kABPersonAddressStateKey] forKey:@"state"];
        [self.dict setValue:[addressDict objectForKey:(NSString *)kABPersonAddressZIPKey] forKey:@"zipcode"];
        [self.dict setValue:[addressDict objectForKey:(NSString *)kABPersonAddressCountryKey] forKey:@"country"];
    }
    
    //
    // dismiss view controller and update the talbe
    [peoplePicker dismissViewControllerAnimated:YES completion:^{
        [self.tableView reloadData];
    }];
}


-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if(buttonIndex == alertView.cancelButtonIndex){
        //if click cancel, chose the first phone number
        NSString *tmpPhone = (__bridge_transfer NSString*)ABMultiValueCopyValueAtIndex(self.phoneNumbers, 0);
        [self.dict setValue:tmpPhone forKey:@"phone"];
    }
    else{
        //cancel button index = 0, so we use buttonIndex-1 here
        NSString *tmpPhone = (__bridge_transfer NSString*)ABMultiValueCopyValueAtIndex(self.phoneNumbers, buttonIndex-1);
        [self.dict setValue:tmpPhone forKey:@"phone"];
    }
    [self.tableView reloadData];
}

-(BOOL)personViewController:(ABPersonViewController *)personViewController shouldPerformDefaultActionForPerson:(ABRecordRef)person property:(ABPropertyID)property identifier:(ABMultiValueIdentifier)identifier
{
    return YES;
}

@end
