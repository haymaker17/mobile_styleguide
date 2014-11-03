//
//  CacheData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CacheMetaData.h"


@interface CacheData : NSObject 
{
	NSMutableDictionary	*cacheDict, *caches;

}
extern NSString * const CACHE_PLIST;
extern NSString * const CACHE_FILE_NAME;
extern NSString * const CACHE_MSG_KEY;
extern NSString * const CACHE_DATE_MODIFIED;
extern NSString * const CACHE_DATE_CREATED;
extern NSString * const CACHE_MSG_TYPE;
extern NSString * const CACHE_FILE_KEY;
extern NSString * const CACHE_DATE_EXPIRES;
extern NSString * const CACHE_NEEDS_REFRESH;
extern NSString * const CACHE_USERID;


@property (strong, nonatomic) NSMutableDictionary	*cacheDict;
@property (strong, nonatomic) NSMutableDictionary	*caches;

-(void)readPlist;
-(void)writeToPlist;
-(id) initPlistFiles;
-(id) init;

-(NSString*) getCacheKey:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;
-(void) removeCacheWithKey:(NSString*) key;
-(CacheMetaData *)getCacheInfo:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;
-(CacheMetaData *)saveCacheMetaData:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;
-(void) markAsNeedingRefresh:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;
-(void) removeCache:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;
-(void) deleteMetaDataForDeletedFiles;

-(void) populateMetaFromDict;
-(void) populateDictFromMeta;


-(void) clearCorruptedCache:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey;
-(void) clearLastCorruptedCache;

@end
