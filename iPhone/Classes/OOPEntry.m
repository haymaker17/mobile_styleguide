//
//  OOPEntry.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "OOPEntry.h"
#import "SmartExpenseManager.h"

@implementation OOPEntry
@synthesize crnCode,tranAmount ,tranDate, vendorName, locationName;
@synthesize pctKey, cctKey;
@synthesize attendees;

@dynamic meKey;
@dynamic expKey;
@dynamic expName;
@dynamic comment;
@dynamic hasReceipt;
@dynamic receiptImage;
@dynamic receiptImageId;
@dynamic updatedImage;
@dynamic receiptData;



-(NSString*)meKey
{
	return [self meKeyImpl];
}

-(NSString*)meKeyImpl
{
	return meKey;	// References the field, not the property
}

-(void)setMeKey:(NSString *)newValue
{
	[self setMeKeyImpl:newValue];
}

-(void)setMeKeyImpl:(NSString *)newValue
{
	// References the field, not the property
	meKey = newValue;
}


-(NSString*)expKey
{
	return [self expKeyImpl];
}

-(NSString*)expKeyImpl
{
	return expKey;	// References the field, not the property
}

-(void)setExpKey:(NSString *)newValue
{
	[self setExpKeyImpl:newValue];
}

-(void)setExpKeyImpl:(NSString *)newValue
{
	// References the field, not the property
	expKey = newValue;
}

-(NSString*)expName
{
	return [self expNameImpl];
}

-(NSString*)expNameImpl
{
	return expName;	// References the field, not the property
}

-(void)setExpName:(NSString *)newValue
{
	[self setExpNameImpl:newValue];
}

-(void)setExpNameImpl:(NSString *)newValue
{
	// References the field, not the property
	expName = newValue;
}

-(NSString*)comment
{
	return [self commentImpl];
}

-(NSString*)commentImpl
{
	return comment;	// References the field, not the property
}

-(void)setComment:(NSString *)newValue
{
	[self setCommentImpl:newValue];
}

-(void)setCommentImpl:(NSString *)newValue
{
	// References the field, not the property
	comment = newValue;
}

-(NSString*)hasReceipt
{
	return [self hasReceiptImpl];
}

-(NSString*)hasReceiptImpl
{
	return hasReceipt;	// References the field, not the property
}

-(void)setHasReceipt:(NSString *)newValue
{
	[self setHasReceiptImpl:newValue];
}

-(void)setHasReceiptImpl:(NSString *)newValue
{
	// References the field, not the property
	hasReceipt = newValue;
}

-(NSString*)receiptImageIdImpl
{
    return receiptImageId;
}

-(NSString*)receiptImageId
{
	return [self receiptImageIdImpl];
}

-(void)setReceiptImageIdImpl:(NSString *)newValue
{
	receiptImageId = newValue;
}

-(void)setReceiptImageId:(NSString *)newValue
{
	[self setReceiptImageIdImpl:newValue];
}

-(UIImage*)receiptImage
{
	return [self receiptImageImpl];
}

-(UIImage*)receiptImageImpl
{
	return receiptImage;	// References the field, not the property
}

-(void)setReceiptImage:(UIImage *)newValue
{
	[self setReceiptImageImpl:newValue];
}

-(void)setReceiptImageImpl:(UIImage *)newValue
{
	// References the field, not the property
	receiptImage = newValue;
}

-(BOOL)updatedImage
{
	return [self updatedImageImpl];
}

-(BOOL)updatedImageImpl
{
	return updatedImage;	// References the field, not the property
}

-(void)setUpdatedImage:(BOOL)newValue
{
	[self setUpdatedImageImpl:newValue];
}

-(void)setUpdatedImageImpl:(BOOL)newValue
{
	// References the field, not the property
	updatedImage = newValue;
}

-(NSData*)receiptData
{
	return [self receiptDataImpl];
}

-(NSData*)receiptDataImpl
{
	return receiptData;	// References the field, not the property
}

-(void)setReceiptData:(NSData *)newValue
{
	[self setReceiptDataImpl:newValue];
}

-(void)setReceiptDataImpl:(NSData *)newValue
{
	// References the field, not the property
	receiptData = newValue;
}

-(id)init
{
    self = [super init];
	if (self)
    {
        hasReceipt = @"N";
        updatedImage = FALSE;
    }
	return self;	
}	

-(NSString*) getKey
{
	if (cctKey != nil)
        return cctKey;
    else if (pctKey != nil)
        return pctKey;
    else
        return meKey;	// References the field, not the property
}

-(NSString*) getIdKey
{
	return [NSString stringWithFormat:@"ME%@", self.meKey];
}

-(BOOL) isPersonalCardTransaction
{
	return pctKey != nil;
}

-(BOOL) isCorporateCardTransaction
{
	return cctKey != nil;
}

-(BOOL) isCardTransaction
{
	return [self isPersonalCardTransaction] || [self isCorporateCardTransaction];
}

-(BOOL) isOOPEntry
{
	return ![self isCardTransaction];
}



#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:crnCode	forKey:@"crnCode"];
	[coder encodeObject:expKey	forKey:@"expKey"];
	[coder encodeObject:expName	forKey:@"expName"];
	[coder encodeObject:meKey	forKey:@"meKey"];
	[coder encodeObject:tranDate	forKey:@"tranDate"];
	[coder encodeObject:vendorName	forKey:@"vendorName"];
	[coder encodeObject:locationName	forKey:@"locationName"];
	[coder encodeObject:hasReceipt	forKey:@"hasReceipt"];
	//[coder encodeObject:comment	forKey:@"comment"];
	[coder encodeObject:receiptImageId	forKey:@"receiptImageId"];
	receiptData = UIImageJPEGRepresentation(receiptImage, 0.9f);
	//[code encodeObject:imageData forKey:@"image"];
	
	[coder encodeObject:receiptData	forKey:@"receiptData"];
	[coder encodeObject:pctKey	forKey:@"pctKey"];
	[coder encodeObject:cctKey	forKey:@"cctKey"];
	[coder encodeDouble:tranAmount	forKey:@"tranAmount"];
	[coder encodeObject:comment	forKey:@"comment"];

}

- (id)initWithCoder:(NSCoder *)coder {
	//    self = [super initWithCoder:coder];
    self.crnCode = [coder decodeObjectForKey:@"crnCode"];
	self.expKey = [coder decodeObjectForKey:@"expKey"];
	self.expName = [coder decodeObjectForKey:@"expName"];
	self.meKey = [coder decodeObjectForKey:@"meKey"];
	self.tranDate = [coder decodeObjectForKey:@"tranDate"];
	self.vendorName = [coder decodeObjectForKey:@"vendorName"];
	self.locationName = [coder decodeObjectForKey:@"locationName"];
	self.hasReceipt = [coder decodeObjectForKey:@"hasReceipt"];
	self.comment = [coder decodeObjectForKey:@"comment"];
	self.receiptImageId = [coder decodeObjectForKey:@"receiptImageId"];
	self.receiptData = [coder decodeObjectForKey:@"receiptData"];
	self.pctKey = [coder decodeObjectForKey:@"pctKey"];
	self.cctKey = [coder decodeObjectForKey:@"cctKey"];
	self.tranAmount = [coder decodeDoubleForKey:@"tranAmount"];
	
    return self;
}

- (BOOL) persistEntry
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
	
    NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.archive", self.meKey]];
    BOOL result = [NSKeyedArchiver archiveRootObject:self toFile:archivePath];
    return result;
}

+ (OOPEntry*) restoreEntry:(NSString *)archiveKey
{
    OOPEntry *myEntry;
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = paths[0];
    NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.archive", archiveKey]];
    
    myEntry = [NSKeyedUnarchiver unarchiveObjectWithFile:archivePath];
//    [myEntry retain];
    return myEntry;
}

- (id) newClone
{
	OOPEntry* newObj = [[OOPEntry alloc] init];
	[newObj copyData:self];
	return newObj;
}

- (void) copyData:(OOPEntry*) src
{
	self.pctKey = src.pctKey;
	self.cctKey = src.cctKey;
	self.meKey = src.meKey;
	self.tranDate = src.tranDate;
	self.tranAmount = src.tranAmount;
	self.crnCode = src.crnCode;
	self.locationName = src.locationName;
	self.vendorName = src.vendorName;
	self.expKey = src.expKey;
	self.expName = src.expName;
	self.hasReceipt = src.hasReceipt;
    self.receiptImageId = src.receiptImageId;
	self.comment = src.comment;
}

@end
