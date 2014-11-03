//
//  LoginHelpContentsVC.m
//  ConcurMobile
//
//  Created by charlottef on 12/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "LoginHelpContentsVC.h"
#import "LoginHelpContentsCell.h"
#import "LoginHelpTopicVC.h"
#import "UIView+FindAndResignFirstResponder.h"
#import "KeychainManager.h"

@interface LoginHelpContentsVC ()
{
    NSMutableArray *sections;
    UIButton       *btnSubmit;
    NSString       *emailLoginID;
}
- (IBAction)btnSubmitPressed:(id)sender;

@end

@implementation LoginHelpContentsVC

@synthesize tableList, topics;

# define kSectionEmail @"LOGIN_EMAIL"
# define kSectionTopics @"HELP_TOPICS"


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Concur Blue : 0.0 0.471 0.784
    self.view.backgroundColor = [UIColor concurBlueColor] ;
    self.tableList.backgroundColor = [UIColor clearColor];

    self.topics = [LoginHelpTopic topics];
    sections = [[NSMutableArray alloc] initWithObjects:kSectionEmail, nil];
    [self.tableList setUserInteractionEnabled:YES];
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tableListWasTapped:) ];
    tap.cancelsTouchesInView = NO;
    [self.tableList addGestureRecognizer:tap];
}


- (void)tableListWasTapped:(UITapGestureRecognizer*)gesture
{
    
    [self.view findAndResignFirstResponder];
}

- (void) viewDidUnload
{
    [super viewDidUnload];
    
    self.tableList = nil;
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

    self.title = [Localizer getLocalizedText:@"Sign In Help"];
}

- (void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    
    self.title = nil; // We want the nav bar's back button to say "Back" whenever a new view is pushed onto the nav stack over this one.  Setting title to nil accomplishes that.
}

#pragma mark - UITableViewSource Methods

- (NSString*)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"";
}

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    NSString *sectionKey = sections[section];    
    if ([sectionKey isEqualToString:kSectionEmail])
    {
        return [self makeHeaderView];
    }
    return nil;
}

- (UIView*) tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
    NSString *sectionKey = sections[section];
    if ([sectionKey isEqualToString:kSectionEmail])
    {
        return [self makeFooterView];
    }
    else
        return nil;
}

- (CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    NSString *sectionKey = sections[section];
    if ([sectionKey isEqualToString:kSectionEmail])
    {
        return [[self makeHeaderView] frame ].size.height;
    }
    return 45;
}

- (CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    NSString *sectionKey = sections[section];
    if ([sectionKey isEqualToString:kSectionEmail])
    {
        return [[self makeFooterView] frame ].size.height;
    }
    return 0;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [sections count];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    NSString *sectionKey = sections[section];
    if ([sectionKey isEqualToString:kSectionEmail])
        return 1;
    else if ([sectionKey isEqualToString:kSectionTopics])
    {
        NSInteger *count = [topics count];
        return count;
    }
    else
        return 0;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    NSUInteger section = [indexPath section];
    NSString *sectionKey = sections[section];
    
    if ([sectionKey isEqualToString:kSectionEmail])
    {
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
        NSString *email = [Localizer getLocalizedText:@"REGISTERED_EMAIL"];
        
        cell.txt.placeholder = email;
        
        cell.txt.secureTextEntry = NO;
        cell.txt.keyboardType = UIKeyboardTypeEmailAddress;
        cell.txt.autocorrectionType = UITextAutocorrectionTypeNo;
        cell.txt.returnKeyType = UIReturnKeyGo;
        [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
        
        return cell;
    }
    
    
    return nil;
}

#pragma mark - header and footer view methods

- (UIView*) makeHeaderView
{
    int xOffset = 30;
    int maxW = 260;
//    if ([UIDevice isPad])
//        xOffset = 250;
    
    UILabel *header = [[UILabel alloc] initWithFrame:CGRectMake(xOffset, 10, maxW, 55)];
    NSString *headerNotes = [Localizer getLocalizedText:@"SIGN_IN_EMAIL"];
    [self prepareLabel:header withTitle:headerNotes];
    
    UIView *headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 250, 65)];
    [headerView addSubview:header];
    
    return headerView;
}

- (UIView*) makeFooterView
{

    btnSubmit = [UIButton buttonWithType:UIButtonTypeCustom];
    btnSubmit.frame = CGRectMake(0, 0, 302, 45);
    btnSubmit.layer.cornerRadius = 3.0f;

    [btnSubmit setTitle:[Localizer getLocalizedText:@"Send My Sign In Details"]  forState:UIControlStateNormal];
    btnSubmit.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    btnSubmit.titleLabel.font = [UIFont systemFontOfSize:14.0];

    //0.0 0.663 0.949 1.0 BrightBlueConcur
    btnSubmit.backgroundColor = [UIColor concurBrightBlueColor];
    btnSubmit.frame = CGRectMake(9, 15, btnSubmit.frame.size.width, btnSubmit.frame.size.height);
    [btnSubmit setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    btnSubmit.titleLabel.font = [UIFont fontWithName:@"Helvetica Neue" size:18.0];
    [btnSubmit addTarget:self action:@selector(btnSubmitPressed:) forControlEvents:UIControlEventTouchUpInside];
    
    UIView *footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 310, 75)];
    [footerView addSubview:btnSubmit];
    return footerView;
}

- (void)prepareLabel:(UILabel *)lbl withTitle:(NSString *)txt
{
    [lbl setText:txt];
    [lbl setTextColor:[UIColor whiteColor]];
    [lbl setFont:[UIFont fontWithName:@"Helvetica Neue" size:18.0]];
    [lbl setTextAlignment:NSTextAlignmentCenter];
    [lbl setBackgroundColor:[UIColor clearColor]];
    [lbl setHighlightedTextColor:[UIColor whiteColor]];
    [lbl setAdjustsFontSizeToFitWidth:YES];
    [lbl setNumberOfLines:0];
    [lbl setMinimumScaleFactor:10.0/[lbl.font pointSize]];
}

#pragma mark - UITableViewDelegate Methods

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
// No op ..
}

#pragma mark - EditInlineCellDelegate Methods

- (BOOL)textFieldShouldReturn:(UITextField *)doneButtonPressed
{
	return YES;
}

- (IBAction)textFieldDoneEditing:(id)sender
{//clears the keyboard from the view
	[sender resignFirstResponder];
}

#pragma mark - Text Field Methods


-(void) resignFirstResponder:(int) row
{
    NSUInteger path[1] = {row};
	NSIndexPath *ip = [[NSIndexPath alloc] initWithIndexes:path length:1];
    EditInlineCell *cell = (EditInlineCell*) [tableList cellForRowAtIndexPath:ip];
    [cell resignFirstResponder];
}

#pragma mark - Button Methods
- (IBAction)btnSubmitPressed:(id)sender
{
    if (![ExSystem connectedToNetwork])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"No Network Connection"]
                              message:[Localizer getLocalizedText:@"Connect to a WiFi or cellular data network."]
                              delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles:nil];
        [alert show];
    }
    
    else if (![emailLoginID lengthIgnoreWhitespace])
    {

        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Please Enter Your Email Address"]
                              message:nil
                              delegate:self
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        
        [alert show];
    }
    else
    {
        NSMutableDictionary *pBag = nil;
        pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:emailLoginID, @"RESET_PIN_EMAIL", @"YES", @"SKIP_CACHE", nil];
        [[ExSystem sharedInstance].msgControl createMsg:RESET_PIN_USER_EMAIL_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
        [self showWaitViewWithText:[Localizer getLocalizedText: @"Waiting"]];
    }
}

- (IBAction)btnMoreHelpPressed:(id)sender;
{
    LoginHelpTopicVC *topicVC = [[LoginHelpTopicVC alloc] initWithNibName:@"LoginHelpTopicVC" bundle:nil];

    if (topics != nil)
    {
        topicVC.topic = topics[0];
        [self.navigationController pushViewController:topicVC animated:YES];
    }
}

#pragma mark - EditInlineCellDelegate Methods
- (BOOL)cellTextFieldShouldReturn:(EditInlineCell *)cell
{
    [self btnSubmitPressed:nil];
    [cell.txt resignFirstResponder];

    return YES;
}

-(IBAction) cellTextEdited:(EditInlineCell*)cell
{
    if( cell.rowPos == 0)
        emailLoginID = cell.txt.text;
}

-(void) cellScrollMeUp:(EditInlineCell*)cell
{    
    self.tableList.contentInset =  UIEdgeInsetsMake(0, 0, 130, 0);
    [self.tableList scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:cell.rowPos inSection:0] atScrollPosition:UITableViewScrollPositionTop animated:YES];
}

#pragma mark -
#pragma mark MVC Methods
-(void)respondToFoundData:(Msg *)msg
{
    [super respondToFoundData:msg];
    
	[self hideWaitView];
    [self hideLoadingView];
    
    if([msg.idKey isEqualToString:RESET_PIN_USER_EMAIL_DATA])
    {
        ResetPinUserEmailData *emailData = (ResetPinUserEmailData*)msg.responder;
        NSString* errMsg = msg.errBody;
        NSString *successMsg = nil;
        NSString *bodyMsg = nil;

        if (errMsg == nil && [emailData.status isEqualToString:@"SUCCESS"])
        {
            // save the email and clientKey to the keychain
            KeychainManager *keychainManager = [[KeychainManager alloc] init];
            
            if ([emailData.clientGUID lengthIgnoreWhitespace]) {
                [keychainManager savePinResetClientToken:emailData.clientGUID];
            }

            // string must be url encoded 
            NSString* urlEncodedEmail = [emailLoginID stringByReplacingOccurrencesOfString:@"@" withString:@"%40"];
            [keychainManager savePinResetEmailToken:urlEncodedEmail];

            successMsg = [Localizer getLocalizedText:@"Sign In Instructions Sent to This Device"];
            bodyMsg = [Localizer getLocalizedText:@"SIGN_IN_INSTRUCT_MSG"];
        }

        else if (errMsg == nil && msg.responder != nil)
        {
            if (emailData.actionStatus != nil && emailData.actionStatus.errMsg != nil)
            {
                errMsg = emailData.actionStatus.errMsg;
            }
            else if (emailData.errMsg != nil)
            {    
                errMsg = emailData.errMsg;
            }
        }
        
        NSString *userMsg = nil;
        if ([successMsg lengthIgnoreWhitespace])
            userMsg = successMsg;
        else
        {
            if ([errMsg rangeOfString:@"is not in the form required for an e-mail address"].location == NSNotFound)
                userMsg = errMsg;
            else
                userMsg = [Localizer getLocalizedText:@"Invalid email address"];
        }
        if ([userMsg lengthIgnoreWhitespace])
        {
            UIAlertView *alert = [[MobileAlertView alloc]
                                  initWithTitle:userMsg
                                  message:bodyMsg
                                  delegate:self
                                  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                                  otherButtonTitles:nil];
             [alert show];
        }
    }
    
}

@end
