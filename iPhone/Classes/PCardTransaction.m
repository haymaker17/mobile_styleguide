//
//  PCardTransaction.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PCardTransaction.h"


@implementation PCardTransaction
@synthesize	category, description, tranStatus;
@synthesize	cardName, pcaKey;

-(NSString*) getIdKey
{
	return [NSString stringWithFormat:@"PC%@", self.pctKey];
}

#pragma mark NSCoding Protocol Methods
- (id)initWithCoder:(NSCoder *)coder
{
	self = [super initWithCoder:coder];
    self.category = [coder decodeObjectForKey:@"category"];
    self.description = [coder decodeObjectForKey:@"description"];
	self.tranStatus = [coder decodeObjectForKey:@"tranStatus"];
    self.cardName = [coder decodeObjectForKey:@"cardName"];
	self.pcaKey = [coder decodeObjectForKey:@"pcaKey"];
	return self;
}

- (void)encodeWithCoder:(NSCoder *)coder
{
	[super encodeWithCoder:coder];
	[coder encodeObject:category	forKey:@"category"];
	[coder encodeObject:description	forKey:@"description"];
	[coder encodeObject:tranStatus	forKey:@"tranStatus"];
	[coder encodeObject:cardName	forKey:@"cardName"];
	[coder encodeObject:pcaKey		forKey:@"pcaKey"];
}



- (void) copyData:(PCardTransaction*) src
{
	[super copyData:src];
	self.cardName = src.cardName;
	self.tranStatus = src.tranStatus;
	self.category = src.category;
	self.description = src.description;
	self.pcaKey = src.pcaKey;
}


- (id) newClone
{
	PCardTransaction* newObj = [[PCardTransaction alloc] init];
	[newObj copyData:self];
	return newObj;
}

@end
