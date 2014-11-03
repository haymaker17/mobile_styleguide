//
//  AppRating.m
//  ConcurMobile
//
//  Created by Paul Kramer on 7/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AppRating.h"
#import "ApproveReportsViewControllerBase.h"
#import "WeakReference.h"
#import "Config.h"

static AppRating* sharedInstance = nil;

@implementation AppRating
@dynamic mvc;

#pragma mark Static Methods
+ (AppRating*) sharedInstance
{
	if (sharedInstance == nil)
	{
		sharedInstance = [[AppRating allocWithZone:NULL] init];
	}
	return sharedInstance;
}

-(MobileViewController*) mvc
{
    return mvc;
}
-(void)setMvc:(MobileViewController *)newValue
{
    MobileViewController *oldValue = mvc;
	
	if (newValue == oldValue)
		return;
	
	if (oldValue != nil)
		[self stopListeningToCancellationNotifications];
	
	mvc = newValue;
	
	if (newValue != nil)
		[self startListeningToCancellationNotifications];
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex 
{
    [AppRating processChoiceToRateApp:buttonIndex vc:self.mvc alertTag:alertView.tag];
}

#pragma mark -
#pragma mark Registration Methods
-(void) startListeningToCancellationNotifications
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter addObserver:self selector:@selector(receivedCancellationNotification:) name:@"MVC_CANCELLATION_MESSAGE" object:nil];
}

-(void) stopListeningToCancellationNotifications
{
	NSNotificationCenter *defaultCenter = [NSNotificationCenter defaultCenter];
	[defaultCenter removeObserver:self name:@"MVC_CANCELLATION_MESSAGE" object:nil];
}

- (void)receivedCancellationNotification:(NSNotification*)notification
{
	NSDictionary* userInfoDict = notification.userInfo;
	if (userInfoDict != nil)
	{
		WeakReference *weakRefToCancelledMvc = userInfoDict[@"CANCELLED_MVC"];
		if (weakRefToCancelledMvc != nil)
		{
			if (weakRefToCancelledMvc.ref == self.mvc)
			{
				[[MCLogging getInstance] log:[NSString stringWithFormat:@"AppRating::receivedCancellationNotification"] Level:MC_LOG_DEBU];
				self.mvc = nil;  // Stops listening to cancellation notifications as a side effect of setting this property
			}
		}
	}
}


#pragma mark App Rating Methods
+(void)offerChoiceToRateApp:(MobileViewController *)vc alertTag:(int)alertTag
{
    if (![ExSystem connectedToNetwork])
        return;
    
    if ([Config isAppleDemo])
        return;
    
	if([AppRating dateDiffSaysPrompt] && [ExSystem sharedInstance].shouldAskAgain) //[[ExSystem sharedInstance].entitySettings.requestAppRating isEqualToString:@"YES"] || 
	{
        AppRating* proxy = [AppRating sharedInstance];
        [proxy setMvc:vc];
		UIAlertView *alert = [[MobileAlertView alloc] initWithTitle: [Localizer getLocalizedText:@"APP_RATING_TITLE"]
															message: [Localizer getLocalizedText:@"APP_RATING_MSG"] 
														   delegate: proxy 
												  cancelButtonTitle: [Localizer getLocalizedText:@"APP_RATING_MSG_DISMISS"] 
												  otherButtonTitles: [Localizer getLocalizedText:@"Rate It!"], [Localizer getLocalizedText:@"Ask Me Later"], nil];
		alert.tag = alertTag;
        
		[alert show];
	}
	else
	{
		[vc afterChoiceToRateApp];
	}
}


+(BOOL) dateDiffSaysPrompt
{
    NSDate *dtLastRated = [ExSystem sharedInstance].entitySettings.dateRatedApp;
    if(dtLastRated == nil)
        return YES; //we don't have a date, so prompt

    NSDate *now = [NSDate date];
    NSTimeInterval interval = [now timeIntervalSinceDate: dtLastRated];
    int dayDiff = interval / 86400;
    if(dayDiff >= 90)
        return YES;
    
    return NO;
}

+(void)processChoiceToRateApp:(int)buttonIndex vc:(MobileViewController *)vc alertTag:(int)alertTag
{
	// If 'Ask me later' (button at index 2) was not pressed
	if (buttonIndex == 2)
	{//ask later
//		[[ExSystem sharedInstance].entitySettings setRequestAppRating:@"NO"];
//        [[ExSystem sharedInstance].entitySettings setDateRatedApp:[NSDate date]];
//		[[ExSystem sharedInstance] saveSettings];
        [ExSystem sharedInstance].shouldAskAgain = NO;
	}
    else if (buttonIndex == 0)
	{
		//'Don't ask again' was pressed.
		[[ExSystem sharedInstance].entitySettings setRequestAppRating:@"NO"];
        [[ExSystem sharedInstance].entitySettings setDateRatedApp:[NSDate date]];
		[[ExSystem sharedInstance] saveSettings];
        
	}
	else if (buttonIndex == 1) // 'Rate it now'
	{
        [[ExSystem sharedInstance].entitySettings setRequestAppRating:@"NO"];
        [[ExSystem sharedInstance].entitySettings setDateRatedApp:[NSDate date]];
		[[ExSystem sharedInstance] saveSettings];
		[AppRating gotoAppStoreRatings];
	}
    
    if(alertTag == 101782)
    {
        ApproveReportsViewControllerBase *reportsVC = (ApproveReportsViewControllerBase*)vc;
        [reportsVC afterChoiceToRateAppApproval];
    }
    else
        [vc afterChoiceToRateApp];
}

+(void)gotoAppStoreRatings
{
	NSString *str = @"itms-apps://ax.itunes.apple.com/WebObjects/MZStore.woa";
	str = [NSString stringWithFormat:@"%@/wa/viewContentsUserReviews?", str]; 
	str = [NSString stringWithFormat:@"%@type=Purple+Software&id=", str];
    
    if ([ExSystem sharedInstance].isSingleUser)
        str = [NSString stringWithFormat:@"%@479280287", str]; // 479280287 is the app id for SingleUser
    else
        str = [NSString stringWithFormat:@"%@335023774", str]; // 372662667 is the app id for Concur Breeze. 335023774 is for corp
	
	[[UIApplication sharedApplication] openURL:[NSURL URLWithString:str]];
}
@end
