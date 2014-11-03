//
//  ReceiptManager2.m
//  ConcurMobile
//
//  Created by ernest cho on 1/28/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ReceiptManager2.h"

// We include both these classes cause there's a wierd inheritance pattern here.
#import "EntityReceiptInfo.h"
#import "EntityReceiptInfoExtension.h"

@implementation ReceiptManager2

/**
 Clears cached receipts
 */
- (void)clearCachedReceipts
{
    [self clearCachedReceiptMetadata];
    [self clearCachedReceiptFiles];
}

/**
 Deletes cached receipts.
 
 Does NOT delete receipts in the upload queue.  Those are in the ReceiptsToUpload folder.
 */
- (void)clearCachedReceiptFiles
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];

	NSFileManager *manager = [NSFileManager defaultManager];
    NSArray *fileList = [manager contentsOfDirectoryAtPath:documentsDirectory error:nil];
    for (NSString *fileName in fileList)
	{
		if([fileName isEqualToString:@"Receipts"])
		{
			NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:fileName];

            NSError *error;
			[manager removeItemAtPath:initFilePath error:&error];
            if (error) {
                NSLog(@"Error while clearing Receipts folder! %@, %@", error, [error userInfo]);
            }
		}
    }
}

/**
 Removes receipt metadata from CoreData.
 
 Does NOT clear the upload queue metadata!
 */
- (void)clearCachedReceiptMetadata
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        NSManagedObjectContext *managedObjectContext = [[NSManagedObjectContext alloc] init];
        [managedObjectContext setPersistentStoreCoordinator:coordinator];

        NSArray *receiptsInCoreData = [EntityReceiptInfo fetchAllInContext:managedObjectContext];
        if (receiptsInCoreData != nil && receiptsInCoreData.count > 0)
        {
            for (EntityReceiptInfo* receiptInCoreData in receiptsInCoreData)
            {
                if (receiptInCoreData.receiptId != nil)
                {
                    [EntityReceiptInfo deleteByImageId:receiptInCoreData.receiptId inContext:managedObjectContext];
                }
            }

            NSError *error;
            [managedObjectContext save:&error];
            if (error) {
                NSLog(@"Error while clearing Receipt Metadata! %@, %@", error, [error userInfo]);
            }
        }
    }
}

/**
 Checks if the receipt folder exists
 
 For unit tests
 */
- (BOOL)recieptFolderExists
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];

	NSFileManager *manager = [NSFileManager defaultManager];
    NSArray *fileList = [manager contentsOfDirectoryAtPath:documentsDirectory error:nil];
    for (NSString *fileName in fileList)
	{
		if([fileName isEqualToString:@"Receipts"])
		{
			return YES;
		}
    }
    return NO;
}

/**
 Checks if CoreData contains receipt metadata
 
 For unit tests
 */
- (BOOL)receiptMetadataExists
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        NSManagedObjectContext *managedObjectContext = [[NSManagedObjectContext alloc] init];
        [managedObjectContext setPersistentStoreCoordinator:coordinator];

        NSArray *receiptsInCoreData = [EntityReceiptInfo fetchAllInContext:managedObjectContext];
        if (receiptsInCoreData != nil && receiptsInCoreData.count > 0)
        {
            return YES;
        }
    }
    return NO;
}

@end
