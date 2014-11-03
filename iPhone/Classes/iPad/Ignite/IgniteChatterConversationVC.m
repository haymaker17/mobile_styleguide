//
//  IgniteChatterConversationVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/10/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterConversationVC.h"
#import "IgniteChatterConversationDS.h"
#import "ConcurMobileAppDelegate.h"
#import "ImageUtil.h"

@interface IgniteChatterConversationVC ()
-(void) configureNavBar;
- (void)buttonReplyPressed;
@end

@implementation IgniteChatterConversationVC

@synthesize tblConversation, dsConversation, navBar, feedEntry;
@synthesize delegate = _delegate;

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
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    
    self.dsConversation = [[IgniteChatterConversationDS alloc] init];
    [dsConversation setSeedData:[ad managedObjectContext] withTable:self.tblConversation withDelegate:self withFeedEntry:self.feedEntry];
    
    [self configureNavBar];
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
    label.text = @"Conversation";		
    navItem.titleView = label;
    
    UIBarButtonItem* btnReply = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Reply" SelectorString:@"buttonReplyPressed" MobileVC:self];
    
	[navItem setRightBarButtonItem:btnReply animated:NO];
    
    [self.navBar pushNavigationItem:navItem animated:YES];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // TODO:
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Button handlers
- (void)buttonReplyPressed
{
    [self.delegate replyToConversationForFeedEntry:self.feedEntry];
}

@end
