//
//  ExpenseTypesData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ExpenseTypesData.h"


@implementation ExpenseTypesData

@synthesize path, version, polKey, currentElement, ets, et, keys;
@synthesize buildString;

static NSMutableDictionary* expTypeXmlToPropertyMap = nil;

+ (NSMutableDictionary*) getExpTypeXmlToPropertyMap
{
	return expTypeXmlToPropertyMap;
}

+ (void)initialize
{
	if (self == [ExpenseTypesData class]) 
	{
        // Perform initialization here.
		expTypeXmlToPropertyMap = [[NSMutableDictionary alloc] init];
		expTypeXmlToPropertyMap[@"ExpCode"] = @"ExpCode";
		expTypeXmlToPropertyMap[@"ExpKey"] = @"ExpKey";
		expTypeXmlToPropertyMap[@"ExpName"] = @"ExpName";
		expTypeXmlToPropertyMap[@"ParentExpName"] = @"ParentExpName";
		expTypeXmlToPropertyMap[@"ParentExpKey"] = @"ParentExpKey";
		expTypeXmlToPropertyMap[@"FormKey"] = @"FormKey";
		expTypeXmlToPropertyMap[@"ItemizeFormKey"] = @"ItemizeFormKey";
		expTypeXmlToPropertyMap[@"Access"] = @"Access";
		expTypeXmlToPropertyMap[@"VendorListKey"] = @"VendorListKey";
		expTypeXmlToPropertyMap[@"SupportsAttendees"] = @"SupportsAttendees";
		expTypeXmlToPropertyMap[@"UserAsAtnDefault"] = @"UserAsAtnDefault";
		expTypeXmlToPropertyMap[@"ItemizeType"] = @"ItemizeType";
		expTypeXmlToPropertyMap[@"ItemizeStyle"] = @"ItemizeStyle";
        expTypeXmlToPropertyMap[@"ItemizationUnallowExpKeys"] = @"ItemizationUnallowExpKeys";
        expTypeXmlToPropertyMap[@"UnallowAtnTypeKeys"] = @"UnallowAtnTypeKeys";
        expTypeXmlToPropertyMap[@"DisplayAtnAmounts"] = @"DisplayAtnAmounts";
        expTypeXmlToPropertyMap[@"DisplayAddAtnOnForm"] = @"DisplayAddAtnOnForm";
        expTypeXmlToPropertyMap[@"AllowEditAtnAmt"] = @"AllowEditAtnAmt";
        expTypeXmlToPropertyMap[@"AllowEditAtnCount"] = @"AllowEditAtnCount";
        expTypeXmlToPropertyMap[@"AllowNoShows"] = @"AllowNoShows";
        // This is rather opaque.  This maps xml node values to object values.  It uses selectors to do so.
        // The name on the left is the variable name, however the first char must be capitalized as the generated setter is setVarName for varName.
        expTypeXmlToPropertyMap[@"HasPostAmtCalc"] = @"HasPostAmountCalculation";
        expTypeXmlToPropertyMap[@"HasTaxForm"] = @"HasTaxForm";
	}
}


//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	self.ets = [[NSMutableDictionary alloc] initWithObjectsAndKeys:nil];  // Inc retain count by 2
	
	self.keys = [[NSMutableArray alloc] initWithObjects:nil];  // Inc retain count by 2

	
	NSXMLParser* dataParser = [[NSXMLParser alloc] initWithData:webData];
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
	return EXPENSE_TYPES_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message

	self.version = parameterBag[@"VERSION"];
	self.polKey = parameterBag[@"POL_KEY"];
	if (polKey == nil)
		self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/GetExpenseTypes%@",[ExSystem sharedInstance].entitySettings.uri, version];
	else {
		self.path = [NSString stringWithFormat:@"%@/Mobile/Expense/GetExpenseTypes%@/%@",[ExSystem sharedInstance].entitySettings.uri, version, polKey];
	}
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}




-(void) flushData
{
	//	if (keys != nil) 
	//	{
	//		[keys release];
	//	}
	//	
	//	if (oopes != nil) 
	//	{
	//		[oopes release];
	//	}
	
}



- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	////NSLog(@"found file and started parsing");
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
	//	NSString * errorString = [NSString stringWithFormat:@"Parser Error (Error code %i )", [parseError code]];
	//	////NSLog(@"error parsing XML: %@", errorString);
	//	
	//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error Parsing Content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
	//	[errorAlert show];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict{
	
	self.currentElement = elementName;
	
	isInElement = @"YES";
	
	self.buildString = [[NSMutableString alloc] init];
	
	if ([elementName isEqualToString:@"ExpenseType"])
	{
		self.et = [[ExpenseTypeData alloc] init];  // Inc retain count by 2

	}
	else if ([elementName isEqualToString:@"ExpName"] || [elementName isEqualToString:@"ParentExpName"] )
	{
		self.buildString = [[NSMutableString alloc] init];

	}
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	isInElement = @"NO";
	
	if ([elementName isEqualToString:@"ExpenseType"])
	{
		if (ets != nil && keys != nil && et != nil)
		{
			ets[et.expKey] = et;
			[keys addObject:et.expKey];
		}
		self.et = nil;
	}
}



- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
	//NSLog(@"Ahh SHUCKS");
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[buildString appendString:string];
	
	if (et == nil)
		return;
	
	NSString* propName = expTypeXmlToPropertyMap[currentElement];
	if (propName != nil)
	{
		[et setValue:buildString forKey:propName];
	}
}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];
}


@end
