//
//  YodleeCardAgreementVC.m
//  ConcurMobile
//
//  Created by yiwen on 11/28/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "YodleeCardAgreementVC.h"
#import "SearchYodleeCardsVC.h"

@implementation YodleeCardAgreementVC
@synthesize webView;


- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle
- (IBAction) close:(id) sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction) accept:(id) sender
{
    UINavigationController* nav = self.navigationController;
    
    [self.navigationController popViewControllerAnimated:NO];

    SearchYodleeCardsVC* vc = [[SearchYodleeCardsVC alloc] initWithNibName:@"SearchYodleeCardsVC" bundle:nil];
    [nav pushViewController:vc animated:YES];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.title = [Localizer getLocalizedText:@"Add Credit Card"];
    // Do any additional setup after loading the view from its nib.
    NSString *text4Btn = [Localizer getLocalizedText:@"Accept"];
    
	UIFont* sysFont13B = [UIFont boldSystemFontOfSize:13]; 
	CGSize s = [text4Btn sizeWithFont:sysFont13B];
	
	const int kButtonA2RW_Max = 80;
	const int kButtonA2RW_Min = 40;
	const int kButtonA2RH = 30;
	int size = (s.width > kButtonA2RW_Max) ? kButtonA2RW_Max : ((s.width < kButtonA2RW_Min)?kButtonA2RW_Min:s.width);
	size += 10;
    
    UIBarButtonItem *btnAccept = nil;
    if ([ExSystem is7Plus]) {
        btnAccept = [[UIBarButtonItem alloc] initWithTitle:text4Btn style:UIBarButtonItemStyleDone target:self action:@selector(accept:)];
    }
    else
        btnAccept = [ExSystem makeColoredButton:@"BLUE" W:size H:kButtonA2RH Text:text4Btn SelectorString:@"accept:" MobileVC:self];

    self.navigationItem.rightBarButtonItem = btnAccept;
    
    if ([UIDevice isPad])
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(close:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }
    
    NSURL *thisUrl = [NSURL URLWithString:@"https://www.concursolutions.com/Expense/Proxy/AmexDataSharingAgreementGetter.asp?fn=CardDataAccessUserAgreement"];;
	NSURLRequest *requestObj = [NSURLRequest requestWithURL:thisUrl];
	webView.delegate = self;
    [webView loadRequest:requestObj];

}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.webView = nil;
}


@end
