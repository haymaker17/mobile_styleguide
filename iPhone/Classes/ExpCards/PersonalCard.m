//
//  PersonalCard.m
//  ConcurMobile
//
//  Created by yiwen on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "PersonalCard.h"

@implementation PersonalCard


#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:self.crnCode	forKey:@"crnCode"];
	[coder encodeObject:self.cardName	forKey:@"cardName"];
	[coder encodeObject:self.pcaKey	forKey:@"pcaKey"];
    [coder encodeObject:self.accountNumberLastFour forKey:@"accountNumberLastFour"];
}

- (id)initWithCoder:(NSCoder *)coder {
	//    self = [super initWithCoder:coder];
    self.crnCode = [coder decodeObjectForKey:@"crnCode"];
	self.cardName = [coder decodeObjectForKey:@"cardName"];
    self.pcaKey = [coder decodeObjectForKey:@"pcaKey"];
    self.accountNumberLastFour = [coder decodeObjectForKey:@"accountNumberLastFour"];
	return self;
}


@end
