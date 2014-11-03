//
//  ChatterTripPostViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 6/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>
#import "ChatterTripPostViewController.h"
#import "IgniteChatterPostData.h"
#import "ChatterPostLookup.h"
#import "ChatterFeedViewController.h"
#import "ConcurJSONUtility.h"
#import "SalesForceUserManager.h"

@interface ChatterTripPostViewController ()
@property (nonatomic, readwrite, strong) IBOutlet UILabel *helpText;
@property (nonatomic, readwrite, strong) IBOutlet UITextView *textView;
@property (nonatomic, readwrite, strong) IBOutlet UIImageView *portrait;

@property (nonatomic, readwrite, copy) NSString *recordLocator;
@property (nonatomic, readwrite, copy) NSString *tripDescription;
@property (nonatomic, readwrite) BOOL hasPosted;
@end

@implementation ChatterTripPostViewController

- (id)initWithTripDescription:(NSString *)tripDescription recordLocator:(NSString *)recordLocator
{
    self = [super initWithNibName:@"ChatterTripPostViewController" bundle:nil];
    if (self) {
        self.title = [Localizer getLocalizedText:@"Post Trip"];
        self.tripDescription = tripDescription;
        self.recordLocator = recordLocator;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.

    // make a border around the textbox.  This is non-standard and I have to reach into some quartz stuff to do this.
    self.textView.layer.borderWidth = 1.0f;
    self.textView.layer.borderColor = [[UIColor lightGrayColor] CGColor];

    // make text box active to start
    [self.textView becomeFirstResponder];
    [self.textView setText:self.tripDescription];
    [self.helpText setText:[Localizer getLocalizedText:@"Chatter post help text"]];

    [[SalesForceUserManager sharedInstance] getPortraitForImageView:self.portrait];
    [self replaceBackButtonWithClose];
    [self createPostButton];
    [self setupBars];

    self.hasPosted = NO;
}

// configures the navigation bar and tool bar programmatically.  I would have preferred to just use the default iOS settings...
- (void)setupBars
{
    self.navigationController.toolbarHidden = NO;

    self.navigationController.navigationBarHidden = NO;
}

// on iPad, we close the view instead of going back to the menu
- (void)replaceBackButtonWithClose
{
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeMe)];
    self.navigationItem.leftBarButtonItem = btnClose;
}

- (void)createPostButton
{
    UIBarButtonItem *postButton = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Post Trip"] style:UIBarButtonItemStyleBordered target:self action:@selector(postTrip)];
    self.navigationItem.rightBarButtonItem = postButton;
}

// closes this ViewController.  Also closes any popovers that may be open.
- (void)closeMe
{
    if (self.pickerPopOver != nil && pickerPopOver.popoverVisible)
        [pickerPopOver dismissPopoverAnimated:YES];

    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)postTrip
{
    if (!self.hasPosted) {
        self.hasPosted = YES;
        NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys: [ConcurJSONUtility escapeJson:self.textView.text], @"TEXT", nil];
        [[ExSystem sharedInstance].msgControl createMsg:CHATTER_POST_DATA CacheOnly:@"NO" ParameterBag:pBag SkipCache:YES RespondTo:self];
    }
}

-(void) didProcessMessage:(Msg *)msg
{
    [self respondToFoundData:msg];
}

-(void)respondToFoundData:(Msg *)msg
{
    if ([msg.idKey isEqualToString:CHATTER_POST_DATA])
    {
        if (msg.responseCode == 201) // Response code 201 = Success
        {
            // associate trip with the post id.
            IgniteChatterPostData *postData = (IgniteChatterPostData *)msg.responder;
            ChatterPostLookup *lookup = [[ChatterPostLookup alloc] init];
            [lookup associateTrip:self.recordLocator withPost:postData.postId];

            // we're no longer showing the feed in our app.
            // I'm keeping this code around since the chatter feature is in a state of flux.
            // if we eventually decide to never handle this stuff on our side, we can remove it and any associated classes.
            // open up the feed with the id
            //ChatterFeedViewController *view = [[ChatterFeedViewController alloc] initWithItemId:postData.postId];
            //[self.navigationController pushViewController:view animated:YES];

            [self closeMe];
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

@end
