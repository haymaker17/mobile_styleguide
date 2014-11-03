//
//  GetReceiptUrl.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "GetReceiptUrl.h"
#import "DataConstants.h"

@implementation GetReceiptUrl
@synthesize receiptUrl, status, fileType;

-(NSString *)getMsgIdKey
{
	return GET_RECEIPT_URL;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = parameterBag[@"URL"];
	
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
	if ([currentElement isEqualToString:@"ReceiptImageURL"])
	{
		self.receiptUrl = buildString;
	}
    else if ([currentElement isEqualToString:@"FileType"])
    {
        self.fileType = buildString;
    }
	else if ([currentElement isEqualToString:@"Status"])
	{
		self.status = buildString;
	}
}
@end
