//
//  GovExpenseSaveData.m
//  ConcurMobile
//
//  Created by ernest cho on 9/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "GovExpenseSaveData.h"
#import "FormFieldData.h"

@implementation GovExpenseSaveData

@synthesize fields, formAttributes, expId;

-(id)init
{
    self = [super init];
	if (self)
    {
        [self flushData];
    }
	return self;
}

-(NSString *) getMsgIdKey
{
    return SAVE_GOV_EXPENSE;
}

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    if( parameterBag != nil & parameterBag[@"FIELDS"] != nil)
        self.fields = parameterBag[@"FIELDS"];  // Fields with updated data
    
    if( parameterBag != nil & parameterBag[@"ATTRIBUTES"] != nil)
        self.formAttributes = parameterBag[@"ATTRIBUTES"];
    
    NSString * msgUuid = nil;
    if (parameterBag != nil && parameterBag[@"MSG_UUID"] != nil)
        msgUuid = parameterBag[@"MSG_UUID"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/SaveTMExpenseForm",[ExSystem sharedInstance].entitySettings.uri];
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    
    [msg setBody:[self makeXMLBody]];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
    [msg setUuid:msgUuid];
	return msg;
}

-(NSString *)makeXMLBody
{
	__autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<SaveTMExpenseFormRequest>"];
    NSString *tagValue = @"";
    
    if (formAttributes != nil & formAttributes[@"acclabel"] != nil)
    {
        tagValue = formAttributes[@"acclabel"];
        [bodyXML appendString:[NSString stringWithFormat:@"<acclabel>%@</acclabel>", tagValue]];
    }
    else
        [bodyXML appendString:@"<acclabel/>"];

    if (formAttributes != nil & formAttributes[@"description"] != nil)
    {
        tagValue = formAttributes[@"description"];
        [bodyXML appendString:[NSString stringWithFormat:@"<description>%@</description>", [NSString stringByEncodingXmlEntities:tagValue]]];
    }
    else
        [bodyXML appendString:@"<description/>"];


    if (formAttributes != nil & formAttributes[@"docType"] != nil)
    {
        tagValue = formAttributes[@"docType"];
        [bodyXML appendString:[NSString stringWithFormat:@"<docType>%@</docType>", tagValue]];
    }
    else
        [bodyXML appendString:@"<docType/>"];
    
    [bodyXML appendString:@"<fields>"];
    // get field counts
    NSUInteger numOfFields = [fields count];
    // fill each field
    for (int i = 0; i < numOfFields; i++)
    {
        FormFieldData *oneField = (FormFieldData *) fields[i];
        [bodyXML appendString:@"<TMFormField>"];
        if (oneField != nil & oneField.ctrlType != nil)
        {
            [bodyXML appendString:[NSString stringWithFormat:@"<CtrlType>%@</CtrlType>",oneField.ctrlType]];
        }
        
        if (oneField != nil & oneField.dataType != nil)
        {
            [bodyXML appendString:[NSString stringWithFormat:@"<DataType>%@</DataType>",oneField.dataType]];
        }
        
        if (oneField != nil & oneField.iD != nil)
        {
            [bodyXML appendString:[NSString stringWithFormat:@"<Id>%@</Id>",oneField.iD]];
        }
        
        if (oneField != nil & oneField.label != nil)
        {
            [bodyXML appendString:[NSString stringWithFormat:@"<Label>%@</Label>",oneField.label]];
        }
        
        if (oneField != nil & oneField.required != nil)
        {
            [bodyXML appendString:[NSString stringWithFormat:@"<Required>%@</Required>",oneField.required]];
        }
        
        if (oneField != nil & oneField.fieldValue != nil)
        {
            [bodyXML appendString:[NSString stringWithFormat:@"<Value>%@</Value>", [NSString stringByEncodingXmlEntities:oneField.fieldValue]]];
        }
        [bodyXML appendString:@"</TMFormField>"];
    }
    
    
    [bodyXML appendString:@"</fields>"];
    
    if (formAttributes != nil & formAttributes[@"mode"] != nil)
    {
        tagValue = formAttributes[@"mode"];
        [bodyXML appendString:[NSString stringWithFormat:@"<mode>%@</mode>", tagValue]];
    }
    else
        [bodyXML appendString:@"<mode/>"];
    
    if (formAttributes != nil & formAttributes[@"org"] != nil)
    {
        tagValue = formAttributes[@"org"];
        [bodyXML appendString:[NSString stringWithFormat:@"<org>%@</org>", tagValue]];
    }
    else
        [bodyXML appendString:@"<org/>"];
    
    if (formAttributes != nil & formAttributes[@"sublabel"] != nil)
    {
        tagValue = formAttributes[@"sublabel"];
        [bodyXML appendString:[NSString stringWithFormat:@"<sublabel>%@</sublabel>", tagValue]];
    }
    else
        [bodyXML appendString:@"<sublabel/>"];
    
    if (formAttributes != nil & formAttributes[@"userId"] != nil)
    {
        tagValue = formAttributes[@"userId"];
        [bodyXML appendString:[NSString stringWithFormat:@"<userId>%@</userId>", tagValue]];
    }
    else
        [bodyXML appendString:@"<userId/>"];
    
    if (formAttributes != nil & formAttributes[@"vchnum"] != nil)
    {
        tagValue = formAttributes[@"vchnum"];
        [bodyXML appendString:[NSString stringWithFormat:@"<vchnum>%@</vchnum>", tagValue]];
    }
    else
        [bodyXML appendString:@"<vchnum/>"];

	[bodyXML appendString:@"</SaveTMExpenseFormRequest>"];  
	return bodyXML;
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    if ([currentElement isEqualToString:@"expID"])
	{
		[self setExpId:[buildString stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]]];
	}
//    else if ([currentElement isEqualToString:@"Status"])
//	{
//		[self.status setStatus:buildString];
//	}
//    else if ([currentElement isEqualToString:@"ErrorMessage"])
//	{
//		[self.status setErrMsg:buildString];
//	}
}


@end
