//
//  AnalyticsLogger.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 2/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AnalyticsTracker.h"
#import "GAIFields.h"
#import "GAIDictionaryBuilder.h"

static NSString *const kTrackingPreferenceKey = @"allowTracking";

static int const kGaDispatchPeriod = 60;

@interface AnalyticsTracker ()

@property (strong, nonatomic) id<GAITracker> googleTracker;

@end


@implementation AnalyticsTracker


#pragma mark - initialize
/**
 Create a singleton instance of the analyticsTracker object
 */
+ (instancetype)sharedInstance{
    static id sharedInstance;
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        sharedInstance = [[[self class] alloc] init];
    });
    return sharedInstance;
}

+ (void)initAnalytics
{
     // Initialize Google analytics
    [[AnalyticsTracker sharedInstance] initializeGoogleAnalytics];
    //Initialize flurry here
    [[AnalyticsTracker sharedInstance] initializeFlurry];

}

/**
 Initialize Flurry
 */
-(void)initializeFlurry
{
    [Flurry setCrashReportingEnabled:YES];
#ifdef DEBUG
    [Flurry setAppVersion:@"DEBUG"]; // do not pollute Analytics with data from Debug Build
#endif
    
    [Flurry startSession:[[NSBundle mainBundle] objectForInfoDictionaryKey:@"Flurry Key"]];
   //  [Flurry setUserID:[NSString stringWithFormat:@"|%@",[UIDevice currentDevice].identifierForVendor.UUIDString]];
}

/**
 Initialize Google Analytics
 */
-(void)initializeGoogleAnalytics
{
    // By default the traking is enabled
    // change this to user preference value in future.
    NSDictionary *appDefaults = @{kTrackingPreferenceKey: @(YES)};
    [[NSUserDefaults standardUserDefaults] registerDefaults:appDefaults];
    // User must be able to opt out of tracking
    [GAI sharedInstance].optOut = ![[NSUserDefaults standardUserDefaults] boolForKey:kTrackingPreferenceKey];
    // Optional: automatically send uncaught exceptions to Google Analytics.
    [GAI sharedInstance].trackUncaughtExceptions = YES;
    
    // Optional: set Google Analytics dispatch interval to e.g. 20 seconds.
    [GAI sharedInstance].dispatchInterval = kGaDispatchPeriod;
    // Dont send stats for debug
#ifdef DEBUG    // Turn this on later for now let the data go in.
//    [[GAI sharedInstance] setDryRun:YES];
   // Optional: set Logger to VERBOSE for debug information.
    // Uncomment this for debugging.
//    [[[GAI sharedInstance] logger] setLogLevel:kGAILogLevelVerbose];
#endif
    
    // Initialize tracker.
    NSString *appkey = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"GoogleAnalytics Key"];
    DLog(@"Google Analytics Property key : %@", appkey)
    self.googleTracker = [[GAI sharedInstance] trackerWithTrackingId:appkey];
}

+(void)updateCID:(NSString *)clientID
{
    if (![NSString isEmpty:clientID])
    {
         [[AnalyticsTracker sharedInstance].googleTracker set:kGAIClientId value:clientID];
         [Flurry setUserID:[NSString stringWithFormat:@"%@",clientID]];
    }
}

/**
 Dispatch any pending Google Analytics data to backend
 */
+(void)dispatchAnalytics
{
    [[GAI sharedInstance] dispatch];
}


#pragma mark - Screen name and log events
+(void)initializeScreenName:(NSString *)screenName
{
    // This screen name value will remain set on the tracker and sent with
    // hits until it is set to a new value or to nil.
    [[AnalyticsTracker sharedInstance].googleTracker set:kGAIScreenName value:screenName];
    
    // Send the screen view.
    [[AnalyticsTracker sharedInstance].googleTracker send:[[GAIDictionaryBuilder createAppView] build]];
}

+(void)resetScreenName
{
    [[AnalyticsTracker sharedInstance].googleTracker set:kGAIScreenName value:nil];
}

+(void)logEventWithCategory:(NSString *)category eventAction:(NSString *)action eventLabel:(NSString *)label eventValue:(NSNumber *)value
{
    [[AnalyticsTracker sharedInstance].googleTracker send:[[GAIDictionaryBuilder createEventWithCategory:category       // Event category (required)
                                                                                                  action:action         // Event action (required)
                                                                                                   label:label            // Event label
                                                                                                   value:value] build]];  // Event value
}

@end
