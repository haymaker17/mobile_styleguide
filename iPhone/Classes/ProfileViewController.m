//
//  ProfileViewController.m
//  ConcurMobile
//
//  Created by Ray Chi on 11/13/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ProfileViewController.h"
#import "CardCell.h"
#import "ProfileTelCell.h"
#import "NameCell.h"
#import "CityCell2.h"
#import "RPFloatingPlaceholderTextField.h"

#define kTagFirstName   1181
#define kTagLastName    1182
#define kTagTelePhone   1183
#define kTagAddress     1184
#define kTagCity        1185
#define kTagState       1186
#define kTagZipCode     1187
#define kTagCountry     1188

@interface ProfileViewController ()


@end

@implementation ProfileViewController

- (instancetype)initWithTitle
{
    ProfileViewController *vc = [[UIStoryboard storyboardWithName:[@"Profile" storyboardName] bundle:nil] instantiateViewControllerWithIdentifier:@"ProfileVC"];
    vc.navigationItem.title = [@"Profile" localize];
    return vc;
}

- (IBAction)btnCloseClick:(id)sender {
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)btnSaveClick:(id)sender {
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.

//    CTEProfilePersonalInfo *test = [[CTEProfilePersonalInfo alloc] initWithFirstName:@"Ray"
//                                                                            lastName:@"CHI"
//                                                                             phoneNo:@"2222222222"
//                                                                         workAddress:@"Concur"
//                                                                                city:@"Bellevue"
//                                                                               state:@"WA"
//                                                                             country:@"USE"
//                                                                             zipCode:@"98008"];

    
    //Register Nib
    [self.tableView registerNib:[UINib nibWithNibName:@"CardCell" bundle:nil] forCellReuseIdentifier:@"CardCell"];
    [self.tableView registerNib:[UINib nibWithNibName:@"ProfileTelCell" bundle:nil] forCellReuseIdentifier:@"ProfileTelCell"];
    [self.tableView registerNib:[UINib nibWithNibName:@"NameCell" bundle:nil] forCellReuseIdentifier:@"NameCell"];
    [self.tableView registerNib:[UINib nibWithNibName:@"CityCell2" bundle:nil] forCellReuseIdentifier:@"CityCell2"];
    
    self.tableView.contentInset = UIEdgeInsetsMake(-33, 0, 0, 0);
    
    //
    // Hide Keyboard once touch anywhere else
    UITapGestureRecognizer *tapGr = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewTapped:)];
    tapGr.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:tapGr];
    
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    
    // Return the number of sections.
    return 4;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {

    //
    // section          use
    //    0         basic profile
    //    1         emergency contact
    //    2         credit card
    //    3         bank account
    NSInteger *ret = 0;
    switch (section) {
        case 0:
            ret = 5;
            break;
        case 1:
            ret = 1;
            break;
        case 2:
            ret = 1;
            break;
        case 3:
            ret = 1;
            break;
        default:
            break;
    }
    
    return ret;
}


 - (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
     
     NSUInteger section = [indexPath section];
     NSUInteger row = [indexPath row];
     
     
     if(section==0){            //Profile Group
         switch (row) {
             case 0:{
                 NameCell *cell = (NameCell*)[tableView dequeueReusableCellWithIdentifier:@"NameCell"];
                 cell.firstNameTextField.delegate = self;
                 cell.lastNameTextField.delegate = self;
                 cell.firstNameTextField.tag = kTagFirstName;
                 cell.lastNameTextField.tag = kTagLastName;
                 return cell;
                 break;
             }
             case 1:{
                 ProfileTelCell *cell = (ProfileTelCell*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell"];
                 cell.textField.delegate = self;
                 cell.textField.tag = kTagTelePhone;
                 cell.custonImageView.image = [UIImage imageNamed:@"icon_profile_phone"];
                 cell.textField.keyboardType = UIKeyboardTypePhonePad;
                 return cell;
                 break;
             }
             case 2:{
                 ProfileTelCell *cell = (ProfileTelCell*)[tableView dequeueReusableCellWithIdentifier:@"ProfileTelCell"];
                 cell.textField.delegate = self;
                 cell.textField.placeholder = @"Address";
                 cell.textField.tag = kTagAddress;
                 cell.custonImageView.image = [UIImage imageNamed:@"icon_profile_address"];
                 return cell;
                 break;
             }
             case 3:{
                 CityCell2 *cell = (CityCell2*)[tableView dequeueReusableCellWithIdentifier:@"CityCell2"];
                 cell.textField1.delegate = self;
                 cell.textField2.delegate = self;
                 cell.textField1.tag = kTagCity;
                 cell.textField2.tag = kTagState;
                 return cell;
                 break;
             }
             case 4:{
                 CityCell2 *cell = (CityCell2*)[tableView dequeueReusableCellWithIdentifier:@"CityCell2"];
                 cell.textField1.delegate = self;
                 cell.textField2.delegate = self;
                 cell.textField1.placeholder = @"Zip Code";
                 cell.textField1.tag = kTagZipCode;
                 cell.textField2.placeholder = @"Country";
                 cell.textField2.tag = kTagCountry;
                 cell.underLine.alpha = 0;
                 UILabel *underLine2 = [[UILabel alloc] initWithFrame:CGRectMake(0, 44, 320, 0.5f)];
                 underLine2.backgroundColor = [UIColor colorWithRed:182/255.0 green:182/255.0 blue:182/255.0 alpha:1];
                 [cell addSubview:underLine2];
                 return cell;
                 break;
             }
             default:
                 break;
         }
     }
     else if(section == 1){     //Emergency Contact
         CardCell *cell = [tableView dequeueReusableCellWithIdentifier:@"CardCell" forIndexPath:indexPath];
         cell.customImageView.image = [UIImage imageNamed:@"icon_profile_emergency"];
         return cell;
     }
     else if(section == 2){     //Credit Card
         CardCell *cell = [tableView dequeueReusableCellWithIdentifier:@"CardCell" forIndexPath:indexPath];
         cell.label.text = @"Add Credit Card";
         cell.customImageView.image = [UIImage imageNamed:@"icon_profile_credit_card"];
         return cell;
     }
     else{                     //Bank Account
         CardCell *cell = [tableView dequeueReusableCellWithIdentifier:@"CardCell" forIndexPath:indexPath];
         cell.label.text = @"Add Bank Account";
         cell.customImageView.image = [UIImage imageNamed:@"icon_profile_bank"];
         return cell;
     }
     
     // Configure the cell...
     return nil;
 }

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section{
    return 0;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    
    if(section==0){
        switch (row) {
            case 0:{
                NameCell *cell = (NameCell*)[tableView cellForRowAtIndexPath:indexPath];
                [cell.firstNameTextField becomeFirstResponder];
                break;
            }
            case 1:case 2:{
                ProfileTelCell *cell = (ProfileTelCell*)[tableView cellForRowAtIndexPath:indexPath];
                [cell.textField becomeFirstResponder];
                break;
            }
            case 3:case 4:{
                CityCell2 *cell = (CityCell2*)[tableView cellForRowAtIndexPath:indexPath];
                [cell.textField1 becomeFirstResponder];
                break;
            }
            default:
                break;
        }
        [tableView deselectRowAtIndexPath:indexPath animated:NO];
    }
    else if(section == 1){     //Emergency Contact
        [self performSegueWithIdentifier:@"Profile_Emergency_Contact" sender:self];
    }
    else if(section == 2){     //Credit Card
        
    }
    else if(section == 3){     //Bank Account
        
    }
}


#pragma mark - TextField Delegate
-(BOOL)textFieldShouldReturn:(UITextField *)textField
{
    if(textField.returnKeyType == UIReturnKeyNext){
        NSUInteger *nextIndex = textField.tag+1;
        UITextField *nextTextFiled = (UITextField*)[self.tableView viewWithTag:nextIndex];
        [nextTextFiled becomeFirstResponder];
        return NO;
    }
    return YES;
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

#pragma mark -- Keyboard functions
-(void)viewTapped:(UITapGestureRecognizer*)tapGr
{
    [self.view endEditing:YES];
}


#pragma mark - Navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
     if([[segue identifier] isEqualToString:@"Profile_Emergency_Contact"]){

     }
}


@end
