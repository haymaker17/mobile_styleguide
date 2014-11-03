//
//  DataCache.h
//  ConcurMobile
//
//  Created by charlottef on 11/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DataCache : NSObject
{
}

#pragma mark - Lookup methods

-(NSData*) getCachedDataForId:(NSString*)dataId dataType:(NSString**)dataType;

#pragma mark - Update Methods

-(BOOL) cacheData:(NSData*)data dataType:(NSString*)dataType dataId:(NSString*)dataId;
-(BOOL) cacheDataFromFilePath:(NSString*)srcFilePath dataType:(NSString*)dataType dataId:(NSString*)dataId;

#pragma mark - Purge Method
-(void) purgeUnusedDataFromCache:(NSDictionary*)usedData;

#pragma mark - Delete Method
-(BOOL) deleteFilesMatchingId:(NSString*)dataId;

#pragma mark - Overridable Methods
-(NSString*) folderForCache;

#pragma mark - Helpers for subclasses
-(NSDictionary*) getAllFiles;

@end
