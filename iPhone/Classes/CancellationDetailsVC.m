//
//  CancellationDetailsVC.m
//  ConcurMobile
//
//  Created by Chris Butcher on 1/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CancellationDetailsVC.h"

@implementation CancellationDetailsVC
@synthesize cancellationText, txtView;

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
    txtView.text = cancellationText;
    
    self.title = [Localizer getLocalizedText:@"Cancellation Policy"];
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
