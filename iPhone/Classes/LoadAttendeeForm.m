//
//  LoadAttendeeForm.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "LoadAttendeeForm.h"
#import "DataConstants.h"
#import "FormFieldData.h"
#import "ReportDetailDataBase.h"

@implementation LoadAttendeeForm

@synthesize path;
@synthesize currentElement;
@synthesize buildString;
@synthesize atnTypeKey;
@synthesize fields;
@synthesize currentField;

-(NSString *)getMsgIdKey
{
	return LOAD_ATTENDEE_FORM;
}

#pragma mark -
#pragma mark Lifecycle


#pragma mark -
#pragma mark Message request
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	NSString *attendeeTypeKey = (NSString*)parameterBag[@"ATTENDEE_TYPE_KEY"];
	NSString *protectedAttendeeKey = (NSString*)parameterBag[@"ATTENDEE_KEY"];
	NSString *prevAtnKey = [ExSystem sharedInstance].prevAtnKey;
    
	if (![protectedAttendeeKey length] && ![prevAtnKey length])
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AttendeeForm/%@",[ExSystem sharedInstance].entitySettings.uri, attendeeTypeKey];
	else if ([protectedAttendeeKey length])
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AttendeeForm/%@/%@",[ExSystem sharedInstance].entitySettings.uri, attendeeTypeKey, protectedAttendeeKey];
	else
    {
        // MOB-8328 protectedAttendeeKey is null & prevAtnKey is not
		self.path = [NSString stringWithFormat:@"%@/mobile/Expense/AttendeeForm/%@/%@/%@",[ExSystem sharedInstance].entitySettings.uri, attendeeTypeKey, @"-1", prevAtnKey];
    }
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"GET"];
	
	self.atnTypeKey = nil;
	self.fields = nil;
	
	return msg;
}

#pragma mark -
#pragma mark Message response
-(void) respondToXMLData:(NSData *)data
{
	[self parseXMLFileAtData:data];
}

#pragma mark -
#pragma mark Parsing
- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	NSString * errorString = [NSString stringWithFormat:@"Unable to get load attendee form (Error code %i )", [parseError code]];
	NSLog(@"error parsing XML: %@", errorString);
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	self.currentElement = elementName;

	self.buildString = [[NSMutableString alloc] init];
	
	if ([elementName isEqualToString:@"Fields"])
	{
		self.fields = [[NSMutableArray alloc] init];	// Retain count = 2
	}
	else if ([elementName isEqualToString:@"a:FormField"])
	{
		self.currentField = [[FormFieldData alloc] init];	// Retain count = 2
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[buildString appendString:string];

	if ([currentElement isEqualToString:@"AtnTypeKey"])
	{
		self.atnTypeKey= string;
	}
	//MOB-15053 - load attendees might return a new atnkey
    if ([currentElement isEqualToString:@"AtnKey"])
	{
		self.atnKey = string;
	}

	//MOB-15053 - AtnTypeCode is not currently used anywhere
    if ([currentElement isEqualToString:@"AtnTypeCode"])
	{
		self.atnTypeCode = string;
	}

	else if (currentField != nil)
	{
		// TODO: Yiwen, what's the best way to strip off the namespace?  E.g. "a:DataType" to "DataType"?
		NSArray* comps = [currentElement componentsSeparatedByString:@":"];
		NSString *propName = comps[([comps count] - 1)];
		[self updateFormField:currentField property:propName value:buildString];
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	if ([elementName isEqualToString:@"a:FormField"])
	{
		[fields addObject:currentField];
		self.currentField = nil;
	}
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
}

- (void) updateFormField:(FormFieldData*)ff property:(NSString*) elementName value: (NSString*) propVal
{
	NSMutableDictionary * formFieldXmlToPropertyMap = [ReportDetailDataBase getFormFieldXmlToPropertyMap];
	NSString* propName = formFieldXmlToPropertyMap[elementName];
	if (propName != nil)
	{
        // TODO: refactor using keyvalue encoding
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        [ff performSelector:NSSelectorFromString([NSString stringWithFormat:@"set%@:", propName]) withObject:buildString];
#pragma clang diagnostic pop
	}
	else if ([elementName isEqualToString:@"Value"])
	{
		//		[buildString appendString:propVal];
		[ff setFieldValue:buildString];
	} else if ([elementName isEqualToString:@"HierKey"])
	{
		ff.hierKey = [propVal intValue];
	}
	else if ([elementName isEqualToString:@"HierLevel"])
	{
		ff.hierLevel = [propVal intValue];
	}
	else if ([elementName isEqualToString:@"ParHierLevel"])
	{
		ff.parHierLevel = [propVal intValue];
	}
	
}


@end
