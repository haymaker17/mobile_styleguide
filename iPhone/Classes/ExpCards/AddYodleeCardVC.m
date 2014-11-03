//
//  AddYodleeCardVC.m
//  ConcurMobile
//
//  Created by yiwen on 11/4/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

// Uses EditFormVC xib, see SafetyCheckInVC


#import "AddYodleeCardVC.h"
#import "AddYodleeCardData.h"
#import "YodleeCardLoginFormData.h"
#import "TextEditVC.h"

#define kAlertViewComplete 200011

@interface AddYodleeCardVC (private)
-(void)initFieldsWithData:(NSArray*)newFields;
-(void) configureWaitView;

@end

@implementation AddYodleeCardVC
@synthesize btnAddAccount, provider, loadingForm, savingForm;

#pragma mark - View lifecycle
- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}



-(void) loadHeaderView
{
    float w = 320;//[self isLandscape]? 480:320;//self.view.frame.size.width;
    NSString *emptyText = [Localizer getLocalizedText:@"ADD_YODLEE_CARD_INSTRUCTION"];
    
    CGSize maxSize = CGSizeMake(w-20, 124);
    CGSize s = [emptyText sizeWithFont:[UIFont systemFontOfSize:12.0f] constrainedToSize:maxSize];
    
    CGRect lblRect =  CGRectMake(8, 5, w-12, s.height+1); // 6 px paddling
    UILabel* labelView = [[UILabel alloc] initWithFrame:lblRect];
    UIView* footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, s.height+6)];
    footerView.autoresizingMask =UIViewAutoresizingFlexibleWidth;
    footerView.backgroundColor = [UIColor clearColor];
    [labelView setText:emptyText];
    [labelView setBackgroundColor:[UIColor clearColor]];
    [labelView setTextAlignment:NSTextAlignmentCenter];
    [labelView setFont:[UIFont systemFontOfSize:12.0f]];
    [labelView setTextColor:[UIColor darkGrayColor]];
    [labelView setShadowColor:[UIColor whiteColor]]; 
    [labelView setShadowOffset:CGSizeMake(1.0f, 1.0f)];
    labelView.numberOfLines = 0;
    labelView.lineBreakMode = NSLineBreakByWordWrapping;
    labelView.autoresizingMask = UIViewAutoresizingFlexibleWidth; // Do not adjust height
    [footerView addSubview:labelView];
    self.tableList.tableHeaderView = footerView;
    
    self.btnAddAccount = [ExSystem makeColoredButtonRegular:@"GREEN_BIG" W:280 H:47 Text:[Localizer getLocalizedText:@"Add Account to Concur"] SelectorString:@"btnAddAccountClicked" MobileVC:self];
    
    btnAddAccount.autoresizingMask = UIViewAutoresizingFlexibleWidth;
    CGRect btnRect =  CGRectMake(20, 3, 280, 47);
    btnAddAccount.frame = btnRect;
	btnAddAccount.titleLabel.font = [UIFont boldSystemFontOfSize:17.0];
    
    footerView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, w, 50)];
    footerView.autoresizingMask =UIViewAutoresizingFlexibleWidth;
    footerView.backgroundColor = [UIColor clearColor];
    [footerView addSubview:btnAddAccount];
    if (loadingForm || savingForm)
    {
        [btnAddAccount setEnabled:FALSE];
    }

    [self configureWaitView];
    //[[MCLogging getInstance] log:[NSString stringWithFormat:@"Creating check in btn, lat- %@, city - %@ ", findMe.latitude, findMe.city] Level:MC_LOG_DEBU];
    
    self.tableList.tableFooterView = footerView;
    
}


-(void)viewWillAppear:(BOOL)animated 
{ 
	if(doReload)
	{
		doReload = NO;
		[tableList reloadData];
        [[MCLogging getInstance] log:@"viewWillAppear:reload table" Level:MC_LOG_DEBU];
	}
	
	[super viewWillAppear:animated]; 
}

// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];

    // Show default back button
    [self.navigationItem setHidesBackButton:NO animated:NO];

    self.title = [Localizer getLocalizedText:@"Add Credit Card"];
    
    [self loadHeaderView];
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return ADD_YODLEE_CARD;
}

-(void)btnAddAccountClicked
{
    self.savingForm = YES;
    [self configureWaitView];
    NSMutableDictionary *pBagMsg = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.provider.contentServiceId, @"ContentServiceId", self.allFields, @"Fields", nil];
	[[ExSystem sharedInstance].msgControl createMsg:ADD_YODLEE_CARD_DATA CacheOnly:@"NO" ParameterBag:pBagMsg SkipCache:YES RespondTo:self];
}

-(void)respondToFoundData:(Msg *)msg
{
        
	if([msg.idKey isEqualToString:YODLEE_CARD_LOGIN_FORM_DATA])
    {
        self.loadingForm = NO;
        [self configureWaitView];
		NSString* errMsg = msg.errBody;
		if (msg.errBody == nil && msg.responseCode != 200)
        {
            errMsg = [Localizer getLocalizedText:@"YODLEE_CARD_LOGIN_FORM_FAILED_MSG"];
        }
        
        if (errMsg != nil) 
		{
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:msg.errCode
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
        {    
            YodleeCardLoginFormData* data = (YodleeCardLoginFormData*) msg.responder;
            [self initFieldsWithData:data.fields];
            [self.tableList reloadData];
            [btnAddAccount setEnabled:YES];

        }
    }
    else if([msg.idKey isEqualToString:ADD_YODLEE_CARD_DATA])
    {
        self.savingForm = NO;
        [self configureWaitView];
		NSString* errMsg = msg.errBody;
        AddYodleeCardData* srd = (AddYodleeCardData*) msg.responder;

		if (msg.errBody == nil && msg.responseCode != 200)
        {
            errMsg = [Localizer getLocalizedText:@"ADD_YODLEE_CARD_FAILED_MSG"];
        }
		else if (errMsg == nil && msg.responder != nil)
		{
			if (srd.status != nil && ![srd.status.status isEqualToString:@"SUCCESS"])
            {
                if (srd.status.errMsg != nil)
                    errMsg = srd.status.errMsg;
                else
                    errMsg = [Localizer getLocalizedText:@"Check In Failed Message"];
            }
        }
		if (errMsg != nil) 
		{
            errMsg = [Localizer getLocalizedText:@"ADD_YODLEE_CARD_FAILED_MSG"];
            
			UIAlertView *alert = [[MobileAlertView alloc] 
								  initWithTitle:msg.errCode
								  message:errMsg
								  delegate:nil 
								  cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] 
								  otherButtonTitles:nil];
			[alert show];
		}
		else 
        {    
            //Message needs to be localized.
            //MOB-9292 detect empty srd.card object by reponse from webservices. Means same card already added, pop differnt alert window.
            if (srd.card.accountNumberLastFour == nil)
            {
                UIAlertView *sameCardAlert = [[MobileAlertView alloc] 
                                              initWithTitle:nil 
                                              message:[Localizer getLocalizedText: @"ADD_EXISTING_CARD"]
                                              delegate:self 
                                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                              otherButtonTitles:nil];
                sameCardAlert.tag = kAlertViewComplete;
                [sameCardAlert show];
            }
            else {
                
                UIAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:nil //[Localizer getLocalizedText:@"Add Card Complete"]
                                      message:[NSString stringWithFormat:[Localizer getLocalizedText:@"ADD_YODLEE_CARD_COMPLETE_MSG"], srd.card.accountNumberLastFour]
                                      delegate:self
                                      cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_OK_BTN"] 
                                      otherButtonTitles:nil];
                
                alert.tag = kAlertViewComplete;
                [alert show];
            }

            // Refresh home page
            NSObject<ExMsgRespondDelegate>* responderVC = (NSObject<ExMsgRespondDelegate>*)[ConcurMobileAppDelegate findRootViewController];
            if ([UIDevice isPad])
            {
                responderVC = (NSObject<ExMsgRespondDelegate>*)[ConcurMobileAppDelegate findiPadHomeVC];
            }
            
            NSMutableDictionary* pBag3 = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[(MobileViewController*)responderVC getViewIDKey], @"TO_VIEW", nil];
			[[ExSystem sharedInstance].msgControl createMsg:OOPES_DATA CacheOnly:@"NO" ParameterBag:pBag3 SkipCache:YES RespondTo:responderVC];

        }
    }
}



-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
	if(alertView.tag == kAlertViewComplete)
	{
        UINavigationController* nav = self.navigationController;
        if ([UIDevice isPad])
            [self dismissViewControllerAnimated:YES completion:nil];
        else
        {    
            [nav popViewControllerAnimated:NO];  // pop this view
            [nav popViewControllerAnimated:YES]; // pop search view
        }
    }
}
#pragma mark - Init data
- (void)setSeedData:(NSDictionary*)pBag
{
    self.provider = pBag[@"PROVIDER"];
    
    self.loadingForm = YES;
    NSMutableDictionary *pBagMsg = [[NSMutableDictionary alloc] initWithObjectsAndKeys:self.provider.contentServiceId, @"ContentServiceId", nil];
	[[ExSystem sharedInstance].msgControl createMsg:YODLEE_CARD_LOGIN_FORM_DATA CacheOnly:@"NO" ParameterBag:pBagMsg SkipCache:YES RespondTo:self];

}


-(void)initFieldsWithData:(NSArray*)newFields
{
    self.sectionFieldsMap = [[NSMutableDictionary alloc] init];
	self.sections = [[NSMutableArray alloc] initWithObjects:@"Data", nil];
	
	NSMutableArray *fields = [[NSMutableArray alloc] init];
	
    self.allFields = [[NSMutableArray alloc] init];
    for (FormFieldData *fld in newFields)
    {
        if (![fld.access isEqualToString:@"HD"])
        {
            [fields addObject:fld];
        }
        [self.allFields addObject:fld];
    }
    sectionFieldsMap[@"Data"] = fields;
    [super initFields];
}


-(void) configureWaitView
{	
    if ((!loadingForm && !savingForm)) {
        [self hideWaitView];
        [self hideLoadingView];
    }
    else {
        if (loadingForm)
            [self showLoadingView];
        else
            [self showWaitView];
    }
}

#pragma mark - FormViewControllerBase overrides

-(BOOL) canUseTextFieldEditor:(FormFieldData*)field
{
    if ([super canUseTextFieldEditor:field])
        return YES;
    else // Yodlee card number field datatype is LOGIN.
        return ([field.dataType isEqualToString:@"LOGIN"] && [field.ctrlType isEqualToString:@"edit"]);
}

-(void)showTextFieldEditor:(FormFieldData*) field
{
    TextEditVC *vc = [[TextEditVC alloc] initWithNibName:@"TextEditVC" bundle:nil];
    BOOL isNumeric = [field requiresNumericInput];
    NSString* val = field.fieldValue;
    
    if ([@"MONEY" isEqualToString:field.dataType] || ([@"NUMERIC" isEqualToString:field.dataType] && [@"edit" isEqualToString:field.ctrlType]))
    {
        double dblVal = 0.0;
        if ([self.helper validateDouble:val doubleValue:&dblVal] && dblVal == 0.0)
        {
            val = @"";
        }
    }
    else if ([@"INTEGER" isEqualToString:field.dataType] && [@"edit" isEqualToString:field.ctrlType] && ![@"CurrencyName" isEqualToString:field.iD])
    {
        int intVal = 0;
        if ([self.helper validateInteger:val integerValue:&intVal] && intVal == 0)
        {
            val = @"";
        }
    }
    
    [vc setSeedData:val context:field
           delegate:self
                tip:field.tip 
              title:[field getFullLabel]
             prompt:[field getFullLabel]
          isNumeric:isNumeric
         isPassword:[field needsSecureEntry]
                err:field.validationErrMsg];
    
    [self.navigationController pushViewController:vc animated:YES];
    
}

-(void)updateSaveBtn
{
    self.navigationItem.rightBarButtonItem = nil;
    self.isDirty = FALSE;
}

-(void)setupFakeBackButton
{
// Override base class function, so it dose not add customized back button.
}
@end
