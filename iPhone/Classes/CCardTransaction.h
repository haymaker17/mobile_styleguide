//
//  CCardTransaction.h
//  ConcurMobile
//
//  Created by yiwen on 6/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CardTransaction.h"

@interface CCardTransaction : CardTransaction 
{
	// XML-data map: transactionData - tranDate, doingBusinessAs - vendorName, transactionCrnCode - crnCode
	NSString *description, *hasRichData, *merchantCity, *merchantName, *merchantState, *merchantCtryCode;
	NSString *cardTypeCode, *cardTypeName;
}

@property (strong, nonatomic) NSString			*description;
@property (strong, nonatomic) NSString			*hasRichData;
@property (strong, nonatomic) NSString			*merchantCity;
@property (strong, nonatomic) NSString			*merchantName;
@property (strong, nonatomic) NSString			*merchantState;
@property (strong, nonatomic) NSString			*merchantCtryCode;
@property (strong, nonatomic) NSString			*cardTypeCode;
@property (strong, nonatomic) NSString			*cardTypeName;

-(NSString*) getMerchantLocationName;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

- (id) newClone;

@end
