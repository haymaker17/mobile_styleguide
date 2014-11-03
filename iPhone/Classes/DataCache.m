//
//  DataCache.m
//  ConcurMobile
//
//  Created by charlottef on 11/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "DataCache.h"

@interface DataCache (private)
-(NSString*)filePathFromDataId:(NSString*)dataId dataType:(NSString*)dataType;
-(BOOL) deleteFilesMatchingId:(NSString*)dataId;
-(NSArray*) findFilesMatchingId:(NSString*)dataId;
@end

@implementation DataCache

#pragma mark - Lookup methods

-(NSData*) getCachedDataForId:(NSString*)dataId dataType:(NSString**)dataType
{
    *dataType = nil;

    NSArray *matchingFiles = [self findFilesMatchingId:dataId];
    if (matchingFiles != nil && matchingFiles.count > 0)
    {
        NSString *filePath = matchingFiles[0];
        NSRange range = [filePath rangeOfString:@"." options:NSBackwardsSearch];
        // Allow file end with "."
        if (range.location != NSNotFound && range.location <= (filePath.length - 1))
        {
            *dataType = [filePath substringFromIndex:range.location + 1];
            return [NSData dataWithContentsOfFile:filePath];
        }
    }
    
    return nil;
}

#pragma mark - Update Methods

-(BOOL) cacheData:(NSData*)data dataType:(NSString*)dataType dataId:(NSString*)dataId
{
    // First delete any old files matching this id
    [self deleteFilesMatchingId:dataId];
    
    // Determine the file name and path
    NSString *filePath = [self filePathFromDataId:dataId dataType:dataType];
    
    // Write the file
    return [data writeToFile:filePath atomically:YES]; // Atomic writing prevents access to partially written file
}

-(BOOL) cacheDataFromFilePath:(NSString*)srcFilePath dataType:(NSString*)dataType dataId:(NSString*)dataId
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *dstFilePath = [self filePathFromDataId:dataId dataType:dataType];
    NSError *error = nil;
    return [fileManager copyItemAtPath:srcFilePath toPath:dstFilePath error:&error];
}

#pragma mark - Helper Methods

-(NSString*)filePathFromDataId:(NSString*)dataId dataType:(NSString*)dataType
{
    NSString *folderPath = [self folderForCache];
    NSString *fileName = [NSString stringWithFormat:@"%@.%@", dataId, dataType];
    NSString *filePath = [folderPath stringByAppendingPathComponent:fileName];
    return filePath;
}

-(BOOL) deleteFilesMatchingId:(NSString*)dataId
{
    BOOL didSucceed = YES;
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError* error = nil;

    NSArray *matchingFiles = [self findFilesMatchingId:dataId];
    for (NSString *filePath in matchingFiles)
    {
        didSucceed &= [fileManager removeItemAtPath:filePath error:&error];
    }
    
    return didSucceed;
}

-(NSArray*) findFilesMatchingId:(NSString*)dataId
{
    // Grab all the files in the folder
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *folder = [self folderForCache];
    NSArray *files = [fileManager contentsOfDirectoryAtPath:folder error:nil];
    
    // Identify the matching files
    NSString *fileNameWithWildcard = [NSString stringWithFormat:@"%@.*", dataId];
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF like %@", fileNameWithWildcard];
    NSArray *matchingFiles = [files filteredArrayUsingPredicate:predicate];
    
    // Create an array containing the full paths of the matching files
    NSMutableArray *matchingFilePaths = [[NSMutableArray alloc] initWithCapacity:matchingFiles.count];
    for (NSString* fileName in matchingFiles)
    {
        NSString *filePath = [folder stringByAppendingPathComponent:fileName];
        [matchingFilePaths addObject:filePath];
    }
    
    return matchingFilePaths;
}

#pragma mark - Purge Method

-(void) purgeUnusedDataFromCache:(NSDictionary*)usedData
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error = nil;
    
    NSDictionary *cachedDataFiles = [self getAllFiles];
    NSArray *cachedDataFileNames = cachedDataFiles.allKeys;
    
    for (NSString *cachedDataFileName in cachedDataFileNames)
    {
        NSRange range = [cachedDataFileName rangeOfString:@"." options:NSBackwardsSearch];
        if (range.location != NSNotFound && range.location > 0)
        {
            NSRange fileIdRange;
            fileIdRange.location = 0;
            fileIdRange.length = range.location;
            
            NSString *cachedDataId = [cachedDataFileName substringWithRange:fileIdRange];
            
            if (usedData[cachedDataId] == nil)
            {
                NSString *cachedDataFilePath = cachedDataFiles[cachedDataFileName];
                [fileManager removeItemAtPath:cachedDataFilePath error:&error];
            }
        }
    }
}

#pragma mark - Helpers for subclasses

-(NSDictionary*) getAllFiles
{
    // Grab all the files in the folder
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *folder = [self folderForCache];
    NSArray *files = [fileManager contentsOfDirectoryAtPath:folder error:nil];
    
    // Identify the matching files
    NSString *wildcard = @"*.*";
    NSPredicate *predicate = [NSPredicate predicateWithFormat:@"SELF like %@", wildcard];
    NSArray *matchingFiles = [files filteredArrayUsingPredicate:predicate];
    
    // Create an array containing the full paths of the matching files
    NSMutableDictionary *filesDict = [[NSMutableDictionary alloc] initWithCapacity:matchingFiles.count];
    for (NSString* fileName in matchingFiles)
    {
        NSString *filePath = [folder stringByAppendingPathComponent:fileName];
        filesDict[fileName] = filePath;
    }
    
    return filesDict;
}

#pragma mark - Overridable Methods

-(NSString*) folderForCache
{
    return nil; // Must be overriden
}

@end
