//
//  CacheData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/14/10.
//  Copyright 2010 Concur. All rights reserved.
//	Cache data meta information is stored inside of here.  This dictionary is a dictionary of dictionaries.  The main key is the msg.idKey.
//	The sub dictionaries have a key of the actual file name.  For things like Travel or where there is only one file for the message type,
//	the key is the same as the msg.idKey.

#import "CacheData.h"
#import "CacheMetaData.h"

@implementation CacheData

@synthesize cacheDict, caches;

NSString * const CACHE_PLIST = @"CACHE_PLIST";
NSString * const CACHE_FILE_NAME = @"CACHE_FILE_NAME";
NSString * const CACHE_MSG_KEY = @"CACHE_MSG_KEY";
NSString * const CACHE_DATE_MODIFIED = @"CACHE_DATE_MODIFIED";
NSString * const CACHE_DATE_CREATED = @"CACHE_DATE_CREATED";
NSString * const CACHE_MSG_TYPE = @"CACHE_MSG_TYPE";
NSString * const CACHE_FILE_KEY = @"CACHE_FILE_KEY";
NSString * const CACHE_DATE_EXPIRES = @"CACHE_DATE_EXPIRES";
NSString * const CACHE_NEEDS_REFRESH = @"CACHE_NEEDS_REFRESH";
NSString * const CACHE_USERID = @"CACHE_USERID";

//the key for the dictionary is a composite
//Msg.idKey_RecordKey_UserId
-(id) init
{
    self = [super init];
    if (self) {
        self.cacheDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
        self.caches = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    }
    return self;
}

-(id) initPlistFiles
{
    self = [super init];
    if (self)
    {
        self.cacheDict = [[NSMutableDictionary alloc] init];
        self.caches = [[NSMutableDictionary alloc] init]; // initWithObjectsAndKeys:nil]; 
    }
	return self;
}


- (void)readPlist
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"CacheData.plist"];

	[cacheDict removeAllObjects];
	[caches removeAllObjects];
	NSMutableDictionary *t = [[NSMutableDictionary alloc] init];
	t = [t initWithContentsOfFile:path];
	if(t != nil)
		cacheDict = [cacheDict initWithContentsOfFile:path];
	
	[self populateMetaFromDict];
}


- (void)writeToPlist
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"CacheData.plist"];

		//NSLog(@"Write to CachePlist, path=%@", path);
		if (self.caches != nil) 
		{
			//NSLog(@"caches is not NULL", path);
			[self populateDictFromMeta];
			[self.cacheDict writeToFile:path atomically: YES];
		}
}




#pragma mark -
#pragma mark Object To Dictionary Transformation Methods

-(NSString*) getCacheKey:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey
{
    NSString *key = [NSString stringWithFormat:@"%@_%@_%@", msgId, recordKey, userId];
    return key;
}

-(void) removeCacheWithKey:(NSString*) key
{
    if (caches[key] != nil) 
	{
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:key];
		
		NSFileManager *fileManager = [NSFileManager defaultManager];
		[fileManager removeItemAtPath:initFilePath error:NULL];
		
		[caches removeObjectForKey:key];
		[self writeToPlist];
	}

}

//from the read in plist, populate the caches dictionary, which will contain objects
-(void) populateMetaFromDict
{
	[caches removeAllObjects];
	
	for(NSString *cacheName in cacheDict)
	{
		NSMutableDictionary *cache = cacheDict[cacheName];
		CacheMetaData *cmd = [[CacheMetaData alloc] init];
		cmd.fileName = cache[CACHE_FILE_NAME];
		cmd.msgKey = cache[CACHE_MSG_KEY];
		cmd.dateModified = cache[CACHE_DATE_MODIFIED];
		cmd.dateCreated = cache[CACHE_DATE_CREATED];
		cmd.msgType = cache[CACHE_MSG_TYPE];
		cmd.recordKey = cache[CACHE_FILE_KEY];
		cmd.dateExpires = cache[CACHE_DATE_EXPIRES];
		cmd.needsRefresh = ((cache[CACHE_NEEDS_REFRESH] == nil) ? NO : [cache[CACHE_NEEDS_REFRESH] boolValue]);
		cmd.userID = cache[CACHE_USERID];
		cmd.cacheKey = cacheName;

		caches[cacheName] = cmd; 
	}
}


//fill up the global dictionary with the records from the dictionary containing the object representation.
//this is in prep for writing out to a plist file
-(void) populateDictFromMeta
{
	//	imgDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:iCName, @"CF_NAME", iTName, @"THUMB_NAME", iName, @"IMAGE_NAME", [NSDate date], @"DATE_MODIFIED", [NSDate date], @"DATE_CREATED"
	//			   , @"", @"RECEIPT_NAME", @"", @"ANNOTATION", NSMutableArray, @"ME_KEYS", NSMutableArray, @"RPT_KEYS", nil];
	//NSLog(@"1cacheDict count = %d", [cacheDict retainCount]);
	//if ([cacheDict retainCount] > 0) {
		//[cacheDict release];
	//}
	[cacheDict removeAllObjects]; //clear it out so that we don't have anything that might have been deleted getting pushed back in.
	for(NSString *cacheName in caches)
	{
		
		CacheMetaData *cmd = caches[cacheName];
		
		if(cmd.dateExpires == nil)
			cmd.dateExpires = [NSDate date];
		
		if(cmd.msgType == nil)
			cmd.msgType = cmd.msgKey;
		
		NSMutableDictionary *cDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys: cmd.fileName, CACHE_FILE_NAME, cmd.msgKey, CACHE_MSG_KEY, cmd.dateModified, CACHE_DATE_MODIFIED
									  ,cmd.dateCreated, CACHE_DATE_CREATED, cmd.msgType, CACHE_MSG_TYPE, cmd.recordKey, CACHE_FILE_KEY, cmd.dateExpires, CACHE_DATE_EXPIRES
									  ,@(cmd.needsRefresh), CACHE_NEEDS_REFRESH
									  ,cmd.userID, CACHE_USERID, nil];
		
		cacheDict[cacheName] = cDict; //stored by the imagename (RECEIPT_0.png), imgDict is a dictionary representing the rimd
	}
	//NSLog(@"2cacheDict count = %d", [cacheDict retainCount]);
}

#pragma mark -
#pragma mark Helper Methods
//a return of nil means that it could not find the file
//pass in a @"0" for cache files that are not seperated out for individual files, e.g. reports, or entries, or cc transactions
-(CacheMetaData *)getCacheInfo:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey
{
	NSString *key = [NSString stringWithFormat:@"%@_%@_%@", msgId, recordKey, userId];
	if (caches[key] != nil) 
	{
		return caches[key];
	}
	
	return nil;
}


-(CacheMetaData *)saveCacheMetaData:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey
{
	NSString *key = [NSString stringWithFormat:@"%@_%@_%@", msgId, recordKey, userId];
	//NSLog(@"cache file name key = %@", key);
	
	
	if (caches[key] != nil) 
	{
		CacheMetaData *cmd = caches[key];
		cmd.dateModified = [NSDate date];
		cmd.needsRefresh = NO;
		caches[key] = cmd;
	}
	else 
	{
		CacheMetaData *cmd = [[CacheMetaData alloc] init];
		cmd.dateModified = [NSDate date];
		cmd.dateCreated = [NSDate date];
		cmd.userID = userId;
		cmd.msgKey = msgId;
		cmd.recordKey = recordKey;
		cmd.cacheKey = key;
		cmd.fileName = key;
		cmd.msgType = msgId;
		cmd.dateExpires = [NSDate date];
		cmd.needsRefresh = NO;
		caches[key] = cmd;
	}

	[self writeToPlist]; //need to transform object to dictionary.
	
	return caches[key];
}

-(void) markAsNeedingRefresh:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey
{
	NSString *key = [NSString stringWithFormat:@"%@_%@_%@", msgId, recordKey, userId];
	if (caches[key] != nil) 
	{
		CacheMetaData *cmd = caches[key];
		cmd.needsRefresh = YES;
		[self writeToPlist];
	}
}

-(void) removeCache:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey
{
    NSString* key =[self getCacheKey:msgId UserID:userId RecordKey:recordKey];
    [self removeCacheWithKey:key];
}

-(void) deleteMetaDataForDeletedFiles
{
	// MOB-3918 states that meta-data is not being deleted for cached files that were wiped from the cache.
	// The fix is for this method to be called after a cache wipe (FileManager::cleanCache).
	// It will walk through each instance of CacheMetaData in the dictionary,
	// and remove it if the underlying file no longer exists.
	
	if (caches == nil)
		return;

	NSFileManager *filemgr = [NSFileManager defaultManager];
	NSArray *allCacheNames = [caches allKeys];
	
	for(NSString *cacheName in allCacheNames)
	{
		CacheMetaData *cmd = caches[cacheName];

		if (cmd != nil && ![filemgr fileExistsAtPath:cmd.fileName])
		{
			[caches removeObjectForKey:cacheName];
		}
	}
}

-(void) clearCorruptedCache:(NSString *)msgId UserID:(NSString *)userId RecordKey:(NSString *)recordKey
{
    // Log failed cache id
    NSString * cacheKey = [self getCacheKey:msgId UserID:userId RecordKey:recordKey];
    [[NSUserDefaults standardUserDefaults] setObject:cacheKey forKey:USER_LAST_CORRUPTED_CACHE_KEY];
    [[NSUserDefaults standardUserDefaults] synchronize];
    [self removeCacheWithKey:cacheKey];

    // If successful, wipe out the log entry
    [[NSUserDefaults standardUserDefaults] setObject:@"" forKey:USER_LAST_CORRUPTED_CACHE_KEY];
    [[NSUserDefaults standardUserDefaults] synchronize];
}

-(void) clearLastCorruptedCache
{
    NSString * cacheKey = [[NSUserDefaults standardUserDefaults] stringForKey:USER_LAST_CORRUPTED_CACHE_KEY];
    if ([cacheKey length])
    {
        @try
        {
            [self removeCacheWithKey:cacheKey];
        }
        @catch (NSException *ne)
        {
            
        }
        [[NSUserDefaults standardUserDefaults] setObject:@"" forKey:USER_LAST_CORRUPTED_CACHE_KEY];
        [[NSUserDefaults standardUserDefaults] synchronize];
    }
}
@end
