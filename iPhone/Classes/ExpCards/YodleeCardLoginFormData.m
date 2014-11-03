//
//  YodleeCardLoginFormData.m
//  ConcurMobile
//
//  Created by yiwen on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "YodleeCardLoginFormData.h"

@implementation YodleeCardLoginFormData

-(NSString *)getMsgIdKey
{
	return YODLEE_CARD_LOGIN_FORM_DATA;
}

-(void) flushData
{
	[super flushData];
    self.fields = nil;
    self.contentServiceId = nil;
    self.formField = nil;
}




-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    self.contentServiceId = parameterBag[@"ContentServiceId"];
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/GetYodleeCardLoginForm/%@",[ExSystem sharedInstance].entitySettings.uri, self.contentServiceId];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}

/*
 <ArrayOfFormField xmlns="http://schemas.datacontract.org/2004/07/Snowbird" xmlns:i="http://www.w3.org/2001/XMLSchema-instance"> 
 <FormField>
 <CtrlType>edit</CtrlType>
 <DataType>TEXT</DataType>
 <Id>LOGIN</Id>
 <Label>Online ID</Label>
 </FormField>
 <FormField>
 <CtrlType>edit</CtrlType><DataType>PASSWORD</DataType><Id>PASSWORD</Id><Label>Passcode</Label>
 </FormField>
 </ArrayOfFormField>
*/

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
	[super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
	
    if ([elementName isEqualToString:@"ArrayOfFormField"])
	{
		self.fields = [[NSMutableArray alloc] init];
        self.formField = nil;
	}
    else if ([elementName isEqualToString:@"FormField"])
    {
        self.formField = [[FormFieldData alloc] init];
    }
}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
	[super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
	if ([elementName isEqualToString:@"FormField"])
	{
		[self.fields addObject:self.formField];
		self.formField = nil;
	}
}


- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
	[super parser:parser foundCharacters:string];
	
    if (self.formField != nil)
    {
        NSString* propName = [FormFieldData getXmlToPropertyMap][currentElement];
        if (propName != nil)
        {
            [self.formField setValue:buildString forKey:propName];
        }
	}
}

@end
