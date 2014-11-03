//
//  ArchivedResponder.h
//  ConcurMobile
//
//  Created by yiwen on 8/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

// MOB-4583 - Make ArchivedResponder a protocol to gradually migrate all report related data handler
@class CacheData;

@protocol ArchivedResponder
// Save to local cache, and update meta data of related cached items.
-(void)saveToLocalCache:(NSString*) uId withCacheMeta:(CacheData*)cacheData;
-(void)loadFromLocalCache:(NSString*) uId withCacheMeta:(CacheData*) cacheData;

@end
