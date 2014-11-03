//
//  MsgResponderCommon.m
//  ConcurMobile
//
//  Created by yiwen on 11/17/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"
#import "ExSystem.h"

@implementation MsgResponderCommon
@synthesize path, currentElement, buildString;


-(void) flushData
{
	inElement = NO;
	self.currentElement = @"";
	self.buildString = nil;
}

-(id)init
{
    self = [super init];
	if (self)
    {
        self.path = nil;
        [self flushData];
    }
	return self;
}

-(void) respondToXMLData:(NSData *)data
{
    [self flushData];
	[self parseXMLFileAtData:data];
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
    NSString * errorString = [NSString stringWithFormat:@"Unable to get data (Error code %i )", [parseError code]];
    [[MCLogging getInstance] log:errorString Level:MC_LOG_WARN];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	self.currentElement = elementName;
	inElement = YES;
	
	self.buildString = [[NSMutableString alloc] init];
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	inElement = NO;
}

- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[buildString appendString:string];
}


- (void)encodeWithCoder:(NSCoder *)coder
{
	[super encodeWithCoder:coder];
}

- (id)initWithCoder:(NSCoder *)coder
{
	return [super initWithCoder:coder];
}

#pragma mark -
#pragma Helper Methods
+(NSString*) getUnqualifiedName:(NSString*)qualifiedName
{
	NSString *unqualifiedName = qualifiedName;
	
	NSArray* nameComponents = [qualifiedName componentsSeparatedByString:@":"];
	if ([nameComponents count] > 1)
	{
		unqualifiedName = nameComponents[([nameComponents count] - 1)];
	}
	
	return unqualifiedName;
}

@end
