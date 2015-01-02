//
//  AttendeeFullSearchData.m
//  ConcurMobile
//
//  Created by yiwen on 10/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AttendeeFullSearchData.h"
#import "AttendeeSearchData.h"

@implementation AttendeeFullSearchData
@synthesize atnTypeKey, searchFields, expKey, polKey, rpeKey;
@synthesize attendeeExclusionList;

-(NSString *)getMsgIdKey
{
	return ATTENDEE_FULL_SEARCH_DATA;
}

#pragma mark -
#pragma mark Lifecycle

#pragma mark -
#pragma mark Message request
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{

    
    self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SearchAttendeesExtendedV2",[ExSystem sharedInstance].entitySettings.uri];
    
	self.searchFields = parameterBag[@"FIELDS"];
	self.atnTypeKey = parameterBag[@"ATN_TYPE_KEY"];
	self.rpeKey = parameterBag[@"RPE_KEY"];
	self.expKey = parameterBag[@"EXP_KEY"];
	self.polKey = parameterBag[@"POL_KEY"];
	self.attendeeExclusionList = parameterBag[@"EXCLUSION_LIST"];

	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	return msg;
}

-(NSString *)makeXMLBody
{
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@""];
	[bodyXML appendString:@"<AttendeeExtendedSearchCriteria xmlns=\"\">"];
	[bodyXML appendString:[NSString stringWithFormat:@"<AtnTypeKey>%@</AtnTypeKey>", self.atnTypeKey]];
	
	[bodyXML appendString:@"<Fields xmlns:a=\"http://schemas.datacontract.org/2004/07/Snowbird\">"];
	
	for (int ix = 0; ix < [self.searchFields count]; ix ++)
	{
		FormFieldData* ff = (self.searchFields)[ix];
		[bodyXML appendString:@"<a:FormField>"];
		
		if ([ff.iD length])
			[bodyXML appendString:[NSString stringWithFormat:@"<a:Id>%@</a:Id>", ff.iD]];
		
        if ([ff.liKey length])
			[bodyXML appendString:[NSString stringWithFormat:@"<a:Value>%@</a:Value>", ff.liKey]];
        else if ([ff.fieldValue length])
		{
			[bodyXML appendString:[NSString stringWithFormat:@"<a:Value>%@</a:Value>", [NSString stringByEncodingXmlEntities:[ff getServerValue]]]];
		}
		[bodyXML appendString:@"</a:FormField>"];
	}
	[bodyXML appendString:@"</Fields>"];
    [bodyXML appendString:[NSString stringWithFormat:@"<ExcludeAtnKeys>%@</ExcludeAtnKeys>",
								  [AttendeeSearchData makeExclusionXml:self.attendeeExclusionList]]];

	[bodyXML appendString:[NSString stringWithFormat:@"<ExpKey>%@</ExpKey>", self.expKey]];
    
	[bodyXML appendString:[NSString stringWithFormat:@"<PolKey>%@</PolKey>", self.polKey]];
    if ([self.rpeKey length])
        [bodyXML appendString:[NSString stringWithFormat:@"<RpeKey>%@</RpeKey>", self.rpeKey]];
	[bodyXML appendString:@"</AttendeeExtendedSearchCriteria>"];
	
	return bodyXML;
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    // MOB-8498 Set all fields to RO and currentVersionNumber to versionNumber, so clients can view these
    // The attendee detail screen will fetch form and take care of RW/RO access.
    [super parserDidEndDocument:parser];
    
    for (AttendeeData* atn in self.attendees)
    {
        if (atn != nil && atn.fieldKeys != nil && [atn.fieldKeys count]>0)
        {
            for (NSString* atnKey in atn.fieldKeys)
            {
                FormFieldData* fld = (atn.fields)[atnKey];
                if (fld.access == nil)
                    fld.access = @"RO";
            }
        }
        if (atn.currentVersionNumber == nil)
        {
            atn.currentVersionNumber = atn.versionNumber;
        }
    }
}


@end
