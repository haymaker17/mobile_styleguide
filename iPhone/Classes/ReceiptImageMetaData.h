//
//  ReceiptImageMetaData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ReceiptImageMetaData : NSObject 
{
	NSString						*imageName, *receiptName, *receiptAnnotation, *userID, *receiptImageId;
	NSDate							*dateModified, *dateCreated;
	NSString						*key;
}

@property (strong, nonatomic) NSString				*imageName;
@property (strong, nonatomic) NSString				*receiptName;
@property (strong, nonatomic) NSString				*receiptAnnotation;
@property (strong, nonatomic) NSString				*userID;
@property (strong, nonatomic) NSDate				*dateModified;
@property (strong, nonatomic) NSDate				*dateCreated;
@property (strong, nonatomic) NSString				*key;

-(ReceiptImageMetaData *) init;
-(void)setMeKey:(NSString*)meKey;
-(void)setRptKey:(NSString*)rptKey;
-(void)setRSKey:(NSString*)rsKey;
-(void)setRpeKey:(NSString*)rpeKey;
-(NSString*)getKeyType:(NSString*)theKey;
@end
