//
//  AppsUtil.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/4/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AppsUtil.h"
#import "EntityOfferOverlay.h"
#import "Config.h"
#import "UberRequest.h"

// build itunes links with https://itunes.apple.com/linkmaker
@implementation AppsUtil

+(void)launchMapsWithOffer:(EntityOffer*)offer
{

    NSString *searchString = nil;
    NSString *layer = nil;
    
    for(EntityOfferOverlay *overlayObj in offer.relOverlay)
    {
        if([overlayObj.overLay isEqualToString:@"Traffic"])
        {
            layer = [NSString stringWithFormat:@"&layer=t"];
        }
        else
        {
            searchString = overlayObj.overLay;
            
        }
    }
    
    if ([searchString isEqualToString:@"GasStation"]) 
    {
        searchString = @"Gas Stations";
        searchString = [searchString stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    }
    
    NSString *urlString = [NSString stringWithFormat:@"https://maps.google.com/maps?q=%@&sll=%@,%@&radius=%@,%@",searchString,offer.geoLatitude,offer.geoLongitude,offer.geoDimensionkm,layer];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:urlString]];
}

+(void)launchTaxiMagicApp
{
    NSDictionary *dict = @{@"Type": @"Curb"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"taximagic://"]];
	
	if (didLaunch == NO) {
        // <a href="https://itunes.apple.com/us/app/taxi-magic/id299226386?mt=8&uo=4" target="itunes_store">Taxi Magic - RideCharge, Inc.</a>
		NSURL *appStoreUrl = [NSURL URLWithString:@"https://itunes.apple.com/us/app/taxi-magic/id299226386?mt=8&uo=4"];
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}

+(void)launchMetroApp
{
    NSDictionary *dict = @{@"Type": @"Metr0"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"metro://com.kinevia.metro//"]];
	
	if (didLaunch == NO) {
        // <a href="https://itunes.apple.com/us/app/metro/id320949132?mt=8&uo=4" target="itunes_store">MetrO - Kinevia</a>
		NSURL *appStoreUrl = [NSURL URLWithString:@"https://itunes.apple.com/us/app/metro/id320949132?mt=8&uo=4"];
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}

+(void)launchUberApp
{
    NSDictionary *dict = @{@"Type": @"Uber"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
    UberRequest *launchUber = [[UberRequest alloc] init];
    [launchUber requestCar];
}

+(void)launchTripItApp
{
    NSDictionary *dict = @{@"Type": @"TripIt"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
    BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"tripit://"]];
	
	if (didLaunch == NO) {
        // <a href="https://itunes.apple.com/us/app/tripit-travel-organizer-free/id311035142?mt=8&uo=4" target="itunes_store">TripIt - Travel Organizer - FREE - TripIt</a>
		NSURL *appStoreUrl = [NSURL URLWithString:@"https://itunes.apple.com/us/app/tripit-travel-organizer-free/id311035142?mt=8&uo=4"];
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}

+(void)launchExpenseItApp
{
    NSDictionary *dict = @{@"Type": @"ExpenseIt"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
    BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"expenseit://"]];
    
	if (didLaunch == NO) {
		NSURL *appStoreUrl = [NSURL URLWithString:@"https://itunes.apple.com/us/app/expenseit-from-concur/id681323605?mt=8"];
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}

+(void)launchTravelTextApp
{
    NSDictionary *dict = @{@"Type": @"TravelText"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
    BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"travelText://"]];
	
	if (didLaunch == NO) {
		NSURL *appStoreUrl = [NSURL URLWithString:@"http://traveltext.net/Cmobile"];
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}

+(void)launchGateGuruAppWithUrl:(NSString *)urlString
{
    // gate guru is an external app and so is disabled for apple demo
    // areas where gate guru can be launched from are not obviously actionable
    // no need for a prompt. it's safe to silently not launch and demo users won't know the difference
    NSDictionary *dict = @{@"Type": @"GateGuru"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
    NSString * url = @"gateguru://";
    
    if (urlString != nil) {
        url = urlString;
    }
    
	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
	if (didLaunch == NO) {
        // <a href="https://itunes.apple.com/us/app/gateguru-airport-info-flight/id326862399?mt=8&uo=4" target="itunes_store">GateGuru, Airport Info & Flight Status - Mobility Apps LLC</a>
		NSURL *appStoreUrl = [NSURL URLWithString:@"https://itunes.apple.com/us/app/gateguru-airport-info-flight/id326862399?mt=8&uo=4"];
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}

+(void)launchOpenTableApp
{
    NSDictionary *dict = @{@"Type": @"OpenTable"};
    [Flurry logEvent:@"External App: Launch" withParameters:dict];
    
	NSString *openTable = @"reserve://opentable.com/2182?refId=8758";
	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:openTable]];
	if (didLaunch == NO) {
		NSURL *appStoreUrl = [NSURL URLWithString:@"http://itunes.apple.com/us/app/opentable-for-ipad/id375864276?mt=8"]; 
		[[UIApplication sharedApplication] openURL:appStoreUrl];
	}
}
+(void)launchSUApp
{
    NSString * url = @"concursmartexpense://";
    
	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
	if (didLaunch == NO) 
    {
        NSURL *appStoreUrl = [NSURL URLWithString:@"http://itunes.apple.com/us/app/smartexpense-by-concur/id509330414?ls=1&mt=8"];
        [[UIApplication sharedApplication] openURL:appStoreUrl];    
    }
}

+(void)launchCorpApp
{
    NSString * url = @"concurmobile://";
    
	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
	if (didLaunch == NO) 
    {
        NSURL *appStoreUrl = [NSURL URLWithString:@"http://itunes.apple.com/us/app/concur-business-travel-expense/id335023774?mt=8"];
        [[UIApplication sharedApplication] openURL:appStoreUrl];
    }
}

+(void)launchChatterApp
{
    // TODO: set this to the chatter openURL
    NSString * url = @"chatter://";

	BOOL didLaunch = [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
	if (didLaunch == NO)
    {
        NSURL *appStoreUrl = [NSURL URLWithString:@"https://itunes.apple.com/us/app/salesforce-chatter/id404249815?mt=8&uo=4"];
        [[UIApplication sharedApplication] openURL:appStoreUrl];
    }
}

+(void)showTestDrivePrivacyLink
{
      [[UIApplication sharedApplication] openURL: [NSURL URLWithString: @"https://www.concur.com/en-us/privacy-policy"]];
}

+(void)showTestDriveTermsofUse
{
      [[UIApplication sharedApplication] openURL: [NSURL URLWithString: @"https://www.concur.com/en-us/termsandconditions.html"]];
}
@end
