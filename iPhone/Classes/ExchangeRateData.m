//
//  ExchangeRateData.m
//  ConcurMobile
//
//  Created by yiwen on 12/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExchangeRateData.h"
#import "DataConstants.h"
//-

@implementation ExchangeRateData
@synthesize fromCrnCode, toCrnCode, forDate, rate, status;

-(NSString *)getMsgIdKey
{
	return EXCHANGE_RATE_DATA;
} 

-(void) flushData
{
	[super flushData];
	self.status = nil;
	self.rate = 1;
}



-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
	self.fromCrnCode = parameterBag[@"FROM_CRN_CODE"];
	self.toCrnCode = parameterBag[@"TO_CRN_CODE"];
	self.forDate = parameterBag[@"FOR_DATE"];
	if (forDate == nil)
	{
		NSDate* today = [NSDate date];
		self.forDate = [CCDateUtilities formatDateYYYYMMddByNSDate:today];  /* e.g. 2011-10-31 */
	}
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ExchangeRate/%@/%@/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.fromCrnCode, self.toCrnCode, self.forDate];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

/*- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ListItem"])
	{
		self.item = [[ListItem alloc] init];
		[self.item release];
	}
}
*/

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"Status"])
	{
		self.status = buildString;
	}
	else if ([elementName isEqualToString:@"ExchangeRate"])
	{
		self.rate = [buildString doubleValue];
	}
	
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
}

#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
	[super encodeWithCoder:coder];
	
	[coder encodeObject:fromCrnCode	forKey:@"fromCrnCode"];
	[coder encodeObject:toCrnCode	forKey:@"toCrnCode"];
	[coder encodeObject:status	forKey:@"status"];
	[coder encodeDouble:rate	forKey:@"rate"];
}

- (id)initWithCoder:(NSCoder *)coder 
{
	if (!(self = [super initWithCoder:coder])) return nil;
	self.fromCrnCode = [coder decodeObjectForKey:@"fromCrnCode"];
	self.toCrnCode = [coder decodeObjectForKey:@"toCrnCode"];
	self.status = [coder decodeObjectForKey:@"status"];
	self.rate = [coder decodeDoubleForKey:@"rate"];
	return self;
}




@end
