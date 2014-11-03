//
//  AppendReceiptData.m
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AppendReceiptData.h"

@implementation AppendReceiptData

@synthesize fromReceiptImageId, toReceiptImageId, actionStatus;

-(NSString *)getMsgIdKey
{
    return APPEND_RECEIPT;
}

-(NSString *)makeXMLBody
{
    __autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc]
                                                    initWithString:@"<AppendReceiptAction xmlns:i='http://www.w3.org/2001/XMLSchema-instance'>"];

    [bodyXML appendString:[NSString stringWithFormat:@"<FromReceiptImageId>%@</FromReceiptImageId>",self.fromReceiptImageId]];
    [bodyXML appendString:[NSString stringWithFormat:@"<ToReceiptImageId>%@</ToReceiptImageId>",self.toReceiptImageId]];
    [bodyXML appendString:@"</AppendReceiptAction>"];
    return bodyXML;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
    self.fromReceiptImageId = parameterBag[@"FROM_RECEIPT_IMAGE_ID"];
    self.toReceiptImageId = parameterBag[@"TO_RECEIPT_IMAGE_ID"];

    NSString *Urlpath = [NSString stringWithFormat:@"%@/mobile/Expense/AppendReceipt",[ExSystem sharedInstance].entitySettings.uri];

    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:Urlpath MessageResponder:self ParameterBag:parameterBag];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
    [msg setContentType:@"application/xml"];
    [msg setMethod:@"POST"];
    [msg setBody:[self makeXMLBody]];

    return msg;
}

- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];

    if ([elementName isEqualToString:@"ActionStatus"])
    {
        self.actionStatus = nil;
        actionStatus = [[ActionStatus alloc] init];
    }
}

- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
}

- (void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];

    if ([currentElement isEqualToString:@"Status"])
    {
        self.actionStatus.status = buildString;
    }
    else if ([currentElement isEqualToString:@"ErrorMessage"])
    {
        self.actionStatus.errMsg = buildString;
    }
}


@end
