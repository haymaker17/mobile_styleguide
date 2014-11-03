//
//  FlowCoverViewController.m
//  FlowCover
//
//  Created by William Woody on 12/13/08.
//  Copyright __MyCompanyName__ 2008. All rights reserved.
//

#import "FlowCoverViewController.h"

@implementation FlowCoverViewController
@synthesize imageData, coverFlowImageIndex, rm;


/*
// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if (self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]) {
        // Custom initialization
    }
    return self;
}
*/

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView {
}
*/


/*
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
}
*/


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation 
{
    return ((interfaceOrientation == UIInterfaceOrientationLandscapeLeft) ||
			(interfaceOrientation == UIInterfaceOrientationLandscapeRight));
}


- (void)didReceiveMemoryWarning 
{
    [super didReceiveMemoryWarning];
}


- (void)dealloc 
{
	// Mob-3577 Crash fix for FlowCoverView
	NSArray *viewsArray = [self.view subviews];
	for (UIView*v in viewsArray) 
	{
		if ([v isKindOfClass:[FlowCoverView class]]) 
		{
			FlowCoverView *fcv = (FlowCoverView*)v;
			fcv.delegate = nil;
		}
	}
	
	[imageData release];
	[rm release];
    [super dealloc];
}


- (IBAction)done:(id)sender
{

	//[[self parentViewController] dismissModalViewControllerAnimated:YES];
	[self  dismissModalViewControllerAnimated:YES];
}

/************************************************************************/
/*																		*/
/*	FlowCover Callbacks													*/
/*																		*/
/************************************************************************/

- (int)flowCoverNumberImages:(FlowCoverView *)view
{
	return [imageData count];
	//return 64;
}

- (UIImage *)flowCover:(FlowCoverView *)view cover:(int)image
{

	return [imageData objectAtIndex:image];
}

- (void)flowCover:(FlowCoverView *)view didSelect:(int)image
{

	rm.coverFlowImageIndex = image;
	[[self parentViewController] dismissModalViewControllerAnimated:YES];
}


@end
