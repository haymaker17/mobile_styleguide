//
//  TrainDeliveryData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "TrainDeliveryData.h"

@interface TrainDeliveryData()

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) DeliveryData				*obj;

@end


@implementation TrainDeliveryData

//	//POST /Mobile/Rail/AmtrakGetDeliveryOptions 
//	X-SessionID: 1053B08C-2986-4352-99FD-57DCB175E5DA
//	<RailGetDeliveryOptions>
//	<Bucket>1-1-1-1</Bucket>
//	<GroupId>2V-NFK-BAL-BAL-NFK-6094-94-99-6099</GroupId>
//	</RailGetDeliveryOptions>

//	<RailGetDeliveryOptionsResponse xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
//	<Status xmlns="http://schemas.datacontract.org/2004/07/Snowbird">SUCCESS</Status>
//	<DeliveryOptions>
//	<TicketDeliveryOption>
//	<Fee>0</Fee>
//	<Name>Mail</Name>
//	<Type>TBM</Type>
//	</TicketDeliveryOption>
//	<TicketDeliveryOption>
//	<Fee>15</Fee>
//	<Name>Express Delivery</Name>
//	<Type>TBX</Type>
//	</TicketDeliveryOption>
//	</DeliveryOptions>
//	</RailGetDeliveryOptionsResponse>

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
	return TRAIN_DELIVERY;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	NSString *groupId = parameterBag[@"GROUP_ID"];
	NSString *bucket = parameterBag[@"BUCKET"];
	
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<RailGetDeliveryOptions>"];
	[bodyXML appendString:@"<Bucket>%@</Bucket>"];
	[bodyXML appendString:@"<GroupId>%@</GroupId>"];
	
	[bodyXML appendString:@"</RailGetDeliveryOptions>"];
	
	
	__autoreleasing NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
							bucket, 
							groupId
							];
		
	return formattedBodyXml;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	//NSString *vendorCode = @"2V"; //amtrak?
	self.path = [NSString stringWithFormat:@"%@/Mobile/Rail/AmtrakGetDeliveryOptions",[ExSystem sharedInstance].entitySettings.uri];
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
	
	if ([elementName isEqualToString:@"TicketDeliveryOption"])
	{		
		self.obj = [[DeliveryData alloc] init];
	}
	
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"TicketDeliveryOption"])
	{
		if (obj.type != nil) 
		{
			items[obj.type] = obj;
			[keys addObject:obj.type];
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
	if ([currentElement isEqualToString:@"Fee"])
	{
		[obj setFee:string];
	}
	else if ([currentElement isEqualToString:@"Name"])
	{
		[obj setName:string];
	}
	else if ([currentElement isEqualToString:@"Type"])
	{
		[obj setType:string];
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end
