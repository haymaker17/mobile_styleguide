//
//  TrainSearchingVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainSearchingVC.h"
#import "MsgControl.h"
#import "TrainTimeTablesFetchData.h"
#import "ConcurMobileAppDelegate.h"

@implementation TrainSearchingVC
@synthesize lblMsg, shop;


#pragma mark -
#pragma mark MVC Methods
-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:TRAIN_SHOP])
	{
		//TrainTimeTablesFetchData *trainTimes = (TrainTimeTablesFetchData *)msg.responder;
		//NSLog(@"Number of choices = %d", [trainTimes.keys count]);
		
		[self.navigationController popViewControllerAnimated:YES];
		self.navigationController.navigationBar.hidden = NO;
		self.navigationController.toolbar.hidden = NO;
		
		TrainBookListingVC *tblvc = [[TrainBookListingVC alloc] initWithNibName:@"TrainBookListingVC" bundle:nil];
		tblvc.hidesBottomBarWhenPushed = NO;
		//tblvc.trainBooking = trainBooking;
		ConcurMobileAppDelegate *delegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
		[delegate.navController pushViewController:tblvc animated:YES];
		
	}
}


-(NSString *)getViewIDKey
{
	return @"TRAIN_SHOPPING";
}


-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	[self fetchTrains:self];
	self.navigationController.navigationBar.hidden = YES;
	self.navigationController.toolbar.hidden = YES;
	self.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height + 88);
    [super viewDidLoad];
}




- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}




#pragma mark -
#pragma mark Fetching Methods
-(void)fetchTrains:(id)sender
{
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:[self getViewIDKey], @"TO_VIEW", shop, @"SHOP", nil];
	[[ExSystem sharedInstance].msgControl createMsg:TRAIN_SHOP CacheOnly:@"NO" ParameterBag:pBag SkipCache:NO RespondTo:self];
}

@end
