//
//  AttendeeWithFieldsBaseData.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 5/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AttendeeWithFieldsBaseData.h"

@implementation AttendeeWithFieldsBaseData
@synthesize atnColumns;
@synthesize currentField;


- (void) updateFormField:(FormFieldData*)ff property:(NSString*) elementName value: (NSString*) propVal
{
	NSMutableDictionary * formFieldXmlToPropertyMap = [FormFieldData getXmlToPropertyMap];
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


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    self.currentElement = [MsgResponderCommon getUnqualifiedName:currentElement]; // Remove namespace qualification from attendee sub-nodes
    
	if ([currentElement isEqualToString:@"Fields"])
	{
//		self.currentAttendee.fields = [[[NSMutableDictionary alloc] init] autorelease];	
//        self.currentAttendee.fieldKeys = [[[NSMutableArray alloc] init] autorelease];
	}
	else if ([currentElement isEqualToString:@"FormField"])
	{
		self.currentField = [[FormFieldData alloc] init];	// Retain count = 2
	}
    else if ([currentElement isEqualToString:@"ColumnDefinitions"])
    {
        self.atnColumns = [[NSMutableArray alloc] init];
        inAtnColumns = YES;
    }
    else if ([currentElement isEqualToString:@"Attendees"])
    {
        inAtnColumns = NO;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
	if (currentField != nil)
	{
		[self updateFormField:currentField property:currentElement value:buildString];
	}
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    elementName = [MsgResponderCommon getUnqualifiedName:elementName];
	if ([elementName isEqualToString:@"FormField"])
	{
        if (!inAtnColumns)
        {
            [self.currentAttendee.fieldKeys addObject:currentField.iD];
            (self.currentAttendee.fields)[currentField.iD] = currentField;
        }
        else 
        {
            [self.atnColumns addObject:currentField];
        }
        self.currentField = nil;
	}
}


@end
