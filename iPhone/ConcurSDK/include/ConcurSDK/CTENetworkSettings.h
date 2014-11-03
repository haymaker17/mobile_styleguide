//
//  CTENetworkSettings.h
//  ConcurSDK
//
//  Created by ernest cho on 2/6/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTENetworkSettings : NSObject

+ (id)sharedInstance;

/**
 Resets the network settings to the Library defaults.  
 
 This might not be appropriate for the client app!
 */
- (void)resetToDefaultSettings;

/**
 @return YES if network debugging is enabled
 */
- (BOOL)debugIsEnabled;

/**
 Enables network debugging
 */
- (void)enableDebugMode;

/**
 Disables network debugging
 */
- (void)disableDebugMode;

/**
 @return saved Concur server URL
 */
- (NSURL *)serverURL;

/**
 Sets the Concur server URL to default. https://www.concursolutions.com
 */
- (void)useDefaultServerURL;

/**
 Sets the Concur server URL to the QA server. https://rqa3-cb.concursolutions.com
 */
- (void)useRqa3ServerURL;

/**
 Changes the Concur server URL.  This is used by Dev, QA and Implementation teams.
 
 @param url
        Concur web service server URL
 */
- (void)saveServerURL:(NSString *)url;

/**
 Some endpoints require a locale and we can't safely use the device iOS format.
 
 Right now it's up to the client to correctly modify the locale string.  We need to make this easier before shipping to 3rd parties!
 
 @param locale
        The locale the concur web service expects
 */
- (void)saveLocaleForServer:(NSString *)locale;

/**
 Get the current server locale
 */
- (NSString *)getLocaleForServer;

/**
 @return saved Concur authorisation token
 */
- (NSString *)token;

/**
 Saves token
 
 @param token
 Concur authorisation token
 */
- (void)saveToken:(NSString *)token;

/**
 @return saved Concur session token
 */
- (NSString *)session;

/**
 Saves session
 
 @param session
        Concur session token
 */
- (void)saveSession:(NSString *)session;

/**
 Sets the User-Agent value in the http header
 
 @param userAgentString
 string that needs to be set as user-agent in the http header
 */
- (void)setUserAgentString:(NSString *)userAgent;

/**
 Gets the User-Agent value in the http header
 */
- (NSString *)getUserAgentString;


@end
