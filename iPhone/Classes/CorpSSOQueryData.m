//
//  CorpSSOQueryData.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 3/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "CorpSSOQueryData.h"
#import "FormatUtils.h"

@implementation CorpSSOQueryData
@synthesize companyCode, ssoUrl, isSSOEnabled, status;
@synthesize serverUrl;

#pragma mark -
#pragma mark XML Init Methods
-(void) flushData
{
    [super flushData];
    self.ssoUrl = nil;
    self.isSSOEnabled = NO;
    self.status = nil;
    self.serverUrl = nil;
}

#pragma mark -
#pragma mark Message Init
-(id)init
{
	self = [super init];
    if (self)
    {
        self.companyCode = nil;
        self.path = nil;
        [self flushData];
    }
	return self;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/mobile/MobileSession/CorpSsoQuery", [ExSystem sharedInstance].entitySettings.uri];
    self.companyCode = parameterBag[@"COMPANY_CODE"];
    
	Msg *msg = [[Msg alloc] initWithData:CORP_SSO_QUERY_DATA State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
    [msg setBody:[self makeXMLBody]];
    [msg setSkipCache:YES];
    
	return msg;
}


-(NSString *)makeXMLBody
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<SsoQuery>"];
	[bodyXML appendString:@"<CompanyCode>%@</CompanyCode>"];
	
	[bodyXML appendString:@"</SsoQuery>"];
	NSString *returnVal = [NSString stringWithFormat:bodyXML, [FormatUtils makeXMLSafe:companyCode]];
	return returnVal;
}

#pragma mark -
#pragma mark URL Methods

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{

}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	self.currentElement = elementName;
    
	buildString = [[NSMutableString alloc] init];
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [buildString appendString:string];
    if ([currentElement isEqualToString:@"SsoUrl"])
    {
        self.ssoUrl = buildString;
    }
    else if ([currentElement isEqualToString:@"SsoEnabled"]) {
        self.isSSOEnabled = [buildString boolValue];
    }
    else if ([currentElement isEqualToString:@"Status"]) {
        self.status = buildString;
    }
    else if ([currentElement isEqualToString:@"ServerUrl"]) {
        self.serverUrl = buildString;
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
}


@end
