//
//  CCardTransaction.m
//  ConcurMobile
//
//  Created by yiwen on 6/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CCardTransaction.h"


@implementation CCardTransaction
@synthesize description, hasRichData, merchantCity, merchantName, merchantState, merchantCtryCode;
@synthesize cardTypeCode, cardTypeName;

-(NSString*) getIdKey
{
	return [NSString stringWithFormat:@"CC%@", self.cctKey];
}

-(NSString*) getMerchantLocationName
{
	if (merchantCity == nil)
	{
		if (merchantState == nil)
			return merchantCtryCode;

		if (merchantCtryCode == nil)
			return merchantState;
		
		return [NSString stringWithFormat:@"%@, %@", merchantState, merchantCtryCode];
	}
	else {
		if (merchantState == nil)
		{
			if (merchantCtryCode == nil)
				return merchantCity;
			
			return [NSString stringWithFormat:@"%@, , %@", merchantCity, merchantCtryCode];
		}
		
		if (merchantCtryCode == nil)
			return [NSString stringWithFormat:@"%@, %@", merchantCity, merchantState];
		
		return [NSString stringWithFormat:@"%@, %@, %@", merchantCity, merchantState, merchantCtryCode];
	}

}

#pragma mark NSCoding Protocol Methods
- (id)initWithCoder:(NSCoder *)coder
{
	self = [super initWithCoder:coder];
    self.cardTypeCode = [coder decodeObjectForKey:@"cardTypeCode"];
	self.cardTypeName = [coder decodeObjectForKey:@"cardTypeName"];
    self.description = [coder decodeObjectForKey:@"description"];
    self.hasRichData = [coder decodeObjectForKey:@"hasRichData"];
    self.merchantCity = [coder decodeObjectForKey:@"merchantCity"];
    self.merchantName = [coder decodeObjectForKey:@"merchantName"];
    self.merchantState = [coder decodeObjectForKey:@"merchantState"];
    self.merchantCtryCode = [coder decodeObjectForKey:@"merchantCtryCode"];
	return self;
}

- (void)encodeWithCoder:(NSCoder *)coder
{
	[super encodeWithCoder:coder];
	[coder encodeObject:cardTypeCode	forKey:@"cardTypeCode"];
	[coder encodeObject:cardTypeName	forKey:@"cardTypeName"];
	[coder encodeObject:description	forKey:@"description"];
	[coder encodeObject:hasRichData	forKey:@"hasRichData"];
	[coder encodeObject:merchantCity	forKey:@"merchantCity"];
	[coder encodeObject:merchantName	forKey:@"merchantName"];
	[coder encodeObject:merchantState	forKey:@"merchantState"];
	[coder encodeObject:merchantCtryCode	forKey:@"merchantCtryCode"];
}


- (void) copyData:(CCardTransaction*) src
{
	[super copyData:src];
	self.cardTypeName = src.cardTypeName;
	self.cardTypeCode = src.cardTypeCode;
	self.merchantName = src.merchantName;
	self.description = src.description;
}


- (id) newClone
{
	CCardTransaction* newObj = [[CCardTransaction alloc] init];
	[newObj copyData:self];
	return newObj;
}

@end
