//
//  DownloadTravelCustomFields.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "DownloadTravelCustomFields.h"
#import "Msg.h"
#import "SystemConfig.h"

@implementation DownloadTravelCustomFields
@synthesize travelCustomFields;
@synthesize field;
@synthesize isAttributeValue;
@synthesize tcfAttribute;
@synthesize tripField;
@synthesize tripFieldAttribute;
@synthesize hasDependency;

-(void) respondToXMLData:(NSData *)data
{
    // MOB-9608 clear entities only if msg succeeds.
    if (!self.isCustomFieldSearch)
        [[TravelCustomFieldsManager sharedInstance] deleteAll];
	[self parseXMLFileAtData:data];
}

-(void) copyTripFieldData:(TravelCustomField *)tcf
{
    self.tripField.attributeId = tcf.attributeId;
    self.tripField.attributeTitle = tcf.attributeTitle;
    self.tripField.dataType = tcf.dataType;
    self.tripField.dependencyAttributeId = @([tcf.dependencyAttributeId integerValue]);
    self.tripField.maxLength = @(tcf.maxLength);
    self.tripField.minLength = @(tcf.minLength);
    self.tripField.required = (tcf.required)?@YES:@NO;
    self.tripField.hasDependency = (tcf.hasDependency)?@YES:@NO;
    if (!self.isCustomFieldSearch)
        self.tripField.attributeValue = tcf.attributeValue;
    self.tripField.displayAtStart = tcf.displayAtStart ? @YES : @NO;
    self.tripField.largeValueCount = ([self.tripField.largeValueCount boolValue] || tcf.largeValueCount) ? @YES : @NO;
}

-(void) copyTripFieldAttributesData:(TravelCustomField *)tcf
{
    BOOL hasUserSelectedValue = NO;
    
    if ([tripField.attributeValue length])
    {
        hasUserSelectedValue = YES;
    }
    
    // Fill in attributes if any
    for (TravelCustomFieldAttributeValue *tcfa in tcf.attributeValues)
    {
        self.tripFieldAttribute = (EntityTravelCustomFieldAttribute *)[[TravelCustomFieldsManager sharedInstance] fetchOrMakeAttribute:tcfa.valueId];
        
        self.tripFieldAttribute.attributeId = tcfa.attributeId;
        self.tripFieldAttribute.optionText = tcfa.optionText;
        self.tripFieldAttribute.value = tcfa.value;
        self.tripFieldAttribute.valueId = tcfa.valueId;
        self.tripFieldAttribute.sequence = @(tcfa.sequence);
        
        [self.tripField addRelAttributeObject:self.tripFieldAttribute];
        self.tripFieldAttribute = nil;
        
        // Set the display value for this custom field
        if (hasUserSelectedValue && [tripField.attributeValue isEqualToString:tcfa.value])
            self.tripField.selectedAttributeOptionText = [tcfa.optionText lengthIgnoreWhitespace] ? tcfa.optionText: tcfa.value;
    }
}

-(void) persistTravelCustomFields
{
    // Save to persistence store - updates custom fields if already present
    for (TravelCustomField *tcf in travelCustomFields)
    {
        self.tripField = (EntityTravelCustomFields *)[[TravelCustomFieldsManager sharedInstance] fetchOrMake:tcf.attributeId];
        
        [self copyTripFieldData:tcf];
        
        if (tcf.attributeValues != nil && [tcf.attributeValues count] > 0)
            [self copyTripFieldAttributesData:tcf];
        else if ([tripField.attributeValue length])
            self.tripField.selectedAttributeOptionText = tripField.attributeValue;
        
        [[TravelCustomFieldsManager sharedInstance] saveIt:self.tripField];
        self.tripField = nil;
    }
}

#pragma mark -
#pragma mark Message Init

-(void)clearCachedData
{
    self.hasDependency = NO;
}

-(id)init
{
	self = [super init];
    if (self)
    {
        self.travelCustomFields = [[NSMutableArray alloc] init];
    
        [self clearCachedData];
    }
	return self;
}


- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    Msg* msg = nil;
    NSString *customFieldsData = parameterBag[@"UPDATED_CUSTOM_FIELDS"];
    
    if ([customFieldsData lengthIgnoreWhitespace])
    {
        self.path = [NSString stringWithFormat:@"%@/mobile/Config/UpdateTravelCustomFields", [ExSystem sharedInstance].entitySettings.uri];
        
        msg = [[Msg alloc] initWithData:DOWNLOAD_TRAVEL_CUSTOMFIELDS State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
        [msg setHeader:[ExSystem sharedInstance].sessionID];
        [msg setContentType:@"application/xml"];
        [msg setMethod:@"POST"];
        [msg setBody:customFieldsData];
    }
    else if (parameterBag[@"ATTRIBUTE_ID"])
    {
        self.isCustomFieldSearch = YES;
        self.path = [NSString stringWithFormat:@"%@/Mobile/Config/SearchTravelCustomFieldValues", [ExSystem sharedInstance].entitySettings.uri];
        
        msg = [[Msg alloc] initWithData:DOWNLOAD_TRAVEL_CUSTOMFIELDS State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
        [msg setHeader:[ExSystem sharedInstance].sessionID];
        [msg setContentType:@"application/xml"];
        [msg setMethod:@"POST"];
        NSString *body = [NSString stringWithFormat:@"<SearchTravelCustomField><AttributeId>%@</AttributeId><SearchPattern>%@</SearchPattern></SearchTravelCustomField>",parameterBag[@"ATTRIBUTE_ID"],[NSString stringByEncodingXmlEntities:parameterBag[@"SEARCH_TEXT"]]];
        [msg setBody:body];
    }
    else
    {
        self.path = [NSString stringWithFormat:@"%@/mobile/Config/TravelCustomFields", [ExSystem sharedInstance].entitySettings.uri];
        
        msg = [[Msg alloc] initWithData:DOWNLOAD_TRAVEL_CUSTOMFIELDS State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
        [msg setMethod:@"GET"];
    }

	return msg;
}

- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
    [super parserDidStartDocument:parser];
}

- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
    [super parser:parser parseErrorOccurred:parseError];
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
    if ([elementName isEqualToString:@"Field"] || [elementName isEqualToString:@"TravelCustomFieldSearch"])
	{
		self.field = [[TravelCustomField alloc] init];
	}
    else if ([elementName isEqualToString:@"Values"])
    {
        field.attributeValues = [[NSMutableArray alloc] init];
    }
    else if ([elementName isEqualToString:@"AttributeValue"])
    {
        isAttributeValue = TRUE;
        self.tcfAttribute = [[TravelCustomFieldAttributeValue alloc] init];
        tcfAttribute.sequence = 0;
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
    if ([elementName isEqualToString:@"Field"])
	{
        [self.travelCustomFields addObject:field];
		self.field = nil;
	}
    else if ([elementName isEqualToString:@"TravelCustomFieldSearch"])
    {
        [self.travelCustomFields addObject:field];
    }
    else if ([elementName isEqualToString:@"AttributeValue"])
    {
        isAttributeValue = FALSE;
        [field.attributeValues addObject:tcfAttribute];
        self.tcfAttribute = nil;
    }
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"DisplayAtStart"])
    {
        field.displayAtStart = ([[buildString lowercaseString] isEqualToString:@"true"])? YES : NO;
    }
    else if ([currentElement isEqualToString:@"HasDependency"])
    {
        field.hasDependency = ([[buildString lowercaseString] isEqualToString:@"true"])? YES : NO;
    }
    else if ([currentElement isEqualToString:@"AttributeId"])
    {
        if (isAttributeValue)
        {
            tcfAttribute.attributeId = buildString;
        }
        else 
        {
            field.attributeId = buildString;
        }
    }
    else if ([currentElement isEqualToString:@"AttributeValue"])
    {
        field.attributeValue = buildString;
    }
    else if ([currentElement isEqualToString:@"CurrentValue"])
    {
        field.attributeValue = buildString;
    }
    else if ([currentElement isEqualToString:@"AttributeTitle"])
    {
        field.attributeTitle = buildString;
    }
    else if ([currentElement isEqualToString:@"DataType"])
    {
        field.dataType = buildString;
    }
    else if ([currentElement isEqualToString:@"DependencyAttributeId"])
    {
        field.dependencyAttributeId = buildString;
    }
    else if ([currentElement isEqualToString:@"MaxLength"])
    {
        field.maxLength = [buildString integerValue];
    }
    else if ([currentElement isEqualToString:@"MinLength"])
    {
        field.minLength = [buildString integerValue];
    }
    else if ([currentElement isEqualToString:@"Required"])
    {
        field.required = ([[buildString lowercaseString] isEqualToString:@"true"])? YES : NO;
    }
    else if ([currentElement isEqualToString:@"LargeValueCount"])
    {
        field.largeValueCount = @([buildString boolValue]);
    }
    else if ([currentElement isEqualToString:@"OptionText"])
    {
        tcfAttribute.optionText = buildString;
    }
    else if ([currentElement isEqualToString:@"Value"])
    {
        tcfAttribute.value = buildString;
    }
    else if ([currentElement isEqualToString:@"ValueId"])
    {
        tcfAttribute.valueId = buildString;
    }
    else if ([currentElement isEqualToString:@"Sequence"])
    {
        tcfAttribute.sequence = [buildString integerValue];
    }
}

- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
    [super parserDidEndDocument:parser];
    [self persistTravelCustomFields];    
}

@end

