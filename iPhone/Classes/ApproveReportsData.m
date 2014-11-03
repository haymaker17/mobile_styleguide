//
//  ApproveReportsData.m
//  ConcurMobile
//
//  Created by yiwen on 1/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportsData.h"
#import "Msg.h"
#import "DataConstants.h"
#import "ReportData.h"
#import "ExSystem.h"

@implementation ApproveReportsData

@synthesize reports;
@synthesize keys;
@synthesize isInElement;
@synthesize currentReport;

@synthesize uri;
@synthesize reportStatus;

#define kRoleCode @"MOBILE_EXPENSE_MANAGER"

//extracts the XML from a data stream and tells the parser to get parsing
- (void)parseXMLFileAtData:(NSData *)webData 
{	
	NSString* tData = [[NSString alloc] initWithData:webData encoding:NSASCIIStringEncoding];

	// Replace all "&amp;" to "&" to not confuse the parser
	NSString* sData = [tData stringByReplacingOccurrencesOfString:@"&amp;" withString:@"|"];
	
	// Replace http://localhost to hostname in message

	sData = [sData stringByReplacingOccurrencesOfString:@"http://localhost" withString:uri];
	
	// Parser only needed in the scope of implementation, single thread situation
	NSXMLParser* dataParser = [[NSXMLParser alloc] initWithData:[sData dataUsingEncoding:NSUnicodeStringEncoding]];
	
	[dataParser setDelegate:self];
	[dataParser setShouldProcessNamespaces:NO];
	[dataParser setShouldReportNamespacePrefixes:NO];
	[dataParser setShouldResolveExternalEntities:NO];
	[dataParser parse];
}

// MsgReponder APIs
-(void) respondToXMLData:(NSData *)data
{//we have many calls, and we don't want to to the calls out of order
	[self flushData];
	[self parseXMLFileAtData:data];
}


-(id)init
{
	self = [super init];
    if (self)
    {
        isInElement = @"NO";
        currentElement = @"";
        [self flushData];
    }
	return self;
}


-(NSString *)getMsgIdKey
{
	return APPROVE_REPORTS_DATA;
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{
    return [self newApproveMsg:parameterBag];
}

-(Msg *) newApproveMsg:(NSMutableDictionary *)parameterBag
{
	isInElement = false;
	ReportData *rpt = parameterBag[@"REPORT"];
	self.uri = [ExSystem sharedInstance].entitySettings.uri;
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/ApproveExpenseReport/%@", 
				 [ExSystem sharedInstance].entitySettings.uri, rpt.rptKey];
	Msg *msg = [[Msg alloc] initWithData:APPROVE_REPORTS_DATA State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];	
    //[self.msg init:APPROVE_REPORTS_DATA State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];	

 	NSString* bodyFormat = @"<AdvanceWorkflow><Comment>%@</Comment><CurrentSequence>%@</CurrentSequence><ProcessInstanceKey>%@</ProcessInstanceKey><RoleCode>%@</RoleCode>%@</AdvanceWorkflow>";
    NSString* statKeyStr = @""; // MOB-9753 custom approval status
    if ([parameterBag[@"STAT_KEY"] length])
        statKeyStr = [NSString stringWithFormat:@"<StatKey>%@</StatKey>", parameterBag[@"STAT_KEY"]];
    
	[msg setBody:[NSString stringWithFormat:bodyFormat, 
					   @"",
					   rpt.currentSequence,
					   rpt.processInstanceKey ,
					   @"MANAGER",
                       statKeyStr
					   ]]; //[parameterBag objectForKey:@"SendBackComment"]
	
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"]; 

	return msg;
}

-(Msg *) newRejectMsg:(NSMutableDictionary *)parameterBag
{
	isInElement = false;
	ReportData *rpt = parameterBag[@"REPORT"];
	self.uri = [ExSystem sharedInstance].entitySettings.uri;
	self.path = [NSString stringWithFormat:@"%@/mobile/Expense/RejectExpenseReport/%@", 
				 [ExSystem sharedInstance].entitySettings.uri, rpt.rptKey];
	Msg *msg = [[Msg alloc] initWithData:APPROVE_REPORTS_DATA State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];
    //[self.msg init:APPROVE_REPORTS_DATA State:@"" Position:nil MessageData:nil URI:self.path MessageResponder:self ParameterBag:parameterBag];	
	//NSLog(@"rpt.currentSequence = %@", rpt.currentSequence);
	NSString* comment = parameterBag[@"SendBackComment"];
	if (comment != nil)
		comment = [NSString stringByEncodingXmlEntities:comment];
 	NSString* bodyFormat = @"<AdvanceWorkflow><Comment>%@</Comment><CurrentSequence>%@</CurrentSequence><ProcessInstanceKey>%@</ProcessInstanceKey><RoleCode>%@</RoleCode></AdvanceWorkflow>";
	[msg setBody:[NSString stringWithFormat:bodyFormat, 
					   comment,//[parameterBag objectForKey:@"SendBackComment"],
					   rpt.currentSequence,
					   rpt.processInstanceKey,
					   @"MANAGER"]];
	
//	NSLog(@"bodyformat %@", [NSString stringWithFormat:bodyFormat, 
//							 [parameterBag objectForKey:@"SendBackComment"],
//							 rpt.currentSequence,
//							 rpt.processInstanceKey ,
//							 @"MANAGER"]);
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"text/xml"];
	[msg setMethod:@"POST"]; 
	
	return msg;
}



-(void) flushData
{
	if(self.keys != nil)
	{
		for(int x = 0; x < [self.keys count]; x++)
		{
            id key = (self.keys)[x];
			[self.reports removeObjectForKey:key];
		}
	}
    
    [self.keys removeAllObjects];
}


// SAX Parsing APIs
- (void)parserDidStartDocument:(NSXMLParser *)parser 
{
	////NSLog(@"found file and started parsing");
}


- (void)parser:(NSXMLParser *)parser parseErrorOccurred:(NSError *)parseError 
{
//	NSString * errorString = [NSString stringWithFormat:@"Unable to authenticate from web site (Error code %i )", [parseError code]];
//	////NSLog(@"error parsing XML: %@", errorString);
//	
//	UIAlertView * errorAlert = [[UIAlertView alloc] initWithTitle:@"Error loading content" message:errorString delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
//	[errorAlert show];
//	if (msg.errBody == nil) 
//		msg.errBody = [parseError localizedDescription];
//	
//	if (msg.errCode == nil) 
//		msg.errCode = [NSString stringWithFormat:@"%i", [parseError code]];
}


- (void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    [super parser:parser didStartElement:elementName namespaceURI: namespaceURI qualifiedName:qName attributes:attributeDict];
	
	self.currentElement = elementName;

	isInElement = true;

    if ([elementName isEqualToString:@"ActionStatus"])
	{
		self.reportStatus = [[ActionStatus alloc] init];
	}

}


- (void)parser:(NSXMLParser *)parser didEndElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName
{
    [super parser:parser didEndElement:elementName namespaceURI: namespaceURI qualifiedName:qName];

	isInElement = false;
	
//	if ([elementName isEqualToString:@"ReportToApprove"]) 
//	{
//		[self.reports setObject:currentReport forKey:[currentReport valueForKey:@"RptKey"]];
//		
//		[self.keys addObject:[currentReport valueForKey:@"RptKey"]];
//	}
}

- (void)parser:(NSXMLParser *)parser foundIgnorableWhitespace:(NSString *)string
{
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

}


- (void)parserDidEndDocument:(NSXMLParser *)parser 
{
	//	[activityIndicator stopAnimating];
	//	[activityIndicator removeFromSuperview];

    // notify that we've finished approving.
    [[NSNotificationCenter defaultCenter] postNotificationName:@"ReportApprovedNotification" object:self];
}

@end
