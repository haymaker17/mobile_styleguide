//
//  GovExpenseFormData.m
//  ConcurMobile
//
//  Created by ernest cho on 9/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovExpenseFormData.h"
#import "Msg.h"
#import "DateTimeFormatter.h"
#import "FormatUtils.h"

@implementation GovExpenseFormData


@synthesize formData;
@synthesize field;
@synthesize li;
@synthesize otherFormAttributes;
@synthesize docType, expDescrip;

@synthesize inDropDownList;

-(id)init
{
	self = [super init];
    if (self)
    {
        
    }
	return self;
}

-(NSString *)getMsgIdKey
{
    return GOV_EXPENSE_FORM_DATA;
}

-(NSString *)makeXMLBody
{//knows how to make a post
    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<GetTMExpenseFormRequest>"];
    [bodyXML appendString:@"<docType>%@</docType>"];
	[bodyXML appendString:@"<expenseDescription>%@</expenseDescription>"];
    [bodyXML appendString:@"</GetTMExpenseFormRequest>"];

    NSString* formattedBodyXml = nil;
	
	formattedBodyXml = [NSString stringWithFormat:bodyXML, [NSString stringByEncodingXmlEntities:self.docType],[NSString stringByEncodingXmlEntities:self.expDescrip]];
    
	return formattedBodyXml;
}


-(Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.docType = parameterBag[@"DOC_TYPE"];
    self.expDescrip = parameterBag[@"EXP_DESCRIP"];
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/GetTMExpenseForm", [ExSystem sharedInstance].entitySettings.uri];
    
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody]];

	return msg;
}

-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
    self.currentElement = elementName;
    if ([elementName isEqualToString:@"GetTMExpenseFormResponse"])
    {
        self.formData = [[NSMutableArray alloc] init];
        self.otherFormAttributes = [[NSMutableDictionary alloc] init];
        (self.otherFormAttributes)[@"docType"] = @"VCH";              //default docType, if TM won't return a docType
    }
    else if ([elementName isEqualToString:@"TMFormField"])
    {
        self.field = [[FormFieldData alloc] init];
    }
    else if ([elementName isEqualToString:@"TMFormFieldDropDownOptions"])
    {
        self.field.listChoices = [[NSMutableArray alloc] init];
    }
    else if ([elementName isEqualToString:@"TMFormFieldDropDownOption"])
    {
        self.li = [[ListItem alloc] init];
        self.inDropDownList = YES;
    }
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"TMFormField"])
    {
        [self.formData addObject:self.field];
    }
    else if ([elementName isEqualToString:@"TMFormFieldDropDownOptions"])
    {
        self.inDropDownList = NO;
    }
    else if ([elementName isEqualToString:@"TMFormFieldDropDownOption"])
    {
        [self.field.listChoices addObject:li];
    }
}

-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"CtrlType"])
    {
        [self.field setCtrlType:buildString];
    }
    else if ([currentElement isEqualToString:@"DataType"])
    {
        [self.field setDataType:buildString];
    }
    else if ([currentElement isEqualToString:@"Id"])
    {
        [self.field setID:buildString];
    }
    else if ([currentElement isEqualToString:@"Label"])
    {
        [self.field setLabel:buildString];
    }
    else if ([currentElement isEqualToString:@"Required"])
    {
        [self.field setRequired:buildString];
    }
    else if ([currentElement isEqualToString:@"Value"])
    {
        [self.field setFieldValue:buildString];
    }
    else if ([currentElement isEqualToString:@"Searchable"])
    {
        [self.field setSearchable:buildString];
    }
    else if(inDropDownList)
    {
        // parse for inline dropdown options.
        [self fillListItem:buildString];
    }
    else if([currentElement isEqualToString:@"acclabel"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"description"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"docType"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"mode"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"org"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"sublabel"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"userId"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
    else if([currentElement isEqualToString:@"vchnum"])
    {
        (self.otherFormAttributes)[currentElement] = buildString;
    }
}

-(void)fillListItem:(NSString *) string
{
    if ([currentElement isEqualToString:@"Description"])
    {
        [li setLiName:string];
        [li setLiKey:string];
    }
    else if ([currentElement isEqualToString:@"Name"])
    {
        //response not used
    }
    else if ([currentElement isEqualToString:@"TabValue"])
    {
        //response not used
    }
}
@end
