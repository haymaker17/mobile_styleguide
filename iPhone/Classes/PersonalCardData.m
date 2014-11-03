//
//  PersonalCardData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PersonalCardData.h"
#import "PCardTransaction.h"

@implementation PersonalCardData
@synthesize accountNumberLastFour, cardName, crnCode, pcaKey, tran;
@synthesize	trans;
@synthesize	tranKeys;
@synthesize runningTotal;
@synthesize transCount;
-(id)init
{
    self = [super init];
	if (self)
    {
        trans = [[NSMutableDictionary alloc] init];
        tran = [[PCardTransaction alloc] init];
        tranKeys = [[NSMutableArray alloc] init];
        transCount = 0;
    }
	return self;
}


-(void)finishTran
{
	if (tran != nil) 
	{
		trans[tran.pctKey] = tran;
		[tranKeys addObject:tran.pctKey];
		tran = [[PCardTransaction alloc] init];
	}
}



#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:crnCode	forKey:@"crnCode"];
	[coder encodeObject:cardName	forKey:@"cardName"];
	[coder encodeObject:pcaKey	forKey:@"pcaKey"];
}

- (id)initWithCoder:(NSCoder *)coder {
	//    self = [super initWithCoder:coder];
    self.crnCode = [coder decodeObjectForKey:@"crnCode"];
	self.cardName = [coder decodeObjectForKey:@"cardName"];
    self.pcaKey = [coder decodeObjectForKey:@"pcaKey"];
	return self;
}

@end
