//
//  TripItLinkVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "TripItLinkVC.h"
#import "TripItLink.h"

@implementation TripItLinkVC
@synthesize tableList, btnGreen, viewHeader, lblHeader, email, pwd;

#pragma mark -
#pragma mark MobileViewController Methods
-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:TRIPIT_LINK])
    {
        /*
         <Status xmlns="http://schemas.datacontract.org/2004/07/Snowbird">SUCCESS</Status>
         <IsEmailAddressConfirmed>false</IsEmailAddressConfirmed>
         <IsNewAccount>true</IsNewAccount>
         */
        [self hideWaitView];
        NSString *responseStatus = nil;//[responder.dictStuff objectForKey:@"Status"];
        NSString *errorMessage = nil;//[responder.dictStuff objectForKey:@"ErrorMessage"];

        if([responseStatus isEqualToString:@"SUCCESS"])
        {
            [ExSystem sharedInstance].isTripItEmailAddressConfirmed = YES;
            [ExSystem sharedInstance].isTripItLinked = YES;
            [[ExSystem sharedInstance] saveSettings];
            [self.navigationController popViewControllerAnimated:YES];
        }
        else
        {
            MobileAlertView *av = [[MobileAlertView alloc] initWithTitle:[Localizer getLocalizedText:@"Failure"] message:errorMessage delegate:nil cancelButtonTitle:[Localizer getLocalizedText:@"OK"] otherButtonTitles: nil];
            [av show];
        }
    }
}


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

-(NSString *)getViewIDKey
{
	return @"TRIPITLINK";
}

-(NSString *)getViewDisplayType
{
	return @"VIEW_DISPLAY_TYPE_MODAL_NAVI";
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
    
    if(email != nil && [email length] > 0)
        [self markFirstResponder:1];
    else 
        [self markFirstResponder:0];
}



- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.title = [Localizer getLocalizedText:@"Link to TripIt"];
    
    self.lblHeader.text = [Localizer getLocalizedText:@"Link with TripIt Header"];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.btnGreen = nil;
    self.tableList = nil;
    self.viewHeader = nil;
    self.lblHeader = nil;
}

#pragma mark -
#pragma mark Table View Data Source Methods
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
    //NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
    
    EditInlineCell *cell = (EditInlineCell *)[tableView dequeueReusableCellWithIdentifier: @"EditInlineCell"];
    if (cell == nil)  
    {
        NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"EditInlineCell" owner:self options:nil];
        for (id oneObject in nib)
            if ([oneObject isKindOfClass:[EditInlineCell class]])
                cell = (EditInlineCell *)oneObject;
    }
    
    NSString *userPlaceholder = [Localizer getLocalizedText:@"LABEL_LOGIN_USER_NAME"];
	NSString *pwdPlaceholder = [Localizer getLocalizedText:@"LABEL_LOGIN_PASSWORD"];

    cell.parentVC = self;

    if(row == 0)
    {
        if ([ExSystem sharedInstance].entitySettings.saveUserName != nil &&
            [[ExSystem sharedInstance].entitySettings.saveUserName isEqualToString:@"YES"] &&
            [ExSystem sharedInstance].userName != nil &&
            [ExSystem sharedInstance].userName.length > 0)
        {
            cell.txt.text = [ExSystem sharedInstance].sys.fbEmail;
        }
        else
        {
            cell.txt.text = @"";
        }
        self.email = cell.txt.text;
        cell.txt.placeholder = userPlaceholder;
        
        cell.txt.secureTextEntry = NO;
        cell.txt.keyboardType = UIKeyboardTypeEmailAddress;
        cell.txt.returnKeyType = UIReturnKeyNext;
    }
    else
    {
        cell.txt.text = @"";
        self.pwd = @"";
        cell.txt.placeholder = pwdPlaceholder;
        cell.txt.secureTextEntry = YES;
        cell.txt.keyboardType = UIKeyboardTypeDefault;
        cell.txt.returnKeyType = UIReturnKeyGo;
        
        cell.txt.placeholder = pwdPlaceholder;
    }
    return cell;
    
}



#pragma mark -
#pragma mark Table View Delegate Methods
- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section 
{
    return [Localizer getLocalizedText:@"Link with TripIt Header"];
}

-(CGFloat) tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section
{
    return 150;
}

-(UIView*) tableView:(UITableView *)tableView viewForFooterInSection:(NSInteger)section
{
    if(section == 0)
    {
        float maxW = 320;
        float x = 20;
        if([UIDevice isPad])
        {
            maxW = 320;
            //            x = 130;
            if([ExSystem isLandscape])
            {
                x = (maxW - 280) / 2;
                //maxW = 1024;
            }
            else
            {
                x = (maxW - 280) / 2;
                //maxW = 768;
            }
        }
        
        float y = 15;
        if([UIDevice isPad])
            y = 10;
        __autoreleasing UIView *v = [[UIView alloc] initWithFrame:CGRectMake(0, 0, maxW, 150)];

        self.btnGreen = [ExSystem makeColoredButtonRegular:@"GREEN_BIG" W:280 H:47 Text:[Localizer getLocalizedText:@"Link with TripIt"] SelectorString:@"buttonLinkPressed:" MobileVC:self];
        if([UIDevice isPad])
            btnGreen.frame = CGRectMake(x, y, 280, 40);
        else
            btnGreen.frame = CGRectMake(x, y, 280, 47);
        [btnGreen addTarget:self action:@selector(buttonLinkPressed:) forControlEvents:UIControlEventTouchUpInside];

        btnGreen.titleLabel.font = [UIFont boldSystemFontOfSize:17.0];
        
        [v addSubview:btnGreen];
        
        return v;
    }
    else
        return nil;
}


-(CGFloat) tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 50;
}

-(UIView*) tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
    if(section == 0)
    {
        return viewHeader;
    }
    else
        return nil;
}


- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 50;	
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath 
{
	
    
}


#pragma mark - TripIt Link Button
-(IBAction)buttonLinkPressed:(id)sender
{
    [self showWaitView];
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:@"TRIPITLINK", @"TO_VIEW"
                                 , @"YES", @"SKIP_CACHE", email, @"EMAIL", pwd, @"PWD", nil];
    [[ExSystem sharedInstance].msgControl createMsg:TRIPIT_LINK CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
}


#pragma mark -
#pragma mark Text Field Methods
- (BOOL)textFieldShouldReturn:(UITextField *)doneButtonPressed 
{//hitting enter or go in the keyboard acts as though you have pressed the sign in button    
	return YES;
}


- (IBAction)textFieldDoneEditing:(id)sender 
{//clears the keyboard from the view
	[sender resignFirstResponder];
}
@end
