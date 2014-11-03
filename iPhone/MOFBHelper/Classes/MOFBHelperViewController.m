#import "MOFBHelperViewController.h"

@implementation MOFBHelperViewController

#pragma mark View Methods

- (void)viewDidLoad {
    [super viewDidLoad];

	session = [FBSession sessionForApplication:@"ca4c0c909252f4a75ba3c17e8e2faf10" secret:@"107e67ce0627d7975c6a830aea9a1c45" delegate:self];		
	
	FBLoginButton* button = [[[FBLoginButton alloc] init] autorelease];
	[self.view addSubview:button];	

	fbHelper = [[MOFBHelper alloc] init];
	fbHelper.delegate = self;
}

- (void)viewDidAppear:(BOOL)animated {
	[super viewDidAppear:animated];	
	[session resume];
}

#pragma mark Facebook Session Protocol Methods

- (void)session:(FBSession*)session didLogin:(FBUID)uid {
	fbHelper.status = @"is learning to set Facebook status programatically from an iPhone";
}

#pragma mark Optional MOFBHelper Protocol Methods

- (void)statusDidUpdate:(MOFBHelper*)helper {
	NSLog(@"status updated");
}

-(void)status:(MOFBHelper*)helper DidFailWithError:(NSError*)error {
	NSLog(@"status update failed: %@", [error description]);
}

# pragma mark Housekeeping

- (void)dealloc {
	[session release];
	[fbHelper release];
    [super dealloc];
}

@end
