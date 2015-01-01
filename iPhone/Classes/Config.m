//
//  Config.m
//  ConcurMobile
//
//  Created by AJ Cram on 4/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Config.h"
#import "SalesforceUserManager.h"
#import "UserDefaultsManager.h"
#import "SignInWithTouchID.h"

@implementation Config

# pragma mark - Build Targets
// This is intended for dev build only
+(BOOL) isDevBuild
{
#ifdef DEV_BUILD
    return YES;
#else
    return NO;
#endif
}

// This is intended for DevCon build only
+(BOOL) isDevConBuild
{
    return NO;
}

+(BOOL) isGov
{
    NSDictionary* infoDict = [[NSBundle mainBundle] infoDictionary];
    return [[infoDict objectForKey:@"Government"] boolValue];
}

+(BOOL) isEnterprise
{
#ifdef ENTERPRISE
    return YES;
#else
    return NO;
#endif
}

+(BOOL) isSprintDemoBuild
{
#if SPRINT_DEMO
    return YES;
#else
    return YES;
#endif
}

# pragma mark - Feature in Development
+(BOOL) isNewHotelBooking
{
    return NO;
}

+(BOOL) isNewEditingEnabled
{
    // set to YES to enable in-line editing for QE
    return NO;
}

/**
 MOB-18709 - Flag for new travel
 */
+(BOOL) isNewTravel
{
    return NO;
}

+(BOOL) isNewAirBooking
{
    return YES;
}

// just for testing now, will remove after OCR feature is completed
+(BOOL) isOCRExpenseEnabled
{
    BOOL isOCRUser = ([self isOCRTesting] && [[ExSystem sharedInstance] hasExpenseIt]);
    return isOCRUser;
}

// Note: should return NO when commit to trunk!
+(BOOL)isOCRTesting
{
    return NO;
}

// new profile workflow
+(BOOL)isProfileEnable
{
    if([self isSprintDemoBuild])
        return YES;
    else
        return NO;
}


# pragma mark - Developer tools
// This is for network debugging on dev machines.  Do NOT commit this set to YES.
+(BOOL) isNetworkDebugEnabled
{
#ifdef DEV_BUILD
    return NO;   // set this to YES to enable network debug trace
#else
    return NO;
#endif
}

# pragma mark - Feature complete (IN USE)
+(BOOL) isCorpHome
{
    NSDictionary* infoDict = [[NSBundle mainBundle] infoDictionary];
    return [[infoDict objectForKey:@"Corporate Home 9"] boolValue];
}

// To enable/Disable voice.
// MOB-14568 - Check for sitesettings
// MOB-14555 - check if the preferred language is english.
// MOB-15533 - enable voice for en\british english
+(BOOL) isEvaVoiceEnabled
{
    BOOL isLangugageSupported  = YES ;
    NSString *prefrerredLanguage  = [Localizer getPreferredLanguage] ;
    isLangugageSupported =  [prefrerredLanguage isEqualToString:@"en"] || [prefrerredLanguage isEqualToString:@"en-GB"] || [prefrerredLanguage isEqualToString:@"en-AU"];
    
    return [[ExSystem sharedInstance] siteSettingVoiceBookingEnabled] && isLangugageSupported && ![self isGov];
}

+(BOOL)isNewSignInFlowEnabled
{
    return YES;
}

+(BOOL)isNewVoiceUIEnabled
{
    if ([ExSystem is7Plus]) // New UI works only for iOS 7 and above
    {
        return YES;
    }
    
    return NO;
}

+(BOOL) isTravelRequestEnabled
{
    
    /*
     * return always false for trunc and branch until Travel Request Will be published
     *
     if ([self isDevBuild] == YES) {
     return YES;
     }*/
    
    return NO;
}

+(BOOL) isEreceiptsEnabled
{
    //     NSUserDefaults *userDefault = [NSUserDefaults standardUserDefaults];
    //    return [userDefault objectForKey:@"Ereceipts"] == nil ? NO : [[userDefault objectForKey:@"Ereceipts"] boolValue] ;
    // Ereceipts is always on.
    return YES;
}

+(BOOL) isTouchIDEnabled
{
    return [[ExSystem sharedInstance] siteSettingAllowsTouchID] && [SignInWithTouchID canEvaluatePolicy];
}

# pragma mark - OLD (may not be in use)
// Salesforce Chatter
+(BOOL) isSalesforceChatterEnabled
{
    if ([[SalesForceUserManager sharedInstance] getInstanceUrl] != nil) {
        return YES;
    }
    return NO;
}

@end