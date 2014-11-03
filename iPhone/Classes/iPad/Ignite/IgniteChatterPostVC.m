//
//  IgniteChatterPostVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/3/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//
//
//  IgniteChatterReplyVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterPostVC.h"
#import "IgniteChatterPostData.h"
#import "SalesForceUserManager.h"
#import "EntitySalesForceUser.h"
#import "EntityChatterFeedEntry.h"
#import "EntityChatterAuthor.h"
#import "ImageUtil.h"

@interface IgniteChatterPostVC (private)
-(void) configureNavBar;
-(void) configureImage;

-(void) buttonCancelPressed;
-(void) buttonSharePressed;

-(void) didPretendToFinishPost;
-(void) didFinishPost;
@end

@implementation IgniteChatterPostVC

@synthesize navBar, imgView, txtView, feedEntry;
@synthesize delegate = _delegate;

#pragma mark - Lifecycle
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
    
    [self configureNavBar];
    [self configureImage];
    
    [self.txtView becomeFirstResponder];
}

-(void) configureNavBar
{
    // Show custom nav bar
    UIImage *imgNavBar = [ImageUtil getImageByName:@"bar_title_landscape"];
    self.navBar.tintColor = [UIColor clearColor];
    [self.navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsDefault];
    [self.navBar setBackgroundImage:imgNavBar forBarMetrics:UIBarMetricsLandscapePhone];

    UINavigationItem *navItem = [UINavigationItem alloc];
    
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 360, 44)];
    label.backgroundColor = [UIColor clearColor];
    label.font = [UIFont boldSystemFontOfSize:16.0];
    label.shadowColor = [UIColor colorWithWhite:0.0 alpha:0.5];
    label.textAlignment = NSTextAlignmentCenter;
    label.textColor =[UIColor whiteColor];
    label.text = (feedEntry != nil ? [NSString stringWithFormat:@"Reply to %@", feedEntry.author.name] : @"What are you working on?");		
    navItem.titleView = label;
    
    UIBarButtonItem* btnCancel = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Cancel" SelectorString:@"buttonCancelPressed" MobileVC:self];
    UIBarButtonItem* btnShare = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Share" SelectorString:@"buttonSharePressed" MobileVC:self];
    
	[navItem setLeftBarButtonItem:btnCancel animated:NO];
	[navItem setRightBarButtonItem:btnShare animated:NO];
    
    [self.navBar pushNavigationItem:navItem animated:YES];
}

-(void) configureImage
{
    EntitySalesForceUser *user = [[SalesForceUserManager sharedInstance] fetchUser];
    if (user != nil && [user.smallPhotoUrl length])
    {
        UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
        NSString *imageCacheName = [NSString stringWithFormat:@"User_%@_Photo_Small", user.identifier];
        [[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:user.smallPhotoUrl RespondToImage:img IV:self.imgView MVC:nil ImageCacheName:imageCacheName OAuth2AccessToken:[[SalesForceUserManager sharedInstance] getAccessToken]];
    }
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    //UIView *v = self.view.superview;
    //v = v.superview;
    //[[v.layer.sublayers objectAtIndex:0] removeFromSuperlayer];
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    
    // TODO
    
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if (interfaceOrientation == UIInterfaceOrientationLandscapeRight || interfaceOrientation == UIInterfaceOrientationLandscapeLeft)
        return YES;
    return NO;
}

#pragma mark - Button handlers
- (void)buttonCancelPressed
{
    [self.delegate closeChatterPostVC];
}

- (void)sendChatterPost:(NSString*) text
{
    NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: text, @"TEXT", nil];
    
    if (self.feedEntry != nil)
        [pBag setObject:self.feedEntry.identifier forKey:@"FEED_ENTRY_IDENTIFIER"];
    
    [[ExSystem sharedInstance].msgControl createMsg:CHATTER_POST_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];

}

- (void)buttonSharePressed
{
    NSString *text = self.txtView.text;
    if (text == nil || text.length == 0)
    {
        UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:@"Text Required" // TODO: localize
							  message:@"Please type the message you would like to share" // TODO: localize
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
        
        [self.txtView becomeFirstResponder];
    }
    else
    {
        if ([[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
        {
            [self showWaitView];
            [self sendChatterPost:text];
        }
        else
        {
             [self didPretendToFinishPost];
        }
    }
}

#pragma mark - Finish methods
-(void) didPretendToFinishPost
{
    [self didFinishPost];
}

-(void) didFinishPost
{
    [self.delegate didPostToChatter];
    [self.delegate closeChatterPostVC];
}

#pragma mark - ExMsgRespondDelegate methods
-(void) respondToFoundData:(Msg *)msg
{
	[self hideWaitView];
    
    if ([msg.idKey isEqualToString:CHATTER_POST_DATA])
    {        
        if (msg.responseCode == 201) // Response code 201 = Success
        {
            [self didFinishPost];
        }
        else
        {
            if ([[ExSystem sharedInstance] shouldErrorResponsesBeHandledSilently])
            {
                // We're not displaying errors so just pretend that it worked, otherwise will get stuck on this view.
                [self didPretendToFinishPost];
            }
            else
            {
                UIAlertView *alert = [[MobileAlertView alloc] 
                                      initWithTitle:[Localizer getLocalizedText:@"Error"]
                                      message:@"Your message could not be posted. Please try again later." // TODO: Localize
                                      delegate:nil
                                      cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                                      otherButtonTitles:nil];
                [alert show];
            }
        }
    }
}

@end
