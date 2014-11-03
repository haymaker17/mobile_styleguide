//
//  AmtrakSell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AmtrakSell.h"
#import "Config.h"


@implementation AmtrakSell

@synthesize path, currentElement, items, obj, keys;

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	self.items = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];
    self.keys = [[NSMutableArray alloc] initWithObjects:nil];
	
	dataParser = [[NSXMLParser alloc] initWithData:webData];
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
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
        isInElement = @"NO";
        currentElement = @"";
        [self flushData];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return AMTRAK_SELL;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	
//	<RailSell>
//	<Bucket>1-1-1-1</Bucket>
//	<CreditCardId>694339</CreditCardId>
//	<DeliveryOption>TBM</DeliveryOption>
//	<GroupId>2V-NFK-BAL-BAL-NFK-6094-94-99-6099</GroupId>
//	</RailSell>
    
    NSString *customFields = parameterBag[@"TRAVEL_CUSTOM_FIELDS"];
	
	NSString *groupId = parameterBag[@"GROUP_ID"];
	NSString *bucket = parameterBag[@"BUCKET"];
	
	NSString *ccId = parameterBag[@"CREDIT_CARD_ID"];
	NSString *deliveryOption = parameterBag[@"DELIVERY_OPTION"];
    
    NSString *violationReasonCode = parameterBag[@"VIOLATION_REASON_CODE"];
    NSString *violationJustification = parameterBag[@"VIOLATION_JUSTIFICATION"];
    
    NSMutableString *USGovtPerDiemLocation = nil;

    if([Config isGov])
    {
        NSString *perDiemLocation = [parameterBag objectForKey:@"PER_DIEM_LOCATION"];
        NSString *perDiemLocState = [parameterBag objectForKey:@"GOV_PER_DIEM_LOC_STATE"];
        NSString *perDiemLocZipCode = [parameterBag objectForKey:@"GOV_PER_DIEM_LOC_ZIP"];
        NSString *perDiemLocCountry = [parameterBag objectForKey:@"GOV_PER_DIEM_COUNTRY"];
        
        if (perDiemLocCountry != nil && perDiemLocation != nil)
        {
            USGovtPerDiemLocation = [[NSMutableString alloc] initWithString:@"<USGovtPerDiemLocation>"];
            [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<Country>%@</Country>", [NSString stringByEncodingXmlEntities:perDiemLocCountry]]];
            [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<Name>%@</Name>", [NSString stringByEncodingXmlEntities:perDiemLocation]]];
            if (perDiemLocState != nil)
                [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<State>%@</State>", [NSString stringByEncodingXmlEntities:perDiemLocState]]];
            else
                [USGovtPerDiemLocation appendString:@"<State/>"];
            
            if (perDiemLocZipCode != nil)
                [USGovtPerDiemLocation appendString:[NSString stringWithFormat:@"<ZipCode>%@</ZipCode>", [NSString stringByEncodingXmlEntities:perDiemLocZipCode]]];
            else
                [USGovtPerDiemLocation appendString:@"<ZipCode/>"];
            [USGovtPerDiemLocation appendString:@"</USGovtPerDiemLocation>"];
        }
    }
    
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<RailSell>"];
	[bodyXML appendString:@"<Bucket>%@</Bucket>"];
	[bodyXML appendString:@"<CreditCardId>%@</CreditCardId>"];
    
    if (customFields != nil) 
        [bodyXML appendString:customFields];
    
	[bodyXML appendString:@"<DeliveryOption>%@</DeliveryOption>"];
    
    NSString *existingTANumber = parameterBag[@"EXISTING_TA_NUMBER"];
    NSString *perdiemLocationID = parameterBag[@"PER_DIEM_LOCATION_ID"];
    if ([existingTANumber length])
        [bodyXML appendString:[NSString stringWithFormat:@"<ExistingTANumber>%@</ExistingTANumber>", [NSString stringByEncodingXmlEntities:existingTANumber]]];
    
	[bodyXML appendString:@"<GroupId>%@</GroupId>"];

    if ([perdiemLocationID length])
        [bodyXML appendString:[NSString stringWithFormat:@"<PerdiemLocationID>%@</PerdiemLocationID>", [NSString stringByEncodingXmlEntities:perdiemLocationID]]];
	
    if (USGovtPerDiemLocation != nil)
        [bodyXML appendString:USGovtPerDiemLocation];
    
    if (violationReasonCode != nil || violationJustification != nil)
    {
        [bodyXML appendString:@"<Violations>"];
       
        //TODO:
        //RuleValueID
        
        if (violationReasonCode != nil && [violationReasonCode length] > 0)
            [bodyXML appendString:[NSString stringWithFormat:@"<ViolationCode>%@</ViolationCode>", violationReasonCode]];
        
        if (violationJustification != nil && [violationJustification length] > 0)
            [bodyXML appendString:[NSString stringWithFormat:@"<ViolationJustification>%@</ViolationJustification>", [NSString stringByEncodingXmlEntities:violationJustification]]];
        
        [bodyXML appendString:@"</Violations>"];
    }
    
	[bodyXML appendString:@"</RailSell>"];
	
	
	NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML,
						bucket, ccId, deliveryOption, groupId];
	
	//NSLog(@"%@", formattedBodyXml);
	return formattedBodyXml;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	//NSString *vendorCode = @"2V"; //amtrak?
	self.path = [NSString stringWithFormat:@"%@/Mobile/Rail/AmtrakSell",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	
	return msg;
}




-(void) flushData
{
	
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
	
	if ([elementName isEqualToString:@"RailSellResponse"])
	{		
		self.obj = [[AmtrakSellData alloc] init];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"RailSellResponse"])
	{
		if (obj.tripLocator != nil) 
		{
			items[obj.tripLocator] = obj;
			[keys addObject:obj.tripLocator];
		}
	}
}


- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	//NSLog(@"element = %@, string = %@", currentElement, string);
	if ([currentElement isEqualToString:@"Status"])
	{
		[obj setSellStatus:string];
	}
	else if ([currentElement isEqualToString:@"TripLocator"])
	{
		[obj setTripLocator:string];
	}
    else if ([currentElement isEqualToString:@"AuthorizationNumber"])
    {
        obj.authorizationNumber = string;
    }
    else if ([currentElement isEqualToString:@"ItinLocator"])
    {
        obj.itinLocator = string;
    }
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end
