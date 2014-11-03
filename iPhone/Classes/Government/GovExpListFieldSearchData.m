//
//  GovExpListFieldSearchData.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovExpListFieldSearchData.h"

@implementation GovExpListFieldSearchData
@synthesize docType, expDescrip;
@synthesize li, field;

-(NSString *)getMsgIdKey
{
    return GOV_EXP_LIST_FIELD_SEARCH_DATA;
}

-(NSString *)makeXMLBody
{
    __autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<SearchExpenseListFieldRequest>"];
    if (![super isFieldEmpty:(NSString*)docType])
        [bodyXML appendString:[NSString stringWithFormat:@"<docType>%@</docType>", self.docType]];
    if (![super isFieldEmpty:(NSString*)expDescrip])
        [bodyXML appendString:[NSString stringWithFormat:@"<expenseDescription>%@</expenseDescription>", [NSString stringByEncodingXmlEntities:self.expDescrip]]];
    if (![super isFieldEmpty:(NSString*)fieldId])
        [bodyXML appendString:[NSString stringWithFormat:@"<fieldId>%@</fieldId>", self.fieldId]];
    if (![super isFieldEmpty:(NSString*)query])
        [bodyXML appendString:[NSString stringWithFormat:@"<query>%@</query>", [NSString stringByEncodingXmlEntities:self.query]]];
    [bodyXML appendString:@"</SearchExpenseListFieldRequest>"];
    
    return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.field = [parameterBag objectForKey:@"FIELD"];
    if (field.listKey == nil) {
        self.fieldId = field.iD;
    }
    else
        self.listKey = field.listKey;
    
    self.query = [parameterBag objectForKey:@"QUERY"];
    self.docType = [parameterBag objectForKey:@"DOCTYPE"];
    self.expDescrip = [parameterBag objectForKey:@"EXPTYPE"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/SearchExpenseListField", [ExSystem sharedInstance].entitySettings.uri];
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
    
    if ([currentElement isEqualToString:@"TMFormField"])
    {
        if (self.field.searchableListChoices == nil)
        {
            self.field.searchableListChoices = [[NSMutableArray alloc] init];
        }
        else
            [self.field.searchableListChoices removeAllObjects];
    }
    if ([elementName isEqualToString:@"TMFormFieldDropDownOptions"])
    {
        self.li = [[ListItem alloc] init];
    }
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    if ([elementName isEqualToString:@"TMFormFieldDropDownOptions"])
    {
        [self.field.searchableListChoices addObject:li];
    }
}

-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];

    //NSString* propName = [xmlToPropertyMap objectForKey:currentElement];
    if ([currentElement isEqualToString:@"Description"])
    {
        if (li != nil)
        {
            [li setLiName:buildString];
            [li setLiKey:buildString];
        }
    }
}
@end
