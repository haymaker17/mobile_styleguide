//
//  KeychainManager.m
//  ConcurMobile
//
//  Created by ernest cho on 12/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "KeychainManager.h"
#import <CommonCrypto/CommonHMAC.h>
#import "SSKeychain.h"
#import "Config.h"

@implementation KeychainManager

#pragma mark -
#pragma mark Keychain clear and repair methods

/**
 Erases Concur keychain data via SSKeychain.
 
 This does not clean some entries with really unusual keys.
 */
- (void)clearKeychainWithSSKeychain
{
    NSArray *allAccounts = [SSKeychain allAccounts];
    DLog(@"All Items: %@", allAccounts);

    for (int i=0; i<[allAccounts count]; i++) {
        // read the unique keys that identify this item
        NSDictionary *items = (NSDictionary *)allAccounts[i];
        NSString *service = [items objectForKey:(__bridge id)kSecAttrService];
        NSString *account = [items objectForKey:(__bridge id)kSecAttrAccount];

        // delete the item
        [SSKeychain deletePasswordForService:service account:account];
    }
}

/**
 Erases Concur keychain data.
 
 This is a low level implementation of this feature.
 Our mess is bad enough where we can't use KeychainItemWrapper to clean up. :/

 Due to a couple keychain bugs, we have bad data in the keychain prior to version 9.8
 1.  Apple's KeychainItemWrapper did not save the identifier in a unique way.
 2.  We were saving values in kSecAttrAccount.  This means our value was going in as the account key

 See the tests and comments in ConcurKeychainTests.m for more information.
 */
- (void)clearKeychain
{
    // Request all items
    NSMutableDictionary *query = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                  (__bridge id)kCFBooleanTrue,
                                  (__bridge id)kSecReturnAttributes,
                                  (__bridge id)kSecMatchLimitAll,
                                  (__bridge id)kSecMatchLimit,
                                  nil];

    // Provide the data class, KeychainItemWrapper always uses the kSecClassGenericPassword.
    // The others are here for completeness.
    NSArray *secItemClasses = [NSArray arrayWithObjects:
                               (__bridge id)kSecClassGenericPassword,
                               (__bridge id)kSecClassInternetPassword,
                               (__bridge id)kSecClassCertificate,
                               (__bridge id)kSecClassKey,
                               (__bridge id)kSecClassIdentity,
                               nil];

    // Go through all the keychain data classes and request all the data for each
    for (id secItemClass in secItemClasses) {
        // set the data class
        [query setObject:secItemClass forKey:(__bridge id)kSecClass];
        if ([ExSystem is8Plus])
            [query setObject:@YES forKey:(__bridge id)kSecUseNoAuthenticationUI];
        CFTypeRef result = NULL;
        SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);

        if (result != NULL) {

            DLog(@"All Items: %@", (__bridge id)result)

            // Cast the result to an array and iterate through them
            NSArray *items = (__bridge_transfer NSArray *)result;
            for (int i=0; i < [items count]; i++) {
                //  Need to init like this for real iOS6 devices.  Can't just cast to a NSMutableDictionary.
                NSMutableDictionary *item = [NSMutableDictionary dictionaryWithDictionary:(NSDictionary *)[items objectAtIndex:i]];
                
                // set the data class for this item, it doesn't come with it set.
                [item setObject:secItemClass forKey:(__bridge id)kSecClass];

                // delete the item
                OSStatus junk = noErr;
                junk = SecItemDelete((__bridge CFDictionaryRef)item);
                if (junk != noErr && junk != errSecItemNotFound) {
                    NSLog(@"Problem deleting keychain entry.");
                }
            }
        }
    }
}

/**
 Used by unit tests to verify the clear works.
 
 Note that flurry saves to keychain early in the app startup process.  This will often be NO.
 */
- (BOOL)isKeychainEmpty
{
    BOOL isEmpty = NO;
    NSArray *allAccounts = [SSKeychain allAccounts];
    if ([allAccounts count] == 0) {
        isEmpty = YES;
    }

    return isEmpty;
}

/**
 Cleans up keychains that have a version id that doesn't match this version id.
 This has to be run very early in the startup, otherwise it will clear data used by 3rd party libraries.
 
 Only runs once.  It saves a version flag and won't clear the keychain again.
 */
- (void)cleanUpOldKeychain
{
    NSString *currentVersion = @"1.0";
    NSString *versionInKeychain = [self keychainStorageVersion];

    if (![currentVersion isEqualToString:versionInKeychain]) {
        NSMutableDictionary *oldValues = [self retrieveOldConcurKeychainData];
        [self clearKeychain];
        [self migrateOldConcurKeychainData:oldValues];
        [self saveKeychainStorageVersion:currentVersion];
    }
}

/**
 Saves old keychain data in the correct format.
 */
- (void)migrateOldConcurKeychainData:(NSMutableDictionary *)oldValues
{
    // keys that are still valid, ignore any other values that were saved
    NSArray *validKeys = @[ @"cToken", @"cSecret", @"cCorpSSOLoginPageUrl", @"pin", @"session", @"userid", @"companycode", @"ResetPinEmailTokenEmailID", @"ResetPinEmailTokenClientGUID"];

    for (NSString *key in validKeys) {
        NSString *value = [oldValues objectForKey:key];
        // ignore values that are part of old hacks!
        if (value != nil
             && ![@"" isEqualToString:value]
             && ![@" " isEqualToString:value]
             && ![@"noToken" isEqualToString:value]
             && ![@"noSecret" isEqualToString:value]
             && ![@"noCorpSSO" isEqualToString:value]
             && ![@"noVal" isEqualToString:value]) {
            [self saveValue:value forKey:key];
        }
    }
}

/**
 Retrieves incorrectly formatted Concur Keychain data
 */
- (NSMutableDictionary *)retrieveOldConcurKeychainData
{
    NSMutableDictionary *oldValues = [[NSMutableDictionary alloc] init];

    // Request all items
    NSMutableDictionary *query = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                  (__bridge id)kCFBooleanTrue,
                                  (__bridge id)kSecReturnAttributes,
                                  (__bridge id)kSecMatchLimitAll,
                                  (__bridge id)kSecMatchLimit,
                                  nil];

    // set the data class
    [query setObject:(__bridge id)kSecClassGenericPassword forKey:(__bridge id)kSecClass];

    CFTypeRef result = NULL;
    SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);

    if (result != NULL) {
        DLog(@"All Items: %@", (__bridge id)result)

        // Cast the result to an array and iterate through them
        NSArray *items = (__bridge_transfer NSArray *)result;
        for (int i=0; i < [items count]; i++) {
            // Need to init like this for real iOS6 devices.  Can't just cast to a NSMutableDictionary.
            NSMutableDictionary *item = [NSMutableDictionary dictionaryWithDictionary:(NSDictionary *)[items objectAtIndex:i]];

            // set the data class for this item, it doesn't come with it set.
            [item setObject:(__bridge id)kSecClassGenericPassword forKey:(__bridge id)kSecClass];
            
            // the old data is stored with the value in the account field and the key in the generic field
            NSString *key = [item objectForKey:(__bridge id)kSecAttrGeneric];
            NSString *value = [item objectForKey:(__bridge id)kSecAttrAccount];

            // Ignore other data, this is stuff setup by 3rd party libraries.  They will reinitialize themselves.
            if (key != nil && ![@"" isEqualToString:key] && value != nil && ![@"" isEqualToString:value]) {
                [oldValues setObject:value forKey:key];
            }
        }
    }
    return oldValues;
}

/** 
 Gets the keychain storage version id in keychain. This value is used to decide if we need to migrate old broken keychain data.
 */
- (NSString *)keychainStorageVersion
{
    return [self valueForKey:@"concurKeychainStorageVersion"];
}

/**
 Save a storage version. This value is used to decide if we need to migrate old broken keychain data.
 */
- (void)saveKeychainStorageVersion:(NSString *)version
{
    [self saveValue:version forKey:@"concurKeychainStorageVersion"];
}

#pragma mark -
#pragma mark Generic keychain access methods

/**
 Saves value for key.  If value is nil or the empty string, this method calls clearValueForKey.
 This is inconsistent with how keychain works, but it is consistent with how we've been trying to use keychain.

 Our app uses keychain like a dictionary.  To be consistent, the API here uses dictionary terminology.
 */
- (void)saveValue:(NSString *)value forKey:(NSString *)key
{

    // give up if we're given an invalid key
    if (key == nil || [@"" isEqualToString:key]) {
        return;
    }

    if (value == nil || [@"" isEqualToString:value]) {
        // our code tries to use nil and the empty string to clear the value in keychain
        [self clearValueForKey:key];
        return;
    }

    // we hard code the service name to the empty string.  this matches the behavior of KeychainItemWrapper
    [SSKeychain setPassword:value forService:@"" account:key];

//    KeychainItemWrapper *keychainItemWrapper = [[KeychainItemWrapper alloc] initWithIdentifier:key accessGroup:nil];
//    [keychainItemWrapper setObject:value forKey:(__bridge id)kSecValueData];
}

/**
 Gets value for key.
 Our app uses keychain like a dictionary.  To be consistent, the API here uses dictionary terminology.
 
 return nil if the key is not found.
 */
- (NSString *)valueForKey:(NSString *)key
{
    NSString *value = nil;
    if (key != nil && ![@"" isEqualToString:key]) {

        // we hard code the service name to the empty string.  this matches the behavior of KeychainItemWrapper
        value = [SSKeychain passwordForService:@"" account:key];

//        KeychainItemWrapper *keychainItemWrapper = [[KeychainItemWrapper alloc] initWithIdentifier:key accessGroup:nil];
//        value = [keychainItemWrapper objectForKey:(__bridge NSString *)kSecValueData];
    }

    return value;
}

/**
 Clears value for key.

 Use this to explicitly clear the value.
 Setting the value to empty string or nil is NOT recommended.  Keychain is not a dictionary.
 */
- (void)clearValueForKey:(NSString *)key
{
    if (key != nil && ![@"" isEqualToString:key]) {
        // we hard code the service name to the empty string.  this matches the behavior of KeychainItemWrapper
        [SSKeychain deletePasswordForService:@"" account:key];

//        KeychainItemWrapper *keychainItemWrapper = [[KeychainItemWrapper alloc] initWithIdentifier:key accessGroup:nil];
//        [keychainItemWrapper resetKeychainItem];
    }
}


#pragma mark - keychain methods which SignInWithTouchID uses
- (NSString *)loadACLuserID
{
    return [self valueForKey:@"ACLuserID"];
}
- (void)saveACLuserID:(NSString*)ACLuserID
{
    [self saveValue:ACLuserID forKey:@"ACLuserID"];
}
- (void)clearACLuserID
{
    [self clearValueForKey:@"ACLuserID"];
}

- (NSString *)loadACLpassword
{
    return [self valueForKey:@"ACLpassword"];
}

- (void)saveACLpassword:(NSString *)ACLpassword
{
    [self saveValue:ACLpassword forKey:@"ACLpassword"];
}

- (void)clearACLpassword
{
    [self clearValueForKey:@"ACLpassword"];
}


#pragma mark -
#pragma mark keychain methods that ExSystem uses
- (NSString *)loadConcurAccessToken
{
    return [self valueForKey:@"cToken"];
}

- (void)saveConcurAccessToken:(NSString *)sToken
{
    [self saveValue:sToken forKey:@"cToken"];
}

- (NSString *)loadConcurAccessTokenSecret
{
    return [self valueForKey:@"cSecret"];
}

- (void)saveConcurAccessTokenSecret:(NSString *)cSecret
{
    [self saveValue:cSecret forKey:@"cSecret"];
}

- (NSString *)loadCompanySSOLoginPageUrl
{
    return [self valueForKey:@"cCorpSSOLoginPageUrl"];
}

- (void)saveCompanySSOLoginPageUrl:(NSString *)ssoUrl
{
    [self saveValue:ssoUrl forKey:@"cCorpSSOLoginPageUrl"];
}

- (void)clearCompanySSOLoginPageUrl
{
    [self clearValueForKey:@"cCorpSSOLoginPageUrl"];
}

- (NSString *)loadPin
{
    return [self valueForKey:@"pin"];
}

- (void)savePin:(NSString *)sPin
{
    [self saveValue:sPin forKey:@"pin"];
}

- (NSString *)loadSession
{
    return [self valueForKey:@"session"];
}

- (void)saveSession:(NSString *)sSession
{
    [self saveValue:sSession forKey:@"session"];
}

- (NSString *)loadUserId
{
    return [self valueForKey:@"userid"];
}

- (void)saveUserId:(NSString *)sUserId
{
    [self saveValue:sUserId forKey:@"userid"];
}

- (void)clearUserId
{
    [self clearValueForKey:@"userid"];
}

- (NSString *)loadUserInputOnLogin
{
    return [self valueForKey:@"userInputOnLogin"];
}

- (void)saveUserInputOnLogin:(NSString *)sUserInputOnLogin;
{
    [self saveValue:sUserInputOnLogin forKey:@"userInputOnLogin"];
}

- (void)clearUserInputOnLogin
{
    [self clearValueForKey:@"userInputOnLogin"];
}

- (NSString *)loadCompanyCode
{
    return [self valueForKey:@"companycode"];
}

- (void)saveCompanyCode:(NSString *)cCode
{
    [self saveValue:cCode forKey:@"companycode"];
}

- (void)clearCompanyCode
{
    [self clearValueForKey:@"companycode"];
}

#pragma mark -
#pragma mark keychain methods that the Pin reset workflow uses

- (NSString *)loadPinResetEmailToken
{
    return [self valueForKey:@"ResetPinEmailTokenEmailID"];
}

- (void)savePinResetEmailToken:(NSString *)token
{
    [self saveValue:token forKey:@"ResetPinEmailTokenEmailID"];
}

- (NSString *)loadPinResetClientToken
{
    return [self valueForKey:@"ResetPinEmailTokenClientGUID"];
}

- (void)savePinResetClientToken:(NSString *)token
{
    [self saveValue:token forKey:@"ResetPinEmailTokenClientGUID"];
}

@end
