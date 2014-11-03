//
//  MsgResponder.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/12/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "MsgResponder.h"
#import "XMLBase.h"

@implementation MsgResponder


#pragma mark -
#pragma mark Coder Methods

- (void)encodeWithCoder:(NSCoder *)coder
{
}

- (id)initWithCoder:(NSCoder *)coder
{
    return self;
}

#pragma mark -
#pragma mark XML Init Methods

-(void) respondToXMLData:(NSData *)data
{

}


-(void) respondToXMLData:(NSData *)data withMsg:(Msg*)msg
{
// Comment out the following to let subclass determine whether to skip parsing on error.
//    if ((msg.responseCode >= 200 && msg.responseCode <= 299) || msg.data != nil || 
//        msg.isCache)
//    {
        [self respondToXMLData:data];
//    }
}


-(NSString *)getMsgIdKey
{
	return @"MSG_RESPONDER";
}


- (void)parseXMLFileAtData:(NSData *)webData 
{
	NSXMLParser* dataParser = [[NSXMLParser alloc] initWithData:webData];
	
//    NSString *s = [[NSString alloc] initWithData:webData encoding:NSStringEncodingConversionExternalRepresentation];
//    NSLog(@"MsgResponder webData = %@", s);
//    [s release];
	
	[dataParser setDelegate:self];
 	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}


#pragma mark -
#pragma mark Responder Methods
-(id)init
{
    self = [super init];
	if (self)
    {
        respondToMvc = nil;
    }
	return self;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	//NSString *path = [NSString stringWithFormat:@"%@/mobile/MobileSession/Login",[ExSystem sharedInstance].entitySettings.uri]; //example
	//Msg *msg = [[[Msg alloc] initWithData:@"UNKNOWN" State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag] autorelease];	

	//[msg setContentType:@"text/xml"];
	//[msg setMethod:@"POST"];
	//[msg setBody:[self makeXMLBody]];
	
	return nil;
}

-(void)fillInfoToPropagateMsg:(NSMutableDictionary*) parameterBag forMsgId:(NSString*)msgId
{
}

-(BOOL) shouldParseCachedData
{
    return TRUE;
}


@end
