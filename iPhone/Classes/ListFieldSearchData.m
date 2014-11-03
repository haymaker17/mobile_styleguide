//
//  ListFieldSearchData.m
//  ConcurMobile
//
//  Created by yiwen on 11/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ListFieldSearchData.h"
#import "DataConstants.h"
#import "FormFieldData.h"

@implementation ListFieldSearchData


@synthesize fieldId, ftCode, isMru, listKey, parentLiKey, query, rptKey, listItems, item, extraFieldId;
@synthesize searchBy;

static NSMutableDictionary* xmlToPropertyMap = nil;

// Initialize msgId to msg class mapping here
+ (void)initialize
{
    if (self == [ListFieldSearchData class])
    {
        // Perform initialization here.
        xmlToPropertyMap = [[NSMutableDictionary alloc] init];
        xmlToPropertyMap[@"FieldId"] = @"FieldId";
        xmlToPropertyMap[@"FtCode"] = @"FtCode";
        xmlToPropertyMap[@"IsMru"] = @"IsMru";
        xmlToPropertyMap[@"ListKey"] = @"ListKey";
        xmlToPropertyMap[@"ParentLiKey"] = @"ParentLiKey";
        xmlToPropertyMap[@"Query"] = @"Query";
        xmlToPropertyMap[@"Key"] = @"LiKey";
        xmlToPropertyMap[@"Code"] = @"LiCode";
        xmlToPropertyMap[@"Text"] = @"LiName";
        xmlToPropertyMap[@"External"] = @"External";
    }
}

// returns first ListItem with LiKey
- (ListItem *)getListItemWithLiKey:(NSString *)liKey
{
    for (ListItem *tmp in self.listItems) {
        if ([liKey isEqualToString:tmp.liKey]) {
            return tmp;
        }
    }
    return nil;
}

// returns first ListItem with liCode
- (ListItem *)getListItemWithLiCode:(NSString *)liCode
{
    for (ListItem *tmp in self.listItems)
    {
        if ([liCode isEqualToString:tmp.liCode])
        {
            return tmp;
        }
    }
    return nil;
}

-(NSString *)getMsgIdKey
{
	return LIST_FIELD_SEARCH_DATA;
}

-(void) flushData
{
    [super flushData];
    self.listItems = [[NSMutableArray alloc] init];
    self.item = nil;
    self.extraFieldId = nil;
}


-(BOOL) isFieldEmpty:(NSString*)val
{
    return ![val length];
}

-(NSString *)makeXMLBody
{//knows how to make a post
    __autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<ListSearchCriteria>"];
    if (![self isFieldEmpty:fieldId])
        [bodyXML appendString:[NSString stringWithFormat:@"<FieldId>%@</FieldId>", self.fieldId]];
    if (![self isFieldEmpty:ftCode])
        [bodyXML appendString:[NSString stringWithFormat:@"<FtCode>%@</FtCode>", self.ftCode]];
    if (![self isFieldEmpty:isMru])
        [bodyXML appendString:[NSString stringWithFormat:@"<IsMru>%@</IsMru>", self.isMru]];
    if (![self isFieldEmpty:listKey])
        [bodyXML appendString:[NSString stringWithFormat:@"<ListKey>%@</ListKey>", self.listKey]];
    if (![self isFieldEmpty:parentLiKey])
        [bodyXML appendString:[NSString stringWithFormat:@"<ParentLiKey>%@</ParentLiKey>", self.parentLiKey]];
    if (![self isFieldEmpty:query])
        [bodyXML appendString:[NSString stringWithFormat:@"<Query>%@</Query>", [NSString stringByEncodingXmlEntities:self.query]]];
    if (![self isFieldEmpty:rptKey])
        [bodyXML appendString:[NSString stringWithFormat:@"<RptKey>%@</RptKey>", self.rptKey]];

    if (![self isFieldEmpty:searchBy])
        [bodyXML appendString:[NSString stringWithFormat:@"<SearchBy>%@</SearchBy>", self.searchBy]];
    
    [bodyXML appendString:@"</ListSearchCriteria>"];
    return bodyXML;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    FormFieldData *field = parameterBag[@"FIELD"];
    if (field.listKey == nil)
        self.fieldId = field.iD;
    else {
        self.listKey = field.listKey;
    }

    self.ftCode = field.ftCode;
    if ([self isFieldEmpty:ftCode])
        self.ftCode = @"RPTINFO";
    
    self.parentLiKey = field.parLiKey;
    self.query = parameterBag[@"QUERY"];
    self.rptKey = parameterBag[@"RPT_KEY"];
    
    self.searchBy = parameterBag[@"SEARCH_BY"];
    
    // Use MRU param, only for ct_list fields
    // TODO - allow MRU in lower level list fields
    if ([self isFieldEmpty:query] && [self isFieldEmpty:fieldId])
        self.isMru = parameterBag[@"MRU"];

    // We need to cache the currency list separate from other lists.  RECORD_KEY is used to generate a unique cache name.
    if (field.iD != nil && [field.iD isEqualToString:@"TransactionCurrencyName"])
        parameterBag[@"RECORD_KEY"] = field.iD;
    
    self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SearchListItemsV3",[ExSystem sharedInstance].entitySettings.uri];
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
    [msg setHeader:[ExSystem sharedInstance].sessionID];
    [msg setContentType:@"application/xml"];
    [msg setMethod:@"POST"];
    [msg setBody:[self makeXMLBody]];
    
    return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
    if ([elementName isEqualToString:@"ListItem"])
    {
        self.item = [[ListItem alloc] init];

    }
    else if ([elementName isEqualToString:@"Fields"])
    {
        if (self.item != nil && self.item.fields == nil)
        {
            self.item.fields = [[NSMutableDictionary alloc] init];

        }
    }
    else if ([elementName isEqualToString:@"Field"])
    {
        self.extraFieldId = nil;	// Reset field Id for a new field
    }
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];

    if ([elementName isEqualToString:@"ListItem"])
    {
        if(self.item == nil)
        {
            [Flurry logError:@"LIST_FIELD_SEARCH_DATA" message:[NSString stringWithFormat:@"Error self.Item is empty. StackTrac: %@",[NSThread callStackSymbols] ] error:nil];
        }
                
        [self.listItems addObject:self.item];
        self.item = nil;
    }
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];

    NSString* propName = xmlToPropertyMap[currentElement];
    if (propName != nil)
    {
        BOOL isItemProp = [currentElement isEqual:@"Key"] || [currentElement isEqual:@"Code"] || [currentElement isEqual:@"Text"] || [currentElement isEqualToString:@"External"]
            ||[currentElement isEqual:@"IsMru"];
        if (self.item != nil && isItemProp) {
            [self.item setValue:buildString forKey:propName];
        }
        else if (!isItemProp) {
            [self setValue:buildString forKey:propName];
        }
    }
    else {
        if ([currentElement isEqualToString:@"Id"])
        {
            self.extraFieldId = buildString;
        }
        else if ([currentElement isEqualToString:@"Value"])
        {
            if (self.item != nil && self.item.fields != nil && self.extraFieldId != nil)
                (self.item.fields)[self.extraFieldId] = buildString;
        }
                
    }
}

#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder
{
    [super encodeWithCoder:coder];

    [coder encodeObject:fieldId	forKey:@"fieldId"];
    [coder encodeObject:ftCode	forKey:@"ftCode"];
    [coder encodeObject:isMru	forKey:@"isMru"];
    [coder encodeObject:listKey	forKey:@"listKey"];
    [coder encodeObject:parentLiKey	forKey:@"parentLiKey"];
    [coder encodeObject:query	forKey:@"query"];
    [coder encodeObject:rptKey	forKey:@"rptKey"];
    [coder encodeObject:listItems	forKey:@"listItems"];
}

- (id)initWithCoder:(NSCoder *)coder 
{
    self = [super initWithCoder:coder];
    self.fieldId = [coder decodeObjectForKey:@"fieldId"];
    self.ftCode = [coder decodeObjectForKey:@"ftCode"];
    self.isMru = [coder decodeObjectForKey:@"isMru"];
    self.listKey = [coder decodeObjectForKey:@"listKey"];
    self.parentLiKey = [coder decodeObjectForKey:@"parentLiKey"];
    self.query = [coder decodeObjectForKey:@"query"];
    self.rptKey = [coder decodeObjectForKey:@"rptKey"];
    self.listItems = [coder decodeObjectForKey:@"listItems"];
    return self;
}

@end
