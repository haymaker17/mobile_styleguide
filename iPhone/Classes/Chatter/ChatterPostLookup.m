//
//  ChatterPostLookup.m
//  ConcurMobile
//
//  Created by ernest cho on 6/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ChatterPostLookup.h"

@interface ChatterPostLookup()
// maps a trip record locator to a chatter post item id.
@property (nonatomic, readwrite, strong) NSMutableDictionary* postLookupDictionary;
@end

// This is demo code.  Really, sharing a trip in chatter is a more involved process.
// We just post about it and keep a mapping of the post item id to the concur trip record locator.
@implementation ChatterPostLookup

- (id)init
{
    self = [super init];
    if (self) {
        [self loadDictionary];
    }
    return self;
}

- (NSString *)plistFilePath
{
    NSString *rootPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"ChatterPostLookup.plist"];
    return plistPath;
}

- (void)loadDictionary
{
    self.postLookupDictionary = [[NSMutableDictionary alloc] init];
    self.postLookupDictionary = [self.postLookupDictionary initWithContentsOfFile:[self plistFilePath]];
    if (self.postLookupDictionary == nil) {
        self.postLookupDictionary = [[NSMutableDictionary alloc] init];
    }
}

// Using the trip record locator as the trip identifier
- (NSString *)getPostItemIdForTrip:(NSString *)recordLocator
{
    if (self.postLookupDictionary != nil) {
        return self.postLookupDictionary[recordLocator];
    }
    return nil;
}

- (void)associateTrip:(NSString *)recordLocator withPost:(NSString *)itemId
{
    if (self.postLookupDictionary != nil) {
        [self.postLookupDictionary setValue:itemId forKey:recordLocator];
        [self.postLookupDictionary writeToFile:[self plistFilePath] atomically:YES];
    }
}

// remove trip association when we discover the server has deleted it
- (void)removeTrip:(NSString *)itemId
{
    if (self.postLookupDictionary != nil) {
        NSEnumerator *keys = [self.postLookupDictionary keyEnumerator];
        for (NSString *key in keys) {
            NSString *value = [self.postLookupDictionary objectForKey:key];
            if ([value isEqualToString:itemId]) {
                [self.postLookupDictionary removeObjectForKey:key];
                [self.postLookupDictionary writeToFile:[self plistFilePath] atomically:YES];
                return;
            }
        }
    }
}

@end
