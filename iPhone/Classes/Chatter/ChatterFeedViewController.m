//
//  ChatterViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 6/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChatterFeedViewController.h"
#import "ChatterFeedView.h"
#import "AppsUtil.h"

@interface ChatterFeedViewController ()
@property (nonatomic, readwrite, strong) IBOutlet ChatterFeedView *chatterFeedView;
@property (nonatomic, readwrite, copy) NSString *itemId;
@end

@implementation ChatterFeedViewController

- (id)initWithItemId:(NSString *)itemId
{
    self = [super initWithNibName:@"ChatterFeedViewController" bundle:nil];
    if (self) {
        self.title = @"Salesforce Chatter";
        self.itemId = itemId;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    [self replaceBackButtonWithClose];
    [self setupBars];

    // I would have preferred to pass this into the init, but since I use Interface Builder to layout the ViewController I cannot.
    [self.chatterFeedView setItemId:self.itemId];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// configures the navigation bar and tool bar programmatically.  I would have preferred to just use the default iOS settings...
- (void)setupBars
{
    self.navigationController.toolbarHidden = YES;

    self.navigationController.navigationBarHidden = NO;

    [self createOpenChatterButton];
    //self.toolbarItems = [self getToolbarItems];
}

- (void)createOpenChatterButton
{
    UIBarButtonItem *openChatter = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"View in Chatter"] style:UIBarButtonItemStyleBordered target:self action:@selector(launchChatterApp)];
    self.navigationItem.rightBarButtonItem = openChatter;
}

// builds list of tool bar items
- (NSMutableArray *)getToolbarItems
{
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    UIBarButtonItem *button = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"View in Chatter"] style:UIBarButtonItemStyleBordered target:self action:@selector(launchChatterApp)];
    return [[NSMutableArray alloc]initWithObjects:flexibleSpace, button, nil];
}

// opens salesforce chatter app
// TODO: pass in feed details so it opens to the right place
- (void)launchChatterApp
{
    [AppsUtil launchChatterApp];
}

// on iPad, we close the view instead of going back to the menu
- (void)replaceBackButtonWithClose
{
    UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"] style:UIBarButtonItemStyleBordered target:self action:@selector(closeMe)];
    self.navigationItem.leftBarButtonItem = btnClose;
}

// closes this ViewController.  Also closes any popovers that may be open.
- (void)closeMe
{
    if (self.pickerPopOver != nil && pickerPopOver.popoverVisible)
        [pickerPopOver dismissPopoverAnimated:YES];
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
