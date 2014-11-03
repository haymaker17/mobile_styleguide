//
//  OOPEntry.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface OOPEntry : NSObject 
{
	NSString			*crnCode, *expKey, *expName, *vendorName, *locationName, *comment, *hasReceipt;
	double				tranAmount;
	NSDate				*tranDate;
	UIImage				*receiptImage;
    NSString            *receiptImageId;
	BOOL				updatedImage;
	NSData				*receiptData;
	NSString            *meKey;
	NSString			*pctKey;
	NSString            *cctKey;
	NSMutableArray		*attendees;
}

@property (strong, nonatomic) NSString *crnCode;
@property (strong, nonatomic) NSString *expKey;
@property (strong, nonatomic) NSString *expName;
@property (strong, nonatomic) NSString *meKey;
@property (strong, nonatomic) NSString *vendorName;
@property (strong, nonatomic) NSString *locationName;
@property (strong, nonatomic) NSString *comment;
@property (strong, nonatomic) NSString *hasReceipt;
@property double tranAmount; 
@property (strong, nonatomic) NSDate	*tranDate;
@property (strong, nonatomic) UIImage	*receiptImage;
@property (strong, nonatomic) NSString	*receiptImageId;
@property (strong, nonatomic) NSData	*receiptData;
@property BOOL updatedImage;
@property (strong, nonatomic) NSString *pctKey;
@property (strong, nonatomic) NSString *cctKey;
@property (strong, nonatomic) NSMutableArray *attendees;

-(NSString*) getKey;

-(NSString*)meKeyImpl;
-(void)setMeKeyImpl:(NSString *)newValue;

-(NSString*)expKeyImpl;
-(void)setExpKeyImpl:(NSString *)newValue;

-(NSString*)expNameImpl;
-(void)setExpNameImpl:(NSString *)newValue;

-(NSString*)commentImpl;
-(void)setCommentImpl:(NSString *)newValue;

-(NSString*)hasReceiptImpl;
-(void)setHasReceiptImpl:(NSString *)newValue;
-(UIImage*)receiptImageImpl;
-(void)setReceiptImageImpl:(UIImage *)newValue;
-(BOOL)updatedImageImpl;
-(void)setUpdatedImageImpl:(BOOL)newValue;
-(NSData*)receiptDataImpl;
-(void)setReceiptDataImpl:(NSData *)newValue;
-(NSString*)receiptImageIdImpl;
-(void)setReceiptImageIdImpl:(NSString *)newValue;

-(id)init;
-(NSString*) getIdKey;  // Unique Id among me, pct and cct objects.
-(BOOL) isOOPEntry;
-(BOOL) isCardTransaction;
-(BOOL) isPersonalCardTransaction;
-(BOOL) isCorporateCardTransaction;

+ (OOPEntry*) restoreEntry:(NSString *)archiveKey;
- (BOOL) persistEntry;
- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;
- (id) newClone;
- (void) copyData:(OOPEntry*) src; 

@end
