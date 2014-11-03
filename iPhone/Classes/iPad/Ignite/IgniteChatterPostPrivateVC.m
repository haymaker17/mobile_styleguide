//
//  IgniteChatterPostPrivateVC.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteChatterPostPrivateVC.h"
#import "IgniteChatterPostData.h"
#import "SalesForceUserManager.h"
#import "EntitySalesForceUser.h"
#import "EntityChatterFeedEntry.h"
#import "EntityChatterAuthor.h"
#import "EntitySalesForceTrip.h"
#import "IgniteSalesforceTripData.h"
#import "IgniteUserSearchData.h"
#import "IgniteUserSearchResult.h"
#import "IgniteUserPickerVC.h"
#import "SalesForceTripManager.h"
#import "ImageUtil.h"

@interface IgniteChatterPostPrivateVC (private)
-(void) configureNavBar;

-(void) buttonCancelPressed;
-(void) buttonSharePressed;

-(void) showPicker;
-(void) dismissPicker;

-(void) addRecipient:(IgniteUserSearchResult*)searchResult;
-(CGFloat) calculateXCoordForNextRecipientButton;

-(void) postToChatter;

-(void) didPretendToFinishPost;
-(void) didFinishPost;

-(void) showErrorAlert;
@end


@implementation IgniteChatterPostPrivateVC

@synthesize navBar, vwScroll, txtRecipients, txtComment, recipientsArray, vcPopover;
@synthesize delegate = _delegate;

#pragma mark - Lifecycle
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        self.recipientsArray = [[NSMutableArray alloc] initWithObjects:nil];
    }
    return self;
}


- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self configureNavBar];
    
    [self.txtRecipients becomeFirstResponder];
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
    label.text = @"Private Message";		
    navItem.titleView = label;
    
    UIBarButtonItem* btnCancel = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Cancel" SelectorString:@"buttonCancelPressed" MobileVC:self];
    UIBarButtonItem* btnShare = [ExSystem makeColoredButton:@"IGNITE_BLUE" W:74 H:31 Text:(NSString *)@"Share" SelectorString:@"buttonSharePressed" MobileVC:self];
    
	[navItem setLeftBarButtonItem:btnCancel animated:NO];
	[navItem setRightBarButtonItem:btnShare animated:NO];
    
    [self.navBar pushNavigationItem:navItem animated:YES];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
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

- (void)buttonSharePressed
{
    NSString *text = self.txtComment.text;
    if (text == nil || text.length == 0)
    {
        UIAlertView *alert = [[MobileAlertView alloc] 
							  initWithTitle:@"Text Required" // TODO: localize
							  message:@"Please type a comment first." // TODO: localize
							  delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
		[alert show];
        
        [self.txtComment becomeFirstResponder];
    }
    else
    {
        [self postToChatter];
    }
}

#pragma mark - Missing data handlers
-(void) handleMissingRecipient
{
    // If we're failing silently and they entered a text in the recipient text box,
    // then pretend that we were able to share the trip so that the demo can go forward.
    //
    if ((![[ExSystem sharedInstance] shouldSendRequestsOverNetwork] || [[ExSystem sharedInstance] shouldErrorResponsesBeHandledSilently]) &&
        txtRecipients.text != nil &&
        txtRecipients.text.length > 0)
    {
        [self didPretendToFinishPost];
    }
    else
    {
        UIAlertView *alert = [[MobileAlertView alloc] 
                              initWithTitle:@"Recipient Required" // TODO: localize
                              message:@"Please enter at least one recipient." // TODO: localize
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                              otherButtonTitles:nil];
        [alert show];
    }
}

-(void) handleMissingTrip
{
    // If we're failing silently, then just pretend to have posted the message so the demo can go foward.
    if ([[ExSystem sharedInstance] shouldErrorResponsesBeHandledSilently])
        [self didPretendToFinishPost];
    else
        [self showErrorAlert];
}

#pragma mark - Messages
-(void) postToChatter
{
    // TODO: Fix this to share the trip with more than one recipient.
    //
    IgniteUserSearchResult* recipient =  (recipientsArray.count == 0 ? nil : [recipientsArray objectAtIndex:0]);
    
    // Check for recipient
    if (recipient == nil)
    {
        [self handleMissingRecipient];
        return;
    }

    if (![[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
    {
        // We can't post the message without the network, so just pretend that we posted it.
        [self didPretendToFinishPost];
    }
    else
    {
        [self showWaitView];
        
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: self.txtComment.text, @"TEXT", recipient.identifier, @"RECIPIENT_ID", nil];
        [[ExSystem sharedInstance].msgControl createMsg:CHATTER_POST_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

#pragma mark - UITextViewDelegate methods
- (void)textViewDidChange:(UITextView *)textView
{
    // Skip user picker if we can't use the network.
    if (![[ExSystem sharedInstance] shouldSendRequestsOverNetwork])
        return;
    
    if (textView.text == nil || textView.text.length < 2)
    {
        [self dismissPicker];
        return;
    }

    if (self.vcPopover == nil)
        [self showPicker];
    
    IgniteUserPickerVC *vcPicker = (IgniteUserPickerVC*)self.vcPopover.contentViewController;
    [vcPicker searchForString:textView.text];
}

#pragma mark - Picker methods
- (void) showPicker
{
    IgniteUserPickerVC *vcPicker = [[IgniteUserPickerVC alloc] initWithNibName:@"IgniteUserPickerVC" bundle:nil];
    vcPicker.delegate = self; //IgniteUserPickerDelegate
    vcPicker.contentSizeForViewInPopover = CGSizeMake(320, 460); // size of view in popover
    vcPicker.modalInPopover = NO; // Clicks outside the popover cause it to be dismissed
    
    self.vcPopover = [[UIPopoverController alloc] initWithContentViewController:vcPicker];
    self.vcPopover.delegate = self; // UIPopoverControllerDelegate
    
    [vcPopover presentPopoverFromRect:self.txtRecipients.frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
}

-(void) dismissPicker
{
    if (self.vcPopover != nil)
    {
        [self.vcPopover dismissPopoverAnimated:NO];
        self.vcPopover = nil;
    }
}

#pragma mark - IgniteUserPickerDelegate methods
-(void) userPickedSearchResult:(IgniteUserSearchResult*)searchResult
{
    [self addRecipient:searchResult];
    
    // Now that the recipient has been added, clear the recipient text field so that a new one can be entered.
    self.txtRecipients.text = @"";
    
    [self dismissPicker];
}

-(void) addRecipient:(IgniteUserSearchResult*)searchResult
{
    // Remember this recipient so that we can share the trip with them.
    [self.recipientsArray addObject:searchResult];
    
    // Figure out where the new button should go.  Do this BEFORE adding the new button to the scroll view of the calculation will be messed up.
    CGFloat xCoord = [self calculateXCoordForNextRecipientButton];

    // Prepend @ to the name
    NSString* atName = [NSString stringWithFormat:@"@%@", searchResult.name];
    
    // Calculate the length of @name
    UIFont *font = [UIFont boldSystemFontOfSize:12.0]; // Used by ExSystem::makeColoredButtonRegular.
    CGSize atNameSize = [atName sizeWithFont: font constrainedToSize:CGSizeMake(300, 12.0) lineBreakMode:NSLineBreakByTruncatingMiddle];
    
    // Calculate the width of the button
    CGFloat extraPadding = 8.0;
    CGFloat buttonWidth = extraPadding + atNameSize.width + extraPadding;
    CGFloat buttonHeight = 28.0;
    
    // Create the button
    UIButton* button = [ExSystem makeColoredButtonRegular:@"IGNITE_BLUE" W:buttonWidth H:buttonHeight Text:atName SelectorString:nil MobileVC:nil];
    
    // Put the button in the scroll view (which only contains buttons created in this method)
    [self.vwScroll addSubview:button];
    
    // Position the button
    button.frame = CGRectMake(xCoord, 0.0, buttonWidth, buttonHeight);
}

-(CGFloat) calculateXCoordForNextRecipientButton
{
    CGFloat farthestRightEdge = 0.0;
    
    // Iterate through each subview
    for (UIView* sub in self.vwScroll.subviews)
    {
        // Find the subview's right edge
        CGFloat rightEdgeOfSubview = sub.frame.origin.x + sub.frame.size.width;
        
        // If it's the right-most edge that we have seen so far, then remember it.
        if (rightEdgeOfSubview > farthestRightEdge)
            farthestRightEdge = rightEdgeOfSubview;
    }
    
    const CGFloat spaceFromPriorButton = (farthestRightEdge > 0 ? 6.0 : 0.0);
    return (farthestRightEdge + spaceFromPriorButton);
}

#pragma mark - UIPopoverControllerDelegate methods
- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController
{
    // This method is called when the popover is dismissed by the user, not when it is programatically dismissed.
    self.vcPopover = nil;
}

#pragma mark - ExMsgRespondDelegate methods
-(void) respondToFoundData:(Msg *)msg
{
	[self hideWaitView];
    
    if ([msg.idKey isEqualToString:CHATTER_POST_DATA])
    {
        if (msg.responseCode < 200 || msg.responseCode > 300) // 201 when successful.
        {
            if ([[ExSystem sharedInstance] shouldErrorResponsesBeHandledSilently])
                [self didPretendToFinishPost]; // We can't show errors, so just pretend we succeeded.
            else
                [self showErrorAlert];
        }
        else
        {
            [self didFinishPost];
        }
    }
}

#pragma mark - Did share methods
-(void) didPretendToFinishPost
{
    [self didFinishPost];
}

-(void) didFinishPost
{
    [self.delegate didPostToChatter];
    [self.delegate closeChatterPostVC];
}

#pragma mark - Error method
-(void) showErrorAlert
{
    UIAlertView *alert = [[MobileAlertView alloc] 
                          initWithTitle:[Localizer getLocalizedText:@"Error"]
                          message:@"Your message could not be posted. Please try again later." // TODO: Localize
                          delegate:nil
                          cancelButtonTitle:[Localizer getLocalizedText:@"Close"]
                          otherButtonTitles:nil];
    [alert show];
}

@end
