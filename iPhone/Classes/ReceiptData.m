//
//  ReceiptData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReceiptData.h"
#import "ReceiptImageMetaData.h"
#import "ExSystem.h"

@implementation ReceiptData


-(id) initPlistFiles
{
    self = [super init];
    if (self) {
        self.receiptDict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    }
	return self;
}


- (void) readPlist
{
	NSFileManager *manager = [NSFileManager defaultManager];
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"ReceiptData.plist"];
	
	self.receiptDict = [[NSMutableDictionary alloc] init];
	
	if ([manager fileExistsAtPath:path])
	{
		NSMutableDictionary *t = [[NSMutableDictionary alloc] init];
		t = [t initWithContentsOfFile:path];
		if(t != nil)
		{
			[self deserializeForReceiptDict:path];
		}
		
		NSString *keysPath = [documentsDirectory stringByAppendingPathComponent:@"ReceiptKeys.plist"];
		NSMutableArray *keys = [[NSMutableArray alloc] init];
		keys = [keys initWithContentsOfFile:keysPath];
		if(keys != nil)
		{
			self.receipts = [[NSMutableArray alloc] initWithContentsOfFile:keysPath];
		}
		
		if (self.receipts == nil && self.receiptDict != nil)
		{
			self.receipts = [[NSMutableArray alloc] initWithArray:[self.receiptDict allKeys]];
		}
	}
	else
	{	
		self.receipts = [[NSMutableArray alloc] init];
	}
}


- (void) writeToPlist
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *path = [documentsDirectory stringByAppendingPathComponent:@"ReceiptData.plist"];
	
	if (self.receiptDict != nil && [self.receiptDict count] > 0)
	{
		NSDictionary *receiptObjDict = [self makeReceiptDictSerializable];
		BOOL success = [receiptObjDict writeToFile:path atomically: YES];	
		if (!success)
		{
			NSLog(@"receiptDict write to plist failed");
		}
	}
	
	NSString *keysPath = [documentsDirectory stringByAppendingPathComponent:@"ReceiptKeys.plist"];
	
	if (self.receipts != nil) 
	{
		[self.receipts writeToFile:keysPath atomically: YES];
	}
}


-(NSDictionary*) makeReceiptDictSerializable
{
	NSMutableDictionary *receiptObjects = [[NSMutableDictionary alloc] init];
	if (self.receiptDict != nil && [self.receiptDict count] > 0)
	{
		NSArray *receiptsKeys = [self.receiptDict allKeys];
		if (receiptsKeys != nil)
		{
			for (NSString *key in receiptsKeys)
			{
				NSMutableDictionary *receiptObject = [[NSMutableDictionary alloc] init];
				ReceiptImageMetaData *rimd = self.receiptDict[key];
				
				receiptObject[@"KEY"] = rimd.key;
				receiptObject[@"IMAGE_NAME"] = rimd.imageName;	
				receiptObject[@"RECEIPT_NAME"] = rimd.receiptName;
				receiptObject[@"DATE_CREATED"] = rimd.dateCreated;
				receiptObject[@"DATE_MODIFIED"] = rimd.dateModified;
				receiptObject[@"USER_ID"] = rimd.userID;
				receiptObject[@"ANNOTATION"] = rimd.receiptAnnotation;
				receiptObjects[rimd.key] = receiptObject;
			}
		}
	}
	
	return (NSDictionary*)receiptObjects;
}


-(void) deserializeForReceiptDict:(NSString*)plistPath
{
	NSFileManager *manager = [NSFileManager defaultManager];
	NSDictionary *receiptsDict = nil;
	
	if (plistPath != nil && [manager fileExistsAtPath:plistPath])
	{
		receiptsDict = [[NSDictionary alloc] initWithContentsOfFile:plistPath];
	}
	
	if (receiptsDict != nil && [receiptsDict count] > 0)
	{
		NSArray *receiptsKeys = [receiptsDict allKeys];
		if (receiptsKeys != nil)
		{
			for (NSString *key in receiptsKeys)
			{
				NSDictionary *receiptObject = receiptsDict[key];
				ReceiptImageMetaData *rimd = [[ReceiptImageMetaData alloc] init];
				rimd.key = receiptObject[@"KEY"];
				rimd.imageName = receiptObject[@"IMAGE_NAME"];
				rimd.receiptName = receiptObject[@"RECEIPT_NAME"];
				rimd.dateCreated = receiptObject[@"DATE_CREATED"];
				rimd.dateModified = receiptObject[@"DATE_MODIFIED"];
				rimd.userID = receiptObject[@"USER_ID"];
				rimd.receiptAnnotation = receiptObject[@"ANNOTATION"];
				self.receiptDict[rimd.key] = rimd;
			}
		}
	}
    
}




#pragma mark -
#pragma mark Access RIMD by association methods

-(ReceiptImageMetaData *) getReceiptMetaDataByKey:(NSString *)key
{
	return self.receiptDict[key];
}


#pragma mark -
#pragma mark Receipts queue methods
-(void) dequeue 
{
	NSString *key = [self.receipts lastObject];
	ReceiptImageMetaData *rimd = self.receiptDict[key];
	[self removeImage:rimd];
	[self.receipts removeLastObject];
}


-(void) enqueue:(ReceiptImageMetaData*) rimd 
{
	[self checkAndUpdateQueueLimit];
	[self.receipts insertObject:rimd.key atIndex:0];
	self.receiptDict[rimd.key] = rimd;
	[self writeToPlist];
}


-(void)save:(ReceiptImageMetaData*)rimd
{
	[self enqueue:rimd];
}


-(void)checkAndUpdateQueueLimit
{
	if ([self.receipts count] >= RECEIPT_STORE_IMAGE_CACHE_LIMIT)
	{
		[self dequeue]; 
	}
}


-(void)deleteFromReceiptsQueue
{
	[self dequeue];
}


-(void)deleteRimd:(ReceiptImageMetaData*)rimd
{
	int receiptIndex = [self getIndexForRimd:rimd];
	
	if (receiptIndex > -1)
	{
		[self.receipts removeObjectAtIndex:receiptIndex];
		[self.receiptDict removeObjectForKey:rimd.key];
	}
}


-(void)clearCache
{
	NSUInteger limit = [self.receipts count];
	for (int i=0; i<limit; i++) 
	{
		[self dequeue];
	}
}


-(int)getIndexForRimd:(ReceiptImageMetaData*)rimd
{
	int receiptIndex = -1;
	for (int count = 0 ;count < [self.receipts count]; count++)
	{
		NSString* key = self.receipts[count];
		ReceiptImageMetaData *receipt = self.receiptDict[key];
		
		if ([receipt isEqual:rimd])
		{
			receiptIndex = count;
		}
	}
	return receiptIndex;
}

#pragma mark -
#pragma mark Save & Delete methods

-(void) saveReceiptImageMetaData:(ReceiptImageMetaData *) rimd
{
	rimd.imageName = [NSString stringWithFormat:@"%@.png", rimd.key];	
	if (rimd.receiptName == nil)
	{
		rimd.receiptName = @"";
	}
	
	if (rimd.receiptAnnotation == nil)
	{
		rimd.receiptAnnotation = @"";
	}
	rimd.dateCreated = [NSDate date];
	rimd.dateModified =  [NSDate date];
	rimd.userID = [ExSystem sharedInstance].userName;
	[self save:rimd]; 
}


// deletes the image & updates the image count
-(void) removeImage:(ReceiptImageMetaData *)rimd {
	if(rimd != nil && rimd.imageName != nil && rimd.key != nil)
	{
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:rimd.imageName];
		if (initFilePath != nil)
		{
			NSFileManager *fileManager = [NSFileManager defaultManager];
			[fileManager removeItemAtPath:initFilePath error:NULL];
		}
		/* Mob-4407: include the following lines within this block*/
		[self.receiptDict removeObjectForKey:rimd.key];
		[self writeToPlist];
	}
}

@end
