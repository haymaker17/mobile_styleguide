//
//  SaveAttendeeData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SaveAttendeeData.h"
#import "DataConstants.h"

@implementation SaveAttendeeData

@synthesize status;
@synthesize attendeeToSave;
@synthesize savedAttendee;
@synthesize duplicateAttendees, dupAttendee;

-(NSString *)getMsgIdKey
{
	return SAVE_ATTENDEE_DATA;
}

#pragma mark -
#pragma mark Lifecycle

#pragma mark -
#pragma mark Message request
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SaveAttendee",[ExSystem sharedInstance].entitySettings.uri];
	
    NSString* ignoreDupFlag = parameterBag[@"OPTIONS"];
    if ([ignoreDupFlag length])
        self.path = [NSString stringWithFormat:@"%@/%@", self.path, ignoreDupFlag];
    
	self.attendeeToSave = (AttendeeData*)parameterBag[@"ATTENDEE"];
	
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
    
    inDuplicateAttendees = NO;
	return msg;
}

-(NSString *)makeXMLBody
{
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@""];
    __autoreleasing NSMutableString *bodyXMLPrefix = [[NSMutableString alloc] initWithString:@""];
	[bodyXMLPrefix appendString:@"<Attendee>"];
	
	if (attendeeToSave.attnKey != nil)
		[bodyXMLPrefix appendString:[NSString stringWithFormat:@"<AtnKey>%@</AtnKey>", attendeeToSave.attnKey]];

	[bodyXML appendString:@"<Fields xmlns:f='http://schemas.datacontract.org/2004/07/Snowbird'>"];
	
	for (int ix = 0; ix < [attendeeToSave.fieldKeys count]; ix ++)
	{
		NSString *fieldKey = (attendeeToSave.fieldKeys)[ix];
		FormFieldData* ff = (attendeeToSave.fields)[fieldKey];
		
		[bodyXML appendString:@"<f:FormField>"];
		
		if (![self isFieldEmpty:ff.access])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:Access>%@</f:Access>", ff.access]];
		if (![self isFieldEmpty:ff.ctrlType])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:CtrlType>%@</f:CtrlType>", ff.ctrlType]];
		if (![self isFieldEmpty:ff.dataType])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:DataType>%@</f:DataType>", ff.dataType]];
		if (![self isFieldEmpty:ff.ftCode])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:FtCode>%@</f:FtCode>", ff.ftCode]];
		if (![self isFieldEmpty:ff.iD])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:Id>%@</f:Id>", ff.iD]];
		if (![self isFieldEmpty:ff.liCode])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:LiCode>%@</f:LiCode>", [NSString stringByEncodingXmlEntities:ff.liCode]]];
		if (![self isFieldEmpty:ff.liKey])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:LiKey>%@</f:LiKey>", ff.liKey]];
		if (![self isFieldEmpty:ff.listKey])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:ListKey>%@</f:ListKey>", ff.listKey]];
		if (![self isFieldEmpty:ff.parLiKey])
			[bodyXML appendString:[NSString stringWithFormat:@"<f:ParLiKey>%@</f:ParLiKey>", ff.parLiKey]];
		if (![self isFieldEmpty:ff.fieldValue])
        {
            // MOB-10792 - 500 saving attendees. It was sending the un-American number format to the server.
            if ([ff.dataType isEqualToString:@"MONEY"])
            {
                NSString* str = [NSNumberFormatter localizedStringFromNumber:@([ff.fieldValue doubleValue])  numberStyle:NSNumberFormatterDecimalStyle];
                if (str != nil)
                {
                    [bodyXML appendString:[NSString stringWithFormat:@"<f:Value>%@</f:Value>", [NSString stringByEncodingXmlEntities:str]]];
                }
                
            }
            else
            {
                [bodyXML appendString:[NSString stringWithFormat:@"<f:Value>%@</f:Value>", [NSString stringByEncodingXmlEntities:ff.fieldValue]]];
            }
        }
		
		[bodyXML appendString:@"</f:FormField>"];
		
	}
	[bodyXML appendString:@"</Fields>"];
	
	if (attendeeToSave.versionNumber != nil)
		[bodyXML appendString:[NSString stringWithFormat:@"<VersionNumber>%@</VersionNumber>", attendeeToSave.versionNumber]];
	
	[bodyXML appendString:@"</Attendee>"];
    
    // MOB-9693 externalId is always a field, editable or non-editable, save it
    NSString * extId = [self.attendeeToSave getNullableValueForFieldId:@"ExternalId"];
    if (extId != nil)
        [bodyXMLPrefix appendString:[NSString stringWithFormat:@"<ExternalId>%@</ExternalId>", extId]];

    [bodyXMLPrefix appendString:bodyXML]; 
	return bodyXMLPrefix;
}
#pragma mark -
#pragma mark Parsing
- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.status = [[ActionStatus alloc] init];
	}
	else if (!inDuplicateAttendees && [elementName isEqualToString:@"Attendee"])
	{
		self.savedAttendee = [[AttendeeData alloc] init];	
	}
    else if ([elementName isEqualToString:@"DuplicateAttendees"])
    {
        inDuplicateAttendees = YES;
        self.duplicateAttendees = [[NSMutableArray alloc] init];
    }
    else if (inDuplicateAttendees && [elementName isEqualToString:@"Attendee"])
    {
        self.dupAttendee = [[AttendeeData alloc] init];
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
	if ([currentElement isEqualToString:@"Status"])
	{
		self.status.status = string;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.status.errMsg = buildString;
	}
    else if (!inDuplicateAttendees&& savedAttendee != nil)
	{
		if ([currentElement isEqualToString:@"AtnKey"])
		{
			savedAttendee.attnKey = string;
		}
		else if ([currentElement isEqualToString:@"VersionNumber"])
		{
			savedAttendee.versionNumber = string;
		}
	}
    else if (inDuplicateAttendees && self.dupAttendee != nil)
    {
		if ([currentElement isEqualToString:@"AtnKey"])
		{
			dupAttendee.attnKey = string;
		}
		else if ([currentElement isEqualToString:@"VersionNumber"])
		{
			dupAttendee.versionNumber = string;
		}
        else if ([currentElement isEqualToString:@"FirstName"])
        {
            [dupAttendee setFieldId:@"FirstName" value:buildString];
        }
        else if ([currentElement isEqualToString:@"LastName"])
        {
            [dupAttendee setFieldId:@"LastName" value:buildString];
        }
        else if ([currentElement isEqualToString:@"Company"])
        {
            [dupAttendee setFieldId:@"Company" value:buildString];
        }
        else if ([currentElement isEqualToString:@"Title"])
        {
            [dupAttendee setFieldId:@"Title" value:buildString];
        }
        else if ([currentElement isEqualToString:@"AtnTypeName"])
        {
            [dupAttendee setFieldId:@"AtnTypeName" value:buildString];
        }
        else if ([currentElement isEqualToString:@"ExternalId"])
        {
            [dupAttendee setFieldId:@"ExternalId" value:buildString];
        }
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];

    // Save the attnKey and versionNumber, only if there is no duplicates
    if ([elementName isEqualToString:@"ActionStatus"])
    {
        if (attendeeToSave != nil && savedAttendee != nil && 
            (self.duplicateAttendees == nil || [self.duplicateAttendees count] == 0))
        {
            // MOB-8328 Save successful, if new attendee, record its atnKey
            // MOB-9406 Move PrevAtnKey to ExSystem to retain between atn saves.
            if (![attendeeToSave.attnKey length])
                [ExSystem sharedInstance].prevAtnKey = savedAttendee.attnKey;
            attendeeToSave.attnKey = savedAttendee.attnKey;
            attendeeToSave.versionNumber = savedAttendee.versionNumber;
            attendeeToSave.currentVersionNumber = savedAttendee.versionNumber;	// After the attendee is saved, the version number we have is the current one.
        }       
    }
    else if ([elementName isEqualToString:@"Attendee"])
    {
        self.dupAttendee.currentVersionNumber = self.dupAttendee.versionNumber;
        [self.duplicateAttendees addObject:self.dupAttendee];
    }
}

-(BOOL) isFieldEmpty:(NSString*)val
{
	return ![val length];
}

@end
