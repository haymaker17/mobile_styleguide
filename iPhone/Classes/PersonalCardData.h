//
//  PersonalCardData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PCardTransaction.h"

@interface PersonalCardData : NSObject 
{
	NSString			*accountNumberLastFour, *cardName, *crnCode, *pcaKey;
	NSMutableDictionary	*trans;
	NSMutableArray		*tranKeys;
	PCardTransaction	*tran;
	float				runningTotal;
	int					transCount;
}

@property (nonatomic, strong) NSString			*accountNumberLastFour;
@property (nonatomic, strong) NSString			*cardName;
@property (nonatomic, strong) NSString			*crnCode;
@property (nonatomic, strong) NSString			*pcaKey;
@property (nonatomic, strong) NSMutableDictionary	*trans;
@property (nonatomic, strong) NSMutableArray		*tranKeys;
@property (nonatomic, strong) PCardTransaction	*tran;
@property float runningTotal;
@property int transCount;

-(void)finishTran;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

@end
