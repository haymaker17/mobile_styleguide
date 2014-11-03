//
//  SubmitReportData.m
//  ConcurMobile
//
//  Created by yiwen on 4/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SubmitReportData.h"
#import "DataConstants.h"

@implementation SubmitReportData
@synthesize reportStatus, approver, approverEmpKey;
static NSMutableDictionary* xmlToPropertyMap = nil;

// Initialize msgId to msg class mapping here
+ (void)initialize
{
	if (self == [SubmitReportData class]) 
	{
        // Perform initialization here.
		xmlToPropertyMap = [[NSMutableDictionary alloc] init];
		xmlToPropertyMap[@"Email"] = @"Email";
		xmlToPropertyMap[@"EmpKey"] = @"EmpKey";
		xmlToPropertyMap[@"FirstName"] = @"FirstName";
		xmlToPropertyMap[@"LastName"] = @"LastName";
		xmlToPropertyMap[@"LoginId"] = @"LoginId";
	}
}


-(NSString *)getMsgIdKey
{
	return SUBMIT_REPORT_DATA;
}

-(void) flushData
{
	[super flushData];
    self.approver = nil;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	self.rptKey = parameterBag[@"ID_KEY"];
	self.approverEmpKey = parameterBag[@"APPROVER_EMP_KEY"];
    if (![self.approverEmpKey length])
        self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SubmitReportV2/%@", 
				[ExSystem sharedInstance].entitySettings.uri, self.rptKey];
    else
        self.path = [NSString stringWithFormat:@"%@/mobile/Expense/SubmitReportV2/%@/%@", 
                     [ExSystem sharedInstance].entitySettings.uri, self.rptKey, self.approverEmpKey];
        
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	
	return msg;
}



# pragma
# pragma Parser delegater methods
- (NSString*) getReportElementName
{
	return @"Report";
}

/*
 <Approver>
 <ApproverRptKey>n5moKv5rnObUc2vM3feNRV9vwojRb$pQ</ApproverRptKey>
 <Email>EManager_1201@concur.com</Email>
 <EmpKey>n8pBquEPHZ$shSSLMI8qG7fx$sa$sg</EmpKey>
 <ExternalUserName>anilb@concurdev.com</ExternalUserName>
 <FirstName>Samuel</FirstName>
 <LastName>EManager_1201</LastName>
 <LoginId>EManager_1201</LoginId>
 </Approver>
 */
- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.reportStatus = [[ActionStatus alloc] init];
	}
    else if ([elementName isEqualToString:@"Approver"])
    {
        self.approver = [[ApproverInfo alloc] init];
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
		self.reportStatus.status = string;
	}	
	else if ([currentElement isEqualToString:@"ErrorMessage"])
	{
		self.reportStatus.errMsg = buildString;
	}
    else if ([currentElement isEqualToString:@"CanDrawSubmit"])
    {
        self.canDrawSubmit = string;
    }
	else if (self.approver != nil)
	{
        NSString* propName = xmlToPropertyMap[currentElement];
        if (propName != nil)
        {
            [self.approver setValue:buildString forKey:propName];
        }
    }

}	

@end
