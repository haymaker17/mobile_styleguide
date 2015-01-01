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

#define kTagName        1181
#define kTagRelation    1182
#define kTagTelePhone   1183
#define kTagAddress     1184
#define kTagCity        1185
#define kTagState       1186
#define kTagZipCode     1187
#define kTagCountry     1188
#define kTagSwitch      1189            // switch control tag for same address

@interface EmergencyContactVC ()

@property (nonatomic,strong) UISwitch *addressSwitch;

@end

@implementation EmergencyContactVC

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tableView.contentInset = UIEdgeInsetsMake(-33, 0, 0, 0);
    
    //Chose from iphone's phone book button
    
    
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
    // Return the number of sections.
    return 2;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSInteger *ret = 0;
    if(section==0) {
        ret = 3;
    }
    else if(section==1){
        ret = 4;
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
                return cell;
                break;
            }
            case 1:{        //relationship
                ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell2"];
                cell.textField.delegate = self;
                cell.textField.placeholder = @"Relationship";
                cell.textField.tag = kTagRelation;
                return cell;
                break;
            }
            case 2:{        //tel
                ProfileTelCell2 *cell = (ProfileTelCell2*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell2"];
                cell.textField.delegate = self;
                cell.textField.placeholder = @"Phone Number";
                cell.textField.keyboardType = UIKeyboardTypePhonePad;
                cell.textField.tag = kTagTelePhone;
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
                    cell.userInteractionEnabled = YES;
                }
                
                return cell;
                break;
            }
            default:
                break;
        }
    }
    
    return nil;
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


/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (IBAction)btnClose:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)btnDone:(id)sender {
}

- (IBAction)switchToggle:(id)sender {
    if(self.addressSwitch.on == YES){
        NSLog(@"ON");
        [self.tableView reloadData];
    }
    else{
        NSLog(@"OFF");
        [self.tableView reloadData];
    }
}

- (IBAction)btnChooseClick:(id)sender {
}

#pragma mark -- text field delegate
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

@end
