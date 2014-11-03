//
//  ValidateSessionData.m
//  ConcurMobile
//
//  Created by yiwen on 10/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ValidateSessionData.h"
#import "ExSystem.h"

@implementation ValidateSessionData
@synthesize dict, isInElement;

static NSMutableDictionary* userContactMap = nil;

+ (void)initialize
{
	if (self == [ValidateSessionData class]) 
	{
        // Perform initialization here.
		userContactMap = [[NSMutableDictionary alloc] init];
		userContactMap[@"FirstName"] = @"FirstName";
		userContactMap[@"LastName"] = @"LastName";
		userContactMap[@"Mi"] = @"Mi";
		userContactMap[@"Email"] = @"Email";
	}
}


-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	
	[self flushData];
	[self parseXMLFileAtData:data];
}

-(id)init
{
	self = [super init];
    if (self)
    {
        self.isInElement = @"NO";
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return @"VALIDATE_SESSION";
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.path = [NSString stringWithFormat:@"%@/Mobile/Home/ValidateSession", 
                 [ExSystem sharedInstance].entitySettings.uri];
    
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"GET"];
    
	return msg;
}

-(void) flushData
{
	self.dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
}


- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	self.isInElement = @"YES";
	buildString = [[NSMutableString alloc] init];
		
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	self.isInElement = @"NO";
	
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	NSString* propName = userContactMap[currentElement];
	if (propName != nil)
	{
		dict[currentElement] = string;
	}
	else if ([currentElement isEqualToString:@"CompanyName"])
	{
		[buildString appendString:string];
		dict[@"CompanyName"] = buildString;
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
}



@end
