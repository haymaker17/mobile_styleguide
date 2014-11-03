//
//  SlidingMessageViewController.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/23/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "SlidingMessageViewController.h"


/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Private interface definitions
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
@interface SlidingMessageViewController(private)
- (void)hideMsg;
@end

@implementation SlidingMessageViewController
@synthesize isRefresh;

/**************************************************************************
 *
 * Private implementation section
 *
 **************************************************************************/

#pragma mark -
#pragma mark Private Methods

/*-------------------------------------------------------------
 *
 *------------------------------------------------------------*/
- (void)hideMsg
{
	// Slide the view down off screen
	CGRect frame = self.view.frame;
	
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:.75];
	
	if ([isRefresh isEqualToString:@"YES"])
	{
		frame.origin.y = 500;
	}
	else 
	{
		frame.origin.y = 480;
	}

	self.view.frame = frame;
	
	// To autorelease the Msg, define stop selector
	[UIView setAnimationDelegate:self];
	[UIView setAnimationDidStopSelector:@selector(animationDidStop:finished:context:)];
	
	[UIView commitAnimations];
}

- (void)animationDidStop:(NSString*)animationID finished:(BOOL)finished context:(void *)context 
{
	// Release
	[self release];
}

/**************************************************************************
 *
 * Class implementation section
 *
 **************************************************************************/

#pragma mark -
#pragma mark Initialization

/*-------------------------------------------------------------
 *
 *------------------------------------------------------------*/
- (id)initWithTitle:(NSString *)title message:(NSString *)msg
{
	if (self = [super init]) 
	{
		if ([isRefresh isEqualToString:@"YES"])
		{
			//y coord is high, to slide down from top to bottom...
			self.view = [[[UIView alloc] initWithFrame:CGRectMake(0, 500, 320, 20)] autorelease];
			[self.view setBackgroundColor:[UIColor blueColor]];
			[self.view setAlpha:.85];
			
			UIImage *gotImg = [UIImage imageNamed:@"home_section_bar_full.png"];
			UIImageView *img = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, 320, 20)];
			img.image = gotImg;
			[self.view addSubview:img];
			[img release];
			
			// Title
			titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 0, 280, 20)];
			titleLabel.font = [UIFont boldSystemFontOfSize:10];
			titleLabel.text = title;
			titleLabel.textAlignment = UITextAlignmentCenter;
			titleLabel.textColor = [UIColor blackColor];
			titleLabel.backgroundColor = [UIColor clearColor];
			[self.view addSubview:titleLabel];
			
//			// Message
//			msgLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 5, 280, 80)];
//			msgLabel.font = [UIFont systemFontOfSize:15];
//			msgLabel.text = msg;
//			msgLabel.textAlignment = UITextAlignmentCenter;
//			msgLabel.textColor = [UIColor whiteColor];
//			msgLabel.backgroundColor = [UIColor clearColor];
//			[self.view addSubview:msgLabel];
			

		}
		else 
		{
			// Notice the view y coordinate is offscreen (480)
			// This hides the view
			self.view = [[[UIView alloc] initWithFrame:CGRectMake(0, 480, 320, 90)] 
						 autorelease];
			[self.view setBackgroundColor:[UIColor blackColor]];
			[self.view setAlpha:.87];
			
			
			// Title
			titleLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 5, 280, 30)];
			titleLabel.font = [UIFont boldSystemFontOfSize:17];
			titleLabel.text = title;
			titleLabel.textAlignment = UITextAlignmentCenter;
			titleLabel.textColor = [UIColor whiteColor];
			titleLabel.backgroundColor = [UIColor clearColor];
			[self.view addSubview:titleLabel];
			
			// Message
			msgLabel = [[UILabel alloc] initWithFrame:CGRectMake(20, 5, 280, 80)];
			msgLabel.font = [UIFont systemFontOfSize:15];
			msgLabel.text = msg;
			msgLabel.textAlignment = UITextAlignmentCenter;
			msgLabel.textColor = [UIColor whiteColor];
			msgLabel.backgroundColor = [UIColor clearColor];
			[self.view addSubview:msgLabel];
			
			UIImage *gotImg = [UIImage imageNamed:@"24_parking.png"];
			UIImageView *img = [[UIImageView alloc] initWithFrame:CGRectMake(20, 5, 24, 24)];
			img.image = gotImg;
			[self.view addSubview:img];
			
			[img release];
		}
	}
	
	return self;
}

#pragma mark -
#pragma mark Message Handling

/*-------------------------------------------------------------
 *
 *------------------------------------------------------------*/
- (void)showMsgWithDelay:(int)delay
{
	//  UIView *view = self.view;
	CGRect frame = self.view.frame;
	[UIView beginAnimations:nil context:NULL];
	[UIView setAnimationDuration:.75];
	
	if ([isRefresh isEqualToString:@"YES"])
	{
		frame.origin.y = 460;
	}
	else 
	{
		// Slide up based on y axis
		// A better solution over a hard-coded value would be to
		// determine the size of the title and msg labels and 
		// set this value accordingly
		frame.origin.y = 390;		
	}
	self.view.frame = frame;
	[UIView commitAnimations];
	
	// Hide the view after the requested delay
	[self performSelector:@selector(hideMsg) withObject:nil afterDelay:delay];
	
}

#pragma mark -
#pragma mark Cleanup

/*-------------------------------------------------------------
 *
 *------------------------------------------------------------*/
- (void)dealloc 
{
	if ([self.view superview])
		[self.view removeFromSuperview];
	[titleLabel release];
	[msgLabel release];
	[isRefresh release];
	[super dealloc];
}

@end
