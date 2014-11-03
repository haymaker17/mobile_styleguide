//
//  AttendeeSearchFieldsData.m
//  ConcurMobile
//
//  Created by yiwen on 10/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AttendeeSearchFieldsData.h"

@implementation AttendeeSearchFieldsData
@synthesize   forms, fields, atnTypeKey, formField;


-(NSString *)getMsgIdKey
{
	return ATTENDEE_SEARCH_FIELDS_DATA;
}

-(void) flushData
{
	[super flushData];
	self.forms = [[NSMutableDictionary alloc] init];
    self.atnTypeKey = nil;
    self.fields = nil;
    self.formField = nil;
}



-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetAttendeeSearchFields",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

/*
 <ArrayOfAttendeeSearchFields xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 <AttendeeSearchFields>
 <AtnTypeKey>1</AtnTypeKey>
 <Fields xmlns:a="http://schemas.datacontract.org/2004/07/Snowbird">
 <a:FormField><a:Access>RW</a:Access><a:CtrlType>edit</a:CtrlType><a:DataType>VARCHAR</a:DataType><a:Id>LastName</a:Id><a:Label>Last Name</a:Label></a:FormField>
 </Fields></AttendeeSearchFields><AttendeeSearchFields><AtnTypeKey>5</AtnTypeKey></AttendeeSearchFields></ArrayOfAttendeeSearchFields>
 */

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	
    elementName = [MsgResponderCommon getUnqualifiedName:elementName]; // Remove namespace 

	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
    if ([elementName isEqualToString:@"AttendeeSearchFields"])
	{
		self.fields = [[NSMutableArray alloc] init];
        self.atnTypeKey = nil;
        self.formField = nil;
	}
    else if ([elementName isEqualToString:@"FormField"])
    {
        self.formField = [[FormFieldData alloc] init];
    }
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	elementName = [MsgResponderCommon getUnqualifiedName:elementName]; // Remove namespace 
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
	if ([elementName isEqualToString:@"FormField"])
	{
		[self.fields addObject:self.formField];
		self.formField = nil;
	}
    else if ([elementName isEqualToString:@"AttendeeSearchFields"])
    {
        (self.forms)[atnTypeKey] = fields;
        self.fields = nil;
        self.atnTypeKey = nil;
    }
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
    if ([currentElement isEqualToString:@"AtnTypeKey"])
        self.atnTypeKey = buildString;
    else if (self.formField != nil)
    {
        NSString* propName = [FormFieldData getXmlToPropertyMap][currentElement];
        if (propName != nil)
        {
            [self.formField setValue:buildString forKey:propName];
        }
	}
}

@end
