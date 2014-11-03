//
//  HelpOverlayStatusList.m
//  ConcurMobile
//
//  Created by ernest cho on 10/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "HelpOverlayStatusList.h"

@interface HelpOverlayStatusList()
@property (nonatomic, readwrite, strong) NSMutableDictionary* helpOverlays;
@end

@implementation HelpOverlayStatusList

/**
 Help Overlay display status Singleton
 */
+ (id)sharedList
{
    static HelpOverlayStatusList *overlayStatus = nil;
    @synchronized(self) {
        if (overlayStatus == nil) {
            overlayStatus = [[self alloc] init];
        }
    }
    return overlayStatus;
}

- (id)init {
    if (self = [super init]) {
        [self loadPlist];
    }
    return self;
}

/**
 Path to the plist file.  I could have put this in the default plist, but I felt it might get messy.
 */
- (NSString *)plistFilePath
{
    NSString *rootPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"HelpOverlays.plist"];
    return plistPath;
}

/**
 Loads help overlay plist file
 */
- (void)loadPlist
{
    self.helpOverlays = [[NSMutableDictionary alloc] init];
    self.helpOverlays = [self.helpOverlays initWithContentsOfFile:[self plistFilePath]];
    if (self.helpOverlays == nil) {
        [self makeNewOverlayPlist];
    }
}

/**
 Checks if the overlay is disabled
 */
- (BOOL)isOverlayDisabled:(NSString *)overlayName
{
    [self handleUserChange];

    BOOL isDisabled = [self.helpOverlays[overlayName] boolValue];
    return isDisabled;
}

/**
 Disables the overlay
 */
- (void)disableOverlay:(NSString *)overlayName
{
    [self handleUserChange];

    [self.helpOverlays setValue:@"YES" forKey:overlayName];
    [self.helpOverlays writeToFile:[self plistFilePath] atomically:YES];
}

/**
 Clear overlay status.  For Unit Tests.
 */
- (void)clearStatusForOverlay:(NSString *)overlayName
{
    [self.helpOverlays removeObjectForKey:overlayName];
    [self.helpOverlays writeToFile:[self plistFilePath] atomically:YES];
}

/**
 This check makes sure we clear the overlay status info if you use more than one test drive user on the same device.
 
 This is for QA.
 */
- (void)handleUserChange
{
    if ([self hasUserChanged]) {
        [self makeNewOverlayPlist];
    }
}

/**
 Checks if the username saved in the plist is the same.
 */
- (BOOL)hasUserChanged
{
    // check if this is a unit test.  tests are sometimes not logged in.
    if ([[ExSystem sharedInstance] userName] == nil) {
        return NO;
    }

    NSString *username = self.helpOverlays[@"username"];
    if ([[[ExSystem sharedInstance] userName] isEqualToString:username]) {
        return NO;
    }
    return YES;
}

/**
 Make a new Overlay Plist
 */
- (void)makeNewOverlayPlist
{
    self.helpOverlays = [[NSMutableDictionary alloc] init];

    // We store the username in the plist.  This is the same username prefilled into the login box, so not any less secure!
    [self.helpOverlays setValue:[[ExSystem sharedInstance] userName] forKey:@"username"];
    [self.helpOverlays writeToFile:[self plistFilePath] atomically:YES];
}

@end
