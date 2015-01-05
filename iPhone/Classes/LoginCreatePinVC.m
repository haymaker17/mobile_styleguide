//
//  LoginCreatePinVC.m
//  ConcurMobile
//
//  Created by Sally Yan on 6/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "LoginCreatePinVC.h"
#import "KeychainManager.h"
#import "UIColor+CCPalette.h"
#import "NSString+Additions.h"


@interface LoginCreatePinVC ()
{
    UIView      *headerView;
    UIView      *footerView;
    NSString    *firstEntryPin;
    NSString    *secondEntryPin;
    NSString    *email;
    NSString    *userPin;
    NSString    *userID;
    BOOL        isEmailLinkExpired;
    int         resetPinAttemptCount;
}
- (IBAction)btnSubmitPressed:(id)sender;

@end

@implementation LoginCreatePinVC
@synthesize tableList;


- (void)viewDidLoad
{
    [super viewDidLoad];
    // Concur Blue : 0.0 0.471 0.784
    self.view.backgroundColor = [UIColor concurBlueColor];
    self.tableList.backgroundColor = [UIColor clearColor];

    [self makeFooterView];
    self.title = [Localizer getLocalizedText:@"CREATE_PIN_TITLE"];
    resetPinAttemptCount = 0;
    
    [self.tableList setUserInteractionEnabled:YES];
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tableListWasTapped:)];
    tap.cancelsTouchesInView = NO;
    [self.tableList addGestureRecognizer:tap];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self markFirstResponder:0];
}

- (void)tableListWasTapped:(UITapGestureRecognizer*)gesture
{
    [self.view findAndResignFirstResponder];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 2;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger row = [indexPath row];

    EditInlineCell *cell = (EditInlineCell *)[tableView dequeueReusableCellWithIdentifier: @"EditInlineCell"];
    
    if (cell == nil)
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"EditInlineCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[EditInlineCell class]])
                cell = (EditInlineCell *)oneObject;
    }

    cell.parentVC = self;
    cell.rowPos = indexPath.row;
    
    NSString *pswPlaceHolder = [Localizer getLocalizedText:@"ENTER_NEW_PIN"];
    NSString *confrimPswPlaceHolder = [Localizer getLocalizedText:@"CONFIRM_PIN"];
    if (row == 0)
    {
        cell.txt.placeholder = pswPlaceHolder;
        cell.txt.returnKeyType = UIReturnKeyNext;
    }
    else if (row == 1)
    {
        cell.txt.placeholder = confrimPswPlaceHolder;
        cell.txt.returnKeyType = UIReturnKeyGo;
    }
    cell.txt.secureTextEntry = YES;
    cell.txt.keyboardType = UIKeyboardTypeDefault;
    
    return cell;
}



-(UIView*) tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
    return footerView;
}

- (CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    return [footerView frame].size.height;
}

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 0;
}

- (CGFloat) tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
   return 44;
}

#pragma mark - Table footer view methods
- (void) makeFooterView
{
    int maxW = 280;
    int xOffset = 15;
    
    UIButton *btnSubmit = [UIButton buttonWithType:UIButtonTypeCustom];
    btnSubmit.frame = CGRectMake(0, 0, 302, 45);
    btnSubmit.layer.cornerRadius = 3.0f;
    
    [btnSubmit setTitle:[Localizer getLocalizedText:@"Set Mobile PIN"]  forState:UIControlStateNormal];
    btnSubmit.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    btnSubmit.titleLabel.font = [UIFont systemFontOfSize:14.0];
    
    //0.0 0.663 0.949 1.0 BrightBlueConcur
    btnSubmit.backgroundColor = [UIColor concurBrightBlueColor];
    btnSubmit.frame = CGRectMake(9, 15, btnSubmit.frame.size.width, btnSubmit.frame.size.height);
    [btnSubmit setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    btnSubmit.titleLabel.font = [UIFont fontWithName:@"Helvetica Neue" size:18.0];
    [btnSubmit addTarget:self action:@selector(btnSubmitPressed:) forControlEvents:UIControlEventTouchUpInside];
    
    footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 320, 200)];
    [footerView addSubview:btnSubmit];

    UILabel *pinNotes = [[UILabel alloc] initWithFrame:CGRectMake(xOffset, 45, maxW, 175)];
    
    NSString *notes = [Localizer getLocalizedText:@"CREATE_PIN_NOTES"];
    NSString *pinMobileNotes = [Localizer getLocalizedText:@"PIN_MOBILE_NOTES"];
    
    NSString *footerNote = [NSString stringWithFormat:@"%@\r\r%@", notes, pinMobileNotes];
    [self prepareLabel:pinNotes withTitle:footerNote isHeaderTitle:NO];

    [footerView addSubview:pinNotes];
}


- (void)prepareLabel:(UILabel *)lbl withTitle:(NSString *)txt isHeaderTitle: (BOOL) isTitle
{
    [lbl setText:txt];
    [lbl setTextColor:[UIColor testDriveFontLightColor]];
    [lbl setFont:[UIFont systemFontOfSize:12.0]];
    [lbl setFont:[UIFont fontWithName:@"Helvetica Neue" size:18.0]];
    [lbl setTextAlignment:NSTextAlignmentCenter];
    [lbl setBackgroundColor:[UIColor clearColor]];
    [lbl setHighlightedTextColor:[UIColor whiteColor]];
    [lbl setAdjustsFontSizeToFitWidth:YES];
    [lbl setNumberOfLines:0];
    [lbl setMinimumScaleFactor:10.0/[lbl.font pointSize]];
}


#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{

}

#pragma mark - EditInlineCellDelegate Methods

- (BOOL)cellTextFieldShouldReturn:(EditInlineCell *)cell
{
    if ([cell.txt.placeholder isEqualToString:[Localizer getLocalizedText:@"ENTER_NEW_PIN"]])
        [self markFirstResponder:1];
    else if ([cell.txt.placeholder isEqualToString:[Localizer getLocalizedText:@"CONFIRM_PIN"]])
        [self btnSubmitPressed:nil];
    return YES;
}

-(IBAction) cellTextEdited:(EditInlineCell*)cell
{
    if( cell.rowPos ==0 && [cell.txt.text lengthIgnoreWhitespace])
        firstEntryPin = cell.txt.text;
    else if (cell.rowPos == 1 && [cell.txt.text lengthIgnoreWhitespace])
        secondEntryPin = cell.txt.text;
}

-(void) cellScrollMeUp:(EditInlineCell*)cell
{
    self.tableList.contentInset =  UIEdgeInsetsMake(0, 0, 130, 0);
    [self.tableList scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:cell.rowPos inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
}

#pragma mark - Text Field Methods
-(void) markFirstResponder:(int) row
{
    NSUInteger path[2] = {0, row};
	NSIndexPath *ip = [[NSIndexPath alloc] initWithIndexes:path length:2];
    EditInlineCell *cell = (EditInlineCell*) [tableList cellForRowAtIndexPath:ip];
    [cell.txt becomeFirstResponder];
}

-(void) resignFirstResponder:(int) row
{
    NSUInteger path[2] = {0, row};
	NSIndexPath *ip = [[NSIndexPath alloc] initWithIndexes:path length:2];
    EditInlineCell *cell = (EditInlineCell*) [tableList cellForRowAtIndexPath:ip];
    [cell.txt resignFirstResponder];
}

- (void) clearTextField: (int) row
{
    NSUInteger path[2] = {0, row};
	NSIndexPath *ip = [[NSIndexPath alloc] initWithIndexes:path length:2];
    EditInlineCell *cell = (EditInlineCell*) [tableList cellForRowAtIndexPath:ip];
    cell.txt.text = @"";
    firstEntryPin = @"";
    secondEntryPin = @"";
}

#pragma mark - Button Methods
- (IBAction)btnSubmitPressed:(id)sender
{
    resetPinAttemptCount++;
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Offline"]
                              message:[Localizer getLocalizedText:@"RESET_PIN_OFFLINE_MSG"]
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [alert show];
        [self clearTextField:0];
        [self clearTextField:1];
        [self resignFirstResponder: 0];
        [self resignFirstResponder: 1];
        [self resetAttemptFlurry:@"Failure" failureType:@"Other Error"];
        return;
    }
    else
    {
        if ([firstEntryPin isEqualToString:secondEntryPin] && [firstEntryPin lengthIgnoreWhitespace] && [secondEntryPin lengthIgnoreWhitespace])
        {
            NSDictionary *dictionary = @{@"Action": @"Reset PIN"};
            [Flurry logEvent:@"Sign In: PIN Reset" withParameters:dictionary];

            // save the email and clientKey to the keychain
            KeychainManager *keychainManager = [[KeychainManager alloc] init];
            email = [keychainManager loadPinResetEmailToken];

            NSString *resetPinKeyPartA = [keychainManager loadPinResetClientToken];
            NSString* unUrlEncodedEmail = [email stringByReplacingOccurrencesOfString:@"%40" withString:@"@"];
            NSString *resetPinKeyPartB = [[ApplicationLock sharedInstance] getResetPinKeypart];
            if ([resetPinKeyPartA lengthIgnoreWhitespace] && [resetPinKeyPartB lengthIgnoreWhitespace])
            {
                NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:unUrlEncodedEmail, @"RESET_PIN_EMAIL", resetPinKeyPartA, @"RESET_PIN_CLIENT_KEY", resetPinKeyPartB, @"RESET_PIN_SERVER_KEY", secondEntryPin, @"MOBILE_PIN", nil];

                [[ExSystem sharedInstance].msgControl createMsg:RESET_PIN_RESET_USER_PIN CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
                [self showWaitViewWithText:[Localizer getLocalizedText: @"Resetting the PIN"]];
            }
        }
        else
        {
            NSString *reenterPinMsg = nil;
            NSString *reenterPinTitle = nil; //[[NSString alloc] initWithString:[Localizer getLocalizedText:@"TITLE_REGISTER_REENTER_PIN"]];
            if (![firstEntryPin lengthIgnoreWhitespace] || ![secondEntryPin lengthIgnoreWhitespace])
            {
                reenterPinTitle = [[NSString alloc] initWithString:[Localizer getLocalizedText:@"TITLE_REGISTER_REENTER_PIN"]];
                reenterPinMsg = [Localizer getLocalizedText:@"Please enter and confirm the PIN"];
            }
            else if (![firstEntryPin isEqualToString:secondEntryPin])
            {
                reenterPinTitle = [Localizer getLocalizedText:@"Entries Don't Match"];
                reenterPinMsg = [Localizer getLocalizedText:@"ENTRIES_NOT_MATCH_MSG"];
            }
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:reenterPinTitle
                                  message:reenterPinMsg
                                  delegate:self
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
            [self clearTextField:0];
            [self clearTextField:1];
            [self resetAttemptFlurry:@"Failure" failureType:@"Bad Pins"];
            [alert show];
        }
    }
}

#pragma mark -
#pragma mark MVC Methods
-(void)respondToFoundData:(Msg *)msg
{
    [super respondToFoundData:msg];
    
	[self hideWaitView];
    [self hideLoadingView];
    
    if([msg.idKey isEqualToString:RESET_PIN_RESET_USER_PIN])
    {
        ResetUserPin *responseData = (ResetUserPin*)msg.responder;
        NSString *errMsg = responseData.errMsg;
        NSString *msgBody = nil;
        isEmailLinkExpired = FALSE;
        
        if (errMsg == nil && [responseData.status isEqualToString:@"SUCCESS"])
        {
            // login with the new pin
            if ([responseData.userID lengthIgnoreWhitespace])
            {
                [ExSystem sharedInstance].userName = responseData.userID;
            }
            
            [ConcurMobileAppDelegate unwindToRootView];
            
            NSString *newPIN = [NSString stringByEncodingXmlEntities:secondEntryPin];
            [ExSystem sharedInstance].pin = newPIN;
            [ApplicationLock sharedInstance].isUserLoggedIn  = YES;
            [ApplicationLock sharedInstance].isShowLoginView = YES;
            [[ApplicationLock sharedInstance] attemptAutoLogin];
            
            //flurry for success pin reset
            [self resetPinSuccessFlurry];
            [self resetAttemptFlurry:@"Success" failureType:nil];
        }
        else if (errMsg == nil && msg.responder != nil)
        {
            if (responseData.actionStatus != nil && responseData.actionStatus.errMsg != nil)
            {
                errMsg = responseData.actionStatus.errMsg;
                [self resetAttemptFlurry:@"Failure" failureType:@"Other Error"];
            }
        }
        else if (errMsg != nil)
        {
            if ([errMsg isEqualToString:@"error.mismatched_keys"])
            {
                errMsg = [Localizer getLocalizedText:@"Invalid Device"];
                msgBody = [Localizer getLocalizedText:@"INVALID_DEVICE_MSG"];
                [self resetAttemptFlurry:@"Failure" failureType:@"Invalid Device"];
            }
            else if ([errMsg isEqualToString:@"error.request_expired"])
            {
                isEmailLinkExpired = TRUE;
                errMsg = [Localizer getLocalizedText:@"Email link expired"];
                msgBody = [Localizer getLocalizedText:@"EMAIL_LINK_EXPIRED_MSG"];
                [self resetAttemptFlurry:@"Failure" failureType:@"Request Expired"];
            }
            else if ([responseData.PinMinLength lengthIgnoreWhitespace])
            {
                int pinLen =  (int)secondEntryPin.length;
                if ( pinLen < [responseData.PinMinLength intValue])
                {
                    errMsg = [Localizer getLocalizedText:@"Invalid PIN length"];
                    msgBody = [NSString stringWithFormat:[Localizer getLocalizedText:@"INVALID_PIN_LEN_MSG"], responseData.PinMinLength];
                    [self resetAttemptFlurry:@"Failure" failureType:@"Invalid Length"];
                }
                else{
                    msgBody = errMsg;
                    errMsg = [Localizer getLocalizedText:@"Invalid PIN"];
                    [self resetAttemptFlurry:@"Failure" failureType:@"Bad Pins"];
                }

            }
            else if ([errMsg isEqualToString:@"error.mobile_disabled"])
            {
                errMsg = [Localizer getLocalizedText:@"Mobile Disabled"];
                msgBody = [Localizer getLocalizedText:@"MOBILE_DISABLED_MSG"];
                [self resetAttemptFlurry:@"Failure" failureType:@"Mobile Disabled"];
            }
            else
            {
                msgBody = errMsg;
                errMsg = [Localizer getLocalizedText:@"Error"];
                [self resetAttemptFlurry:@"Failure" failureType:@"Other Error"];
            }
        }       // end of error response
        
        if ([errMsg lengthIgnoreWhitespace])
        {
            if (isEmailLinkExpired)
            {
                UIAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:errMsg
                                      message:msgBody
                                      delegate:self
                                      cancelButtonTitle:[Localizer getLocalizedText:@"Cancel"]
                                      otherButtonTitles:[Localizer getLocalizedText:@"TRY_AGAIN"], nil];
                [alert show];
            }
            else
            {
                UIAlertView *alert = [[MobileAlertView alloc]
                                      initWithTitle:errMsg
                                      message:msgBody
                                      delegate:self
                                      cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                      otherButtonTitles:nil];
                
                [alert show];
            }
            [self clearTextField:0];
            [self clearTextField:1];
        }           // end of the alert view
    }
}

#pragma mark - AlertView delegate methods
- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    if (isEmailLinkExpired)
    {
        if (buttonIndex == alertView.cancelButtonIndex)     // go to the login view
            [self.navigationController popToRootViewControllerAnimated:NO];
        else    // go to the login help screen to try the pin reset again
        {
            NSArray *viewControllers = self.navigationController.viewControllers;
            NSUInteger count = [viewControllers count];
            if (count > 2)
            {
                if ([viewControllers[count - 2] isKindOfClass:[LoginHelpContentsVC class]])
                    [self.navigationController popViewControllerAnimated:NO];
                
            }
            else{
                LoginHelpContentsVC *helpVC = [[LoginHelpContentsVC alloc] initWithNibName:@"LoginHelpContentsVC" bundle:nil];
                [self.navigationController pushViewController:helpVC animated:nil];
            }
        }
            
    }
    else{
        [self resignFirstResponder:1];
        [self markFirstResponder:0];
        
    }
}

#pragma mark - Flurry
- (void)resetAttemptFlurry:(NSString*)status failureType:(NSString*)type
{
    NSDictionary *dict = nil;
    if ([status isEqualToString:@"Success"]) {
        dict = @{@"Status": status};
    } else {
        if (type == nil) { type = @"";} // nsdictionary cannot store nil
        dict = @{@"Status": status, @"Failure Type:": type};
    }
    
    [Flurry logEvent:@"Sign In: Reset Pin Attempt" withParameters:dict];
}

- (void)resetPinSuccessFlurry
{
    NSString *count = nil;
    if (resetPinAttemptCount <= 3)
        count = [NSString stringWithFormat:@"%i", resetPinAttemptCount];
    else
        count = @"Over 3";
    
    NSDictionary *dict = @{@"Attempt Count": count};
    [Flurry logEvent:@"Sign In: Reset Pin Success" withParameters:dict];
}

@end
