//
//  TripApproveOrReject.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 07/05/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TripApproveOrReject.h"
#import "TripToApprove.h"

@interface TripApproveOrReject()
@property (nonatomic, strong) NSXMLParser *dataParser;
@property (nonatomic, strong) NSString *currentElement;
@property (nonatomic, strong) NSMutableString *buildString;
@end

@implementation TripApproveOrReject

@synthesize isSuccess, responseErrorMessage;

-(NSString *)getMsgIdKey
{
	return APPROVE_TRIPS_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	NSString *path = [NSString stringWithFormat:@"%@/Mobile/TripApprovalActionV2",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	
//    <ApproveAction>
//    <Action>approve</Action>  // or 'reject'
//    <Comment>Trip rejection comment</Comment>
//    <CompanyId>1</CompanyId>
//    <TripId>3080</TripId>
//    <UserId>0</UserId>
//    </ApproveAction>
    NSString *action = parameterBag[@"ACTION"];
    TripToApprove *trip = parameterBag[@"TRIP_TO_APPROVE"];
    NSString* comment = parameterBag[@"COMMENT"];
	if (comment) comment = [NSString stringByEncodingXmlEntities:comment];
    
    NSMutableString *xml = [[NSMutableString alloc] initWithString:@"<ApproveAction>"];
    [xml appendFormat:@"<Action>%@</Action>",action];
    if (comment) [xml appendFormat:@"<Comment>%@</Comment>",comment];
    [xml appendFormat:@"<CompanyId>%@</CompanyId>",trip.travelerCompanyId];
    [xml appendFormat:@"<ItinLocator>%@</ItinLocator>",trip.itinLocator];
    [xml appendFormat:@"<UserId>%@</UserId>",trip.travelerUserId];
    [xml appendFormat:@"</ApproveAction>"];
    
    return xml;
}


-(void) respondToXMLData:(NSData *)data
{
    self.dataParser = [[NSXMLParser alloc] initWithData:data];
	[self.dataParser setDelegate:self];
	[self.dataParser setShouldProcessNamespaces:NO];
	[self.dataParser setShouldReportNamespacePrefixes:NO];
	[self.dataParser setShouldResolveExternalEntities:NO];
	[self.dataParser parse];
}

-(void)parser:(NSXMLParser *)parser didStartElement:(NSString *)elementName namespaceURI:(NSString *)namespaceURI qualifiedName:(NSString *)qName attributes:(NSDictionary *)attributeDict
{
    self.currentElement = elementName;
    self.buildString = [[NSMutableString alloc] init];
}

-(void)parser:(NSXMLParser *)parser foundCharacters:(NSString *)string
{
    [self.buildString appendString:string];
    if ([self.currentElement isEqualToString:@"IsSuccess"])
    {
        self.isSuccess = [self.buildString isEqualToString:@"true"];
    }
    else if ([self.currentElement isEqualToString:@"UserMessage"])
    {
        self.responseErrorMessage = self.buildString;
    }
}
    //<ActionStatus>
    //<Status>OK</Status>  // or 'FAIL'
    //<ErrorMessage>Shazbat!</ErrorMessage>
    //</ActionStatus>
    
    // New MWS response
    //<MWSResponse xmlns:i="">
    //   <Errors i:nil="true"/>
    //   <Response/>
    //   <Status>
    //      <IsSuccess>true</IsSuccess>
    //      <Message i:nil="true"/>
    //   </Status>
    //</MWSResponse>
    
    // OR
    
    //<MWSResponse xmlns:i="">
    //  <Errors>
    //    <Error>
    //      <Code i:nil="true"/>
    //      <SystemMessage>TripApprovalActionV2 error: Failure</SystemMessage>
    //      <UserMessage i:nil="true"/>
    //    </Error>
    //  </Errors>
    //  <Response/>
    //  <Status>
    //    <IsSuccess>false</IsSuccess>
    //    <Message i:nil="true"/>
    //  </Status>
    //</MWSResponse>
    
//    NSString *theXML = [[NSString alloc] initWithBytes: [data bytes] length:[data length]  encoding:NSUTF8StringEncoding];
////    NSLog(@"TheXML = %@",theXML);
//    RXMLElement *rootXML = [RXMLElement elementFromXMLString:theXML encoding:NSUTF8StringEncoding];
//    
//    //self.responseStatus = [[rootXML child:@"Status/IsSuccess"] text];
//    self.responseErrorMessage = [[rootXML child:@"ErrorMessage"] text];
//    self.isSuccess = [self.responseStatus isEqualToString:@"true"];
//}

@end
