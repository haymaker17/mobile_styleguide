//
//  FileManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FileManager.h"
#import "CacheData.h"
#import "ConcurMobileAppDelegate.h"
#import "iPadHomeVC.h"
#import "ExSystem.h" 


@implementation FileManager

+(void)cleanOldLogs
{   // MOB-7690 Delete logs more than one week old
    NSString* logsDir = [MCLogging obtainLogsDir];
	NSFileManager *manager = [NSFileManager defaultManager];
    NSArray *fileList = [manager contentsOfDirectoryAtPath:logsDir error:nil]; //[manager directoryContentsAtPath:documentsDirectory];
    for (NSString *s in fileList)
	{
        NSError* err = NULL;
        
        NSString* fileFullPath = [logsDir stringByAppendingPathComponent:s];
        
        NSDictionary* attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:fileFullPath error:&err];
        NSDate *lastModificationDate = attributes[NSFileModificationDate];
        //        NSLog(@"%@ %f", lastModificationDate, [lastModificationDate timeIntervalSinceDate:now]);
        if (lastModificationDate != nil && 
            ([lastModificationDate timeIntervalSinceNow]< - 60.0*60.0*24.0*7.0))
        {
            
            [manager removeItemAtPath:fileFullPath error:NULL];
        }
    }
}

+(void)cleanCache
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES); 
    NSString *documentsDirectory = paths[0];
	
	NSFileManager *manager = [NSFileManager defaultManager];
    NSArray *fileList = [manager contentsOfDirectoryAtPath:documentsDirectory error:nil]; //[manager directoryContentsAtPath:documentsDirectory];
    for (NSString *s in fileList)
	{
		if([s isEqualToString:@"admob"] || [s isEqualToString:@"logs"] || [s isEqualToString:@"ExTest.sqlite"] || [s isEqualToString:@"MobileSettings.plist"]|| [s isEqualToString:@"ReceiptData.plist"] || [s isEqualToString:@"BackUpReceiptData.plist"] || [s isEqualToString:@"ReceiptsToUpload"] || [s isEqualToString:@"ChatterPostLookup.plist"] || [s isEqualToString:@"Receipts"])
		{
			//NSLog(@"NOT Killing cache file %@", s);
		}
		else 
		{
			//NSLog(@"Killing cache file %@", s);
			NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:s];
			[manager removeItemAtPath:initFilePath error:NULL];
		}
    }

	// Find the instance of CacheData, if any, that belongs to the root view controller
	CacheData *cacheData = nil;
	ConcurMobileAppDelegate *appDelegate = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
	if (appDelegate != nil && appDelegate.navController != nil)
	{
		NSArray *viewControllers = appDelegate.navController.viewControllers;
		if (viewControllers != nil && [viewControllers count] > 0)
		{
			RootViewController *rootViewController = [ConcurMobileAppDelegate findRootViewController];
			
			if (rootViewController != nil)
			{
				[[ExSystem sharedInstance].receiptData readPlist];
				[[ExSystem sharedInstance].receiptData clearCache];
				
				cacheData = [ExSystem sharedInstance].cacheData;
			}
		}
	}
	
    [[TravelCustomFieldsManager sharedInstance] deleteAll];
    
	// If we don't have an instance of cacheData, then create a new one
	//CacheData *newCacheData = nil;
	if (cacheData == nil)
	{
		cacheData = [[CacheData alloc] initPlistFiles];
		[cacheData readPlist];
	}
    else
        ;  // MOB-6475 To counter the next release
	
	// Delete all cached meta data whose underlying files were deleted
	[cacheData deleteMetaDataForDeletedFiles];
	[cacheData writeToPlist];
}


+(void) removeReceiptCacheFiles
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES); 
    NSString *documentsDirectory = paths[0];
	NSFileManager *manager = [NSFileManager defaultManager];    
	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:@"ReceiptData.plist"];
    
	[manager removeItemAtPath:initFilePath error:NULL];
}


+(void) backUpCachedReceiptsFileFrom:(NSString*)srcPath toFile:(NSString*)backUpPath
{
	NSDictionary *t = [[NSDictionary alloc] initWithContentsOfFile:srcPath];
	if(t != nil && [t count] > 0)
	{
		[t writeToFile:backUpPath atomically:YES];
	}
    
	[FileManager removeReceiptCacheFiles];
}


@end
