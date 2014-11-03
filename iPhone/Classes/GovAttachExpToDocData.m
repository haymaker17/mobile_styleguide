//
//  GovAttachExpToDocData.m
//  ConcurMobile
//
//  Created by Shifan Wu on 1/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovAttachExpToDocData.h"

@implementation GovAttachExpToDocData

@synthesize expId, docName, docType, expUploadStatus, overAllStatus, inSignleExp;

-(NSString*) getMsgIdKey
{
    return GOV_ATTACH_EXP_TO_DOC;
}

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag
{
    if (parameterBag != nil & [parameterBag objectForKey:@"KEYS"] != nil) {
        self.expId = [[NSArray alloc] initWithArray:[parameterBag objectForKey:@"KEYS"]];
    }
    if (parameterBag != nil & [parameterBag objectForKey:@"DOC_TYPE"] != nil) {
        self.docType = [parameterBag objectForKey:@"DOC_TYPE"];
    }
    if (parameterBag != nil & [parameterBag objectForKey:@"DOC_NAME"] != nil) {
        self.docName = [parameterBag objectForKey:@"DOC_NAME"];
    }
    
    NSString * msgUuid = nil;
    if (parameterBag != nil && [parameterBag objectForKey:@"MSG_UUID"] != nil)
        msgUuid = [parameterBag objectForKey:@"MSG_UUID"];
    
    self.path = [NSString stringWithFormat:@"%@/Mobile/GovTravelManager/AttachExpenseToDocument", [ExSystem sharedInstance].entitySettings.uri];
    Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
    
    [msg setBody:[self makeXMLBody]];
    [msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
    [msg setUuid:msgUuid];
	return msg;
}

-(NSString *) makeXMLBody
{
    __autoreleasing NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<AttachExpenseToDocumentRequest>"];
    NSString *tagValue = @"";
    if (docType != nil)
        [bodyXML appendString:[NSString stringWithFormat:@"<docType>%@</docType>",docType]];
    else
        [bodyXML appendString:@"<docType/>"];
    tagValue = [NSString stringWithFormat:@"\"%@\"",@"http://schemas.microsoft.com/2003/10/Serialization/Arrays"];
    [bodyXML appendString:[NSString stringWithFormat:@"<expenseIds  xmlns:a=%@>",tagValue]];
    if ([expId count] > 0) {
        for(NSString *ID in expId)
        {
            [bodyXML appendString:[NSString stringWithFormat:@"<a:string>%@</a:string>", ID]];
        }
    }
    [bodyXML appendString:@"</expenseIds>"];
    if (docName != nil)
        [bodyXML appendString:[NSString stringWithFormat:@"<vchnum>%@</vchnum>", docName]];
    else
        [bodyXML appendString:@"<vchnum/>"];

    [bodyXML appendString:@"</AttachExpenseToDocumentRequest>"];
    
    //NSLog(@"%@", bodyXML);
    return bodyXML;
}

-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI:namespaceURI qualifiedName:qName attributes:attributeDict];
    
    if ([currentElement isEqualToString:@"expenses"]) {
        self.expUploadStatus = [[NSMutableDictionary alloc] init];
    }
    
    if ([currentElement isEqualToString:@"ActionStatus"]) {
        self.inSignleExp = YES;
    }
}

-(void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI:namespaceURI qualifiedName:qName];
    
    if ([currentElement isEqualToString:@"ActionStatus"]) {
        self.inSignleExp = NO;
    }
}

-(void) parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [super parser:parser foundCharacters:string];
    
    if ([currentElement isEqualToString:@"Status"])
    {
        if ([buildString isEqualToString:@"SUCCESS"])
            self.overAllStatus = YES;
        else
            self.overAllStatus = NO;
    }
//    else if (inSignleExp && [currentElement isEqualToString:@"Status"])
//    {
//        if (![buildString isEqualToString:@"SUCCESS"]) {
//            // TODO: parse unsuccessfull expenses.
//        }
//    }
}

-(void) parserDidStartDocument:(NSXMLParser *)parser
{
    [super parserDidStartDocument:parser];
}

-(void) parserDidEndDocument:(NSXMLParser *)parser
{
    [super parserDidEndDocument:parser];
}
@end
