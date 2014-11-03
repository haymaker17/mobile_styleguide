//
//  ViolationDetailsVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/9/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "ViolationDetailsVC.h"

@implementation ViolationDetailsVC
@synthesize violationText, txtView;

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

-(void)closeMe:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:NULL];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    txtView.text = violationText;
    
    self.title = [Localizer getLocalizedText:@"Details"];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.txtView = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}


@end
