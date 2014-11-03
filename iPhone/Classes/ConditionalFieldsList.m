//
//  DynamicFields.m
//  ConcurMobile
//
//  Created by Antonio Alwan on 12/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ConditionalFieldsList.h"
#import "RXMLElement.h"
#import "ConditionalFieldsActionList.h"

@implementation ConditionalFieldsList

@synthesize conditionalFieldListData;

-(id)init
{
    self = [super init];
    if (self)
    {
        self.conditionalFieldListData = [[ConditionalFieldsActionList alloc] init];
    }
    return self;
}

-(NSString *)getMsgIdKey
{
    return GET_DYNAMIC_ACTIONS;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    [self.conditionalFieldListData removeAll];
    
    NSString *path = [NSString stringWithFormat:@"%@/mobile/Expense/GetFormFieldDynamicAction/%@",
                      [ExSystem sharedInstance].entitySettings.uri,
                      parameterBag[@"ROLE_CODE"] ];
    
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey]
                                   State:@"" Position:nil MessageData:nil
                                     URI:path
                        MessageResponder:self
                            ParameterBag:parameterBag];
    
    [msg setHeader:[ExSystem sharedInstance].sessionID];
    [msg setContentType:@"application/xml"];
    [msg setMethod:@"POST"];
    [msg setBody:[self makeXMLBody: parameterBag[@"FF_KEY"]
                         editValue: parameterBag[@"VALUE"]]];
    return msg;
}

-(NSString *)makeXMLBody: (NSString*) formFieldKey editValue: (NSString *) editValue
{

    NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<DynamicFieldCriteria>"];
    
    [bodyXML appendString: [NSString stringWithFormat:@"<FormFieldKey>%@</FormFieldKey>", formFieldKey ]];
    [bodyXML appendString: [NSString stringWithFormat:@"<FormFieldValue>%@</FormFieldValue>", [NSString stringByEncodingXmlEntities:editValue]]];
    [bodyXML appendString:@"</DynamicFieldCriteria>"];
    
    return bodyXML;
}

/*
 <?xml version="1.0" encoding="utf-16"?>
    <ArrayOfDynamicFieldAction xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
    <DynamicFieldAction>
        <Access>RW</Access>
        <Action>SHOW</Action>
        <Field>351</Field>
    </DynamicFieldAction>
 </ArrayOfDynamicFieldAction>
 */

-(void) respondToXMLData:(NSData *)data
{
    NSString *theXML = [[NSString alloc] initWithBytes: [data bytes] length:[data length]  encoding:NSUTF8StringEncoding];
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:theXML encoding:NSUTF8StringEncoding];
    NSArray *list = [rootXML childrenWithRootXPath:@"/ArrayOfDynamicFieldAction/DynamicFieldAction"];
    
    for (RXMLElement *condition in list)
    {
        ConditionalFieldAction *conditionFieldAction = [[ConditionalFieldAction alloc] init];
        conditionFieldAction.action = [[condition child:@"Action"] text];
        conditionFieldAction.access = [[condition child:@"Access"] text];
        conditionFieldAction.field = [[condition child:@"Field"] textAsInt];
        [self.conditionalFieldListData add:conditionFieldAction];
    }
}

@end
