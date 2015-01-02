//
//  MobileAlertView.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 10/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "MobileAlertView.h"
#import "MCLogging.h"

@implementation MobileAlertView

static NSMutableArray* alertsCurrentlyShowing;

@dynamic loggableTitle;
@dynamic loggableMessage;
@synthesize  eventData;

#pragma mark -
#pragma mark NSObject Methods


#pragma mark -
#pragma mark Static Methods
+(void) willPresentMobileAlertView:(MobileAlertView*)mav
{
	@synchronized (self)
	{
		if (alertsCurrentlyShowing == nil) 
		{
			alertsCurrentlyShowing = [[NSMutableArray alloc] init];
		}
		
		[alertsCurrentlyShowing addObject:mav];
	}
}

+(void) didDismissMobileAlertView:(MobileAlertView*)mav
{
	@synchronized (self)
	{
		if (alertsCurrentlyShowing == nil) 
		{
			return;
		}
		
		[alertsCurrentlyShowing removeObject:mav];
	}
}

+(void) dismissAllMobileAlertViews
{
	[[MCLogging getInstance] log:@"MobileAlertView::dismissAllMobileAlertViews" Level:MC_LOG_DEBU];
	
	@synchronized (self)
	{
		if (alertsCurrentlyShowing != nil)
		{
			while ([alertsCurrentlyShowing count] > 0)
			{
				MobileAlertView* mav = alertsCurrentlyShowing[0];
				[mav clearDelegate];
				mav.delegate = nil;
				[mav dismissWithClickedButtonIndex:0 animated:NO];
				[alertsCurrentlyShowing removeObject:mav];
			}
		}
	}
}

#pragma mark -
#pragma mark Dynamic Properties
-(NSString*)loggableTitle
{
	return (self.title ? self.title : @"");
}

-(NSString*)loggableMessage
{
	if (self.message == nil)
	{
		return @"";
	}
	
	const int maxLength = 30;
	return ([self.message length] > maxLength ? [self.message substringToIndex:maxLength] : self.message);
}

#pragma mark -
#pragma mark Delegate Methods
-(void)clearDelegate
{
	mobileAlertView_originalDelegate = nil;
}

#pragma mark -
#pragma mark UIAlertView Methods
-(void)show
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MobileAlertView::show: title = %@, message = %@", self.loggableTitle, self.loggableMessage] Level:MC_LOG_DEBU];
	mobileAlertView_originalDelegate = super.delegate;
	super.delegate = self;
	[super show];
}

#pragma mark -
#pragma mark UIAlertViewDelegate Methods

- (void)willPresentAlertView:(UIAlertView *)alertView
{
	[MobileAlertView willPresentMobileAlertView:self];
	
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MobileAlertView::willPresentAlertView: title = %@, message = %@", self.loggableTitle, self.loggableMessage] Level:MC_LOG_DEBU];

	if (mobileAlertView_originalDelegate != nil && mobileAlertView_originalDelegate != self && [mobileAlertView_originalDelegate respondsToSelector:@selector(willPresentAlertView:)])
	{
		[mobileAlertView_originalDelegate willPresentAlertView:alertView];
	}
}

- (void)didPresentAlertView:(UIAlertView *)alertView
{
	if (mobileAlertView_originalDelegate != nil && mobileAlertView_originalDelegate != self && [mobileAlertView_originalDelegate respondsToSelector:@selector(didPresentAlertView:)])
	{
		[mobileAlertView_originalDelegate didPresentAlertView:alertView];
	}
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
	if (mobileAlertView_originalDelegate != nil && mobileAlertView_originalDelegate != self && [mobileAlertView_originalDelegate respondsToSelector:@selector(alertView:clickedButtonAtIndex:)])
	{
		[mobileAlertView_originalDelegate alertView:alertView clickedButtonAtIndex:buttonIndex];
	}
}

- (void)alertView:(UIAlertView *)alertView willDismissWithButtonIndex:(NSInteger)buttonIndex
{
	if (mobileAlertView_originalDelegate != nil && mobileAlertView_originalDelegate != self && [mobileAlertView_originalDelegate respondsToSelector:@selector(alertView:willDismissWithButtonIndex:)])
	{
		[mobileAlertView_originalDelegate alertView:alertView willDismissWithButtonIndex:buttonIndex];
	}
}

- (void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
	[[MCLogging getInstance] log:[NSString stringWithFormat:@"MobileAlertView::alertView:didDismissWithButtonIndex: title = %@, message = %@, buttonIndex = %li", self.loggableTitle, self.loggableMessage, (long)buttonIndex] Level:MC_LOG_DEBU];
	
    //suggest change to the following code to fix ALL double-alert problem in iOS 6.0
    //Maynot work in some cases due to apple framework bug. See Rev#: 287961, UploadQueueAlertView::didDismissWithButtonIndex
    //    if (mobileAlertView_originalDelegate != nil && mobileAlertView_originalDelegate != self && [mobileAlertView_originalDelegate respondsToSelector:@selector(alertView:clickedButtonAtIndex:)])
    //    {
    //        [mobileAlertView_originalDelegate alertView:alertView clickedButtonAtIndex:buttonIndex];
    //    }
    
	if (mobileAlertView_originalDelegate != nil && mobileAlertView_originalDelegate != self && [mobileAlertView_originalDelegate respondsToSelector:@selector(alertView:didDismissWithButtonIndex:)])
	{
		[mobileAlertView_originalDelegate alertView:alertView didDismissWithButtonIndex:buttonIndex];
	}

	[MobileAlertView didDismissMobileAlertView:self];
}

@end
