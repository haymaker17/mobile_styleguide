//
//  AttendeeSearchData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AttendeeSearchData.h"
#import "Msg.h"

@implementation AttendeeSearchData

@synthesize query;
@synthesize attendeeExclusionList;

-(NSString *)getMsgIdKey
{
	return ATTENDEE_SEARCH_DATA;
}

#pragma mark -
#pragma mark Lifecycle
-(id)init
{
	self = [super init];
    if (self)
    {
        self.query = @"";
    }
	return self;
}


#pragma mark -
#pragma mark Message request
- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SearchAttendeesV2",[ExSystem sharedInstance].entitySettings.uri];
	
	self.query = parameterBag[@"QUERY"];
	if (self.query == nil)
	{
		self.query = @"";
	}
	
	self.attendeeExclusionList = parameterBag[@"EXCLUSION_LIST"];
	
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];
	return msg;
}

-(NSString *)makeXMLBody
{
// MOB-7816 the following line does not work after porting MWS from calling GetClientData.asp to Expense DotNet
//	NSString* queryValue = [NSString stringWithFormat:@"%@&FetchGroups=N", query];
	NSString* escapedQueryValue= [NSString stringByEncodingXmlEntities:query];
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@""];
	[bodyXML appendString:@"<AttendeeSearchCriteria>"];
	[bodyXML appendString:@"<ExcludeAtnKeys>%@</ExcludeAtnKeys>"];
	[bodyXML appendString:@"<Query>%@</Query>"];
	[bodyXML appendString:@"</AttendeeSearchCriteria>"];
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
								  [AttendeeSearchData makeExclusionXml:self.attendeeExclusionList],
								  escapedQueryValue
								  ];
	
	
	return formattedBodyXml;
}

+(NSString *)makeExclusionXml:(NSArray*) atnExclusionList
{
	__autoreleasing NSMutableString *xml = [[NSMutableString alloc] init];
	
	if (atnExclusionList != nil)
	{
		for (AttendeeData* attendee in atnExclusionList)
		{
			if (attendee.attnKey != nil && [attendee.attnKey length] > 0)
			{
				if ([xml length] > 0)
				{
					[xml appendString:@","];
				}
				[xml appendString:attendee.attnKey];
			}
		}
	}
	
	return xml;
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    // MOB-8498 Set all fields to RO and currentVersionNumber to versionNumber, so clients can view these
    // The attendee detail screen will fetch form and take care of RW/RO access.
    [super parserDidEndDocument:parser];
    
    for (NSObject* obj in self.attendees)
    {
        if ([obj isKindOfClass:[AttendeeData class]])
        {
            AttendeeData* atn = (AttendeeData*) obj;
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
}

@end
