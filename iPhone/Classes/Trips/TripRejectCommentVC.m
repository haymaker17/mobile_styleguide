//
//  TripRejectCommentVC.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 03/05/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TripRejectCommentVC.h"

@interface TripRejectCommentVC ()

@end

@implementation TripRejectCommentVC

@synthesize txtComment, tripRejectDelegate;


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
    self.title = [@"Comment" localize];
    UIBarButtonItem *btnReject = [[UIBarButtonItem alloc] initWithTitle:[@"Reject" localize] style:UIBarButtonItemStyleBordered target:self action:@selector(rejectTrip:)];
    self.navigationItem.rightBarButtonItem = btnReject;
    
    if(![UIDevice isPad]) {
        UIBarButtonItem *btnCancel = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemCancel target:self action:@selector(cancel:)];
        self.navigationItem.leftBarButtonItem = btnCancel;
    }
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [txtComment becomeFirstResponder];
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


- (void) rejectTrip:(id)sender
{
    if ([[txtComment.text stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]] length] == 0) {
        MobileAlertView *alert = [[MobileAlertView alloc] initWithTitle:nil message:[@"APPROVE_ENTER_COMMENT_FOR_SENDBACK" localize] delegate:nil
                                                      cancelButtonTitle:[@"LABEL_CLOSE_BTN" localize] otherButtonTitles:nil];
        [alert show];
    }
    else
    {
        [self.tripRejectDelegate rejectedWithComment:txtComment.text];
        [self close:sender];
    }
}


- (void) cancel:(id)sender
{
    [self.tripRejectDelegate rejectionCancelled];
    [self close:sender];
}


- (void) close:(id)sender
{
    if ([UIDevice isPad]) {
        [self.navigationController popViewControllerAnimated:YES]; // Displayed on Navigation controller
    }
    else {
        [self dismissViewControllerAnimated:YES completion:nil]; // Displayed modal(ly) on iPhone
    }
}

@end
