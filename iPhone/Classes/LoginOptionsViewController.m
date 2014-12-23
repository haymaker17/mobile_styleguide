//
//  LoginOptionsViewController.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "LoginOptionsViewController.h"
#import "LoginWebViewController.h"
#import "CorpSSOQueryData.h"
#import "WaitViewController.h"

@implementation LoginOptionsViewController
@synthesize txtCompanyCode, tableList, headerView, loginDelegate;

-(NSString *)getViewIDKey
{
	return LOGIN_OPTIONS_VIEW;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle
-(void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:NO];
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    self.view.backgroundColor = [UIColor concurBlueColor];
    self.tableList.backgroundColor = [UIColor clearColor];
    
    if ([Config isGov])
        self.title = [@"Agency Sign In" localize];
    else
        self.title = [@"Company Code Sign In" localize];
    
    [self makeHeaderView];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    self.tableList = nil;
}

- (void)viewWillLayoutSubviews
{
    [self makeHeaderView];
    [tableList reloadData];
    [super viewWillLayoutSubviews];
}

-(void) respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:CORP_SSO_QUERY_DATA]) 
    {
        CorpSSOQueryData *response = (CorpSSOQueryData *)msg.responder;
        
        if ([self isViewLoaded])
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
        
        if (msg.errBody == nil && response != nil) 
        {
            if (response.ssoUrl != nil && response.status != nil && [response.status isEqualToString:@"OK"]) 
            {
                NSString *ssoUrl = (NSString *)response.ssoUrl;
				// MOB-10893 : Save company code if success
                NSString *cCode = (NSString *)response.companyCode;
                [[ExSystem sharedInstance] saveCompanyCode:cCode];
                [[ExSystem sharedInstance] saveCompanySSOLoginPageUrl:response.ssoUrl];

                // MOB-13261 Update base server url
                if ([response.serverUrl length])
                {
                    [ExSystem sharedInstance].entitySettings.uri = response.serverUrl;
                    [[ExSystem sharedInstance] saveSettings];
                }
                
                //
                // Hack for Login with Safari.
                if([[ExSystem sharedInstance] isLoginFromSafari]){
                    // Open Safari
                    NSURL *url = [NSURL URLWithString:response.ssoUrl];
                    [[UIApplication sharedApplication] openURL:url];
                }
                else{
                    LoginWebViewController *lWVC = [[LoginWebViewController alloc] init];
                    [lWVC setLoginUrl:ssoUrl];
                    lWVC.loginDelegate = loginDelegate;
                    [self.navigationController pushViewController:lWVC animated:YES];
                }
            }
            else 
            {
                if (response.isSSOEnabled ==  NO && [response.status isEqualToString:@"OK"]) // SSO disabled
                {
                    [ExSystem sharedInstance].isCorpSSOUser = NO;
                    MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"COMPANY_SSO_DISABLED_MSG" localize] delegate:self cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles: nil];
                    [alert show];
                }
                else if (response.status != nil && [response.status isEqualToString:@"FAIL"]) // Invalid company code
                {
                    NSString *alertTitle = nil;
                    NSString *alertMessage = nil;
                    if ([Config isGov])
                    {
                        alertTitle = [@"Wrong agency code" localize];
                        alertMessage = [@"ERROR_INVALID_AGENCY_CODE" localize];
                    }
                    else{
                        alertTitle = [@"Wrong company code" localize];
                        alertMessage = [@"ERROR_INVALID_COMPANY_CODE" localize];
                    }

                    MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:alertTitle message:alertMessage delegate:self cancelButtonTitle:[@"LABEL_CLOSE_BTN" localize] otherButtonTitles:nil];
                    [alert show];
                }        
            }
        }
    }
}

#pragma mark View methods
- (void)prepareLabel:(UILabel *)lbl withTitle:(NSString *)txt
{
    [lbl setText:txt];
    [lbl setTextColor:[UIColor whiteColor]];
    [lbl setFont:[UIFont fontWithName:@"Helvetica Neue" size:18.0]];
    [lbl setTextAlignment:NSTextAlignmentCenter];
    [lbl setBackgroundColor:[UIColor clearColor]];
    [lbl setHighlightedTextColor:[UIColor whiteColor]];
//    [lbl setShadowColor:[UIColor whiteColor]];
//    [lbl setShadowOffset:CGSizeMake(0, 1)];
    [lbl setAdjustsFontSizeToFitWidth:YES];
    [lbl setNumberOfLines:0];
    lbl.minimumScaleFactor = 10.0/[lbl.font pointSize];
}

-(void) makeHeaderView
{
    int maxW = 260;
    self.xOffset = 30;
    
     
    self.headerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 250, 90)];
    
    NSString *txt;
    if ([Config isGov])
        txt = [@"CORP_SSO_MSG_GOV" localize];
    else
        txt = [@"CORP_SSO_MSG" localize];
    
    UILabel *lblSSOLogin = [[UILabel alloc] initWithFrame:CGRectMake(self.xOffset, 10, maxW, 70)];
    
    [self prepareLabel:lblSSOLogin withTitle:txt];
    
//    [headerView addSubview:lblResetPIN];
    [headerView addSubview:lblSSOLogin];
}


- (UIView*) makeFooterView
{
    
    UIButton *btnSubmit = [UIButton buttonWithType:UIButtonTypeCustom];
    btnSubmit.frame = CGRectMake(0, 0, 302, 45);
    btnSubmit.layer.cornerRadius = 3.0f;
    // UIColor *textColor = [UIColor whiteColor];
    [btnSubmit setTitle:[Localizer getLocalizedText:@"Proceed to Sign In"]  forState:UIControlStateNormal];
    btnSubmit.contentHorizontalAlignment = UIControlContentHorizontalAlignmentCenter;
    btnSubmit.titleLabel.font = [UIFont systemFontOfSize:14.0];
    
    
    //0.0 0.663 0.949 1.0 BrightBlueConcur
    btnSubmit.backgroundColor = [UIColor concurBrightBlueColor];
    btnSubmit.frame = CGRectMake(9, 15, btnSubmit.frame.size.width, btnSubmit.frame.size.height);
    [btnSubmit setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    btnSubmit.titleLabel.font = [UIFont fontWithName:@"Helvetica Neue" size:18.0];
    [btnSubmit addTarget:self action:@selector(buttonSSOLoginPressed:) forControlEvents:UIControlEventTouchUpInside];
    
    UIView *footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 310, 75)];
    [footerView addSubview:btnSubmit];
    return footerView;
}


#pragma mark Actions
-(void)buttonSSOLoginPressed:(id)sender
{
    if (![txtCompanyCode lengthIgnoreWhitespace])
    {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:[@"ERROR" localize] message:[@"ENTER_VALID_COMPANY_CODE" localize] delegate:self cancelButtonTitle:[@"LABEL_OK_BTN" localize] otherButtonTitles:nil];
        [alert show];
    }
    else
    {
        [WaitViewController showWithText:[@"SSO_FETCHING_DATA" localize] animated:YES ];
         NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:LOGIN_OPTIONS_VIEW, @"TO_VIEW", txtCompanyCode, @"COMPANY_CODE", nil];
         
         [[ExSystem sharedInstance].msgControl createMsg:CORP_SSO_QUERY_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

#pragma mark -
#pragma mark Table View Data Source Methods
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 1;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath

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
    
    if ([Config isGov])
        cell.txt.placeholder = [@"Enter your Agency Code" localize];
    else
        cell.txt.placeholder = [@"Company Code" localize];

    // MOB-10893 - Load from cache if present
    NSString *cachedcCode = [[ExSystem sharedInstance] loadCompanyCode];
    if ( cachedcCode != nil && ! [cachedcCode isEqualToString:@""] )
    {
        cell.txt.text = cachedcCode ;
        txtCompanyCode = cachedcCode;
    }
    
    cell.txt.secureTextEntry = NO;
    cell.txt.keyboardType = UIKeyboardTypeDefault;
    cell.txt.autocorrectionType = UITextAutocorrectionTypeNo;
    cell.txt.returnKeyType = UIReturnKeyGo;
    [cell setSelectionStyle:UITableViewCellSelectionStyleNone];
    
    return cell;
}

#pragma mark -
#pragma mark Table View Delegate Methods
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 45;
}

-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    NSLog(@" heigh: %f", self.headerView.frame.size.height);
    return self.headerView.frame.size.height;
}

-(CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    return 100;
}

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    return headerView;
}

-(UIView*) tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{

    return [self makeFooterView];
}

-(IBAction) cellTextEdited:(EditInlineCell*)sender
{
    self.txtCompanyCode = sender.txt.text;
}

- (BOOL)cellTextFieldShouldReturn:(EditInlineCell *)cell
{
    return YES;
}

-(void) cellScrollMeUp:(EditInlineCell*)sender
{
    
}


@end
