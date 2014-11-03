//
//  PCardTransaction.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CardTransaction.h"

@interface PCardTransaction : CardTransaction 
{
	NSString			*category, *description, *tranStatus;
	// XML-data map: datePosted - tranDate, description - vendorName, crnCode - crnCode, amount-tranAmount
	NSString			*cardName;
	NSString			*pcaKey;
}

@property (strong, nonatomic) NSString			*category;
@property (strong, nonatomic) NSString			*description;
@property (strong, nonatomic) NSString			*tranStatus;
@property (strong, nonatomic) NSString			*cardName;
@property (strong, nonatomic) NSString			*pcaKey;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;
- (id) newClone;
@end
