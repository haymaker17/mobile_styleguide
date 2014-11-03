//
//  AttendeeBaseData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 2/21/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AttendeeBaseData.h"
#import "Msg.h"

@implementation AttendeeBaseData

@synthesize currentAttendee;
@synthesize attendees;
@synthesize currentGroup;
@synthesize status;
#pragma mark -
#pragma mark Lifecycle
-(id)init
{
	self = [super init];
    if (self)
    {
        self.attendees = [[NSMutableArray alloc] init];
	}
    return self;
}


#pragma mark -
#pragma mark Message response
-(void) respondToXMLData:(NSData *)data
{
	[self parseXMLFileAtData:data];
}

#pragma mark -
#pragma mark Parsing
- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    [super parserDidStartDocument:parser];
	self.attendees = [[NSMutableArray alloc] init];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	if ([elementName isEqualToString:@"Attendee"])
	{
		self.currentAttendee = [[AttendeeData alloc] init];
	}
    else if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.status = [[ActionStatus alloc] init];
	}

}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
	if ([elementName isEqualToString:@"Attendee"])
	{
        if (self.currentGroup != nil)
            [attendees addObject:currentGroup];
        else
            [attendees addObject:currentAttendee];
		self.currentAttendee = nil;
        self.currentGroup = nil;
	}
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
	if ([currentElement isEqualToString:@"Status"])
	{
		self.status.status = buildString;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.status.errMsg = buildString;
	}
    else if ([currentElement isEqualToString:@"GroupKey"] || [currentElement isEqualToString:@"GroupName"])
    {
        if (self.currentGroup == nil)
        {
            self.currentGroup = [[AttendeeGroup alloc] init];
            self.currentAttendee = nil;
        }
        if ([currentElement isEqualToString:@"GroupKey"])
            self.currentGroup.groupKey = buildString;
        else
            self.currentGroup.name = buildString;
    }
	else if (currentAttendee != nil)
	{
        if ([currentElement isEqualToString:@"AtnKey"])
		{
			currentAttendee.attnKey = buildString;
		}
		else if ([currentElement isEqualToString:@"AtnTypeKey"])
		{
			currentAttendee.atnTypeKey = buildString;
		}
		else if ([currentElement isEqualToString:@"VersionNumber"])
		{
			currentAttendee.versionNumber = buildString;
		}
		else if ([currentElement isEqualToString:@"CurrentVersionNumber"])
		{
			currentAttendee.currentVersionNumber = buildString;
		}
		else if ([currentElement isEqualToString:@"AtnTypeCode"])
		{
			[currentAttendee setFieldId:@"AtnTypeCode" value:buildString];	// Do not localize (the field id is a key)
		}
		else if ([currentElement isEqualToString:@"AtnTypeName"])
		{
			[currentAttendee setFieldId:@"AtnTypeName" value:buildString];	// Do not localize (the field id is a key)
		}
		else if ([currentElement isEqualToString:@"Company"])
		{
			[currentAttendee setFieldId:@"Company" value:buildString];	// Do not localize (the field id is a key)
		}
		else if ([currentElement isEqualToString:@"FirstName"])
		{
			[currentAttendee setFieldId:@"FirstName" value:string];	// Do not localize (the field id is a key)
		}
		else if ([currentElement isEqualToString:@"LastName"])
		{
			[currentAttendee setFieldId:@"LastName" value:buildString];	// Do not localize (the field id is a key)
		}
		else if ([currentElement isEqualToString:@"Title"])
		{
			[currentAttendee setFieldId:@"Title" value:buildString];	// Do not localize (the field id is a key)
		}
        else if ([currentElement isEqualToString:@"ExternalId"])
        {
            [currentAttendee setFieldId:@"ExternalId" value:buildString];
        }
	}
}


@end
