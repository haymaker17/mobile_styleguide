//
//  CorpSSOAuthenticate.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 3/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "CorpSSOAuthenticate.h"
#import "FormatUtils.h"

@implementation CorpSSOAuthenticate

- (void)parseXMLFileAtData:(NSData *)webData 
{	
	[super parseXMLFileAtData:webData];
}

-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    NSURLRequest *request = (NSURLRequest *)parameterBag[@"HTTP_REQUEST"];
	Msg *msg = [[Msg alloc] initWithNSURLRequestAndSkipCache:CORP_SSO_AUTHENTICATION_DATA State:@"" Request:request MessageResponder:self ParameterBag:parameterBag];
    [msg setSkipCache:YES];
    
	return msg;
}

@end
