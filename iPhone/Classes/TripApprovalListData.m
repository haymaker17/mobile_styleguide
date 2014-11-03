//
//  TripApprovalListData.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 02/05/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TripApprovalListData.h"
#import "RXMLElement.h"
#import "TripToApprove.h"

@implementation TripApprovalListData

@synthesize aTripsForApproval;

-(NSString *)getMsgIdKey
{
	return TRIP_APPROVAL_LIST_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	NSString *path = [NSString stringWithFormat:@"%@/Mobile/TripApproval/TripsV3",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"GET"];
	
	return msg;
}


//<MWSResponse xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
//<Errors i:nil="true" />
//<Response>
//<Status i:nil="true" xmlns="http://schemas.datacontract.org/2004/07/Snowbird" />
//<TripsToApproveList>
//<TripToApprove>
//<ApproveByDate>2013-10-15T12:00:00</ApproveByDate>
//<BookedDate>2013-05-09T11:42:00</BookedDate>
//<EndDate>2013-10-16T12:00:00</EndDate>
//<ItinLocator>3147</ItinLocator>
//<RecordLocator>P4QTLL</RecordLocator>
//<StartDate>2013-10-15T12:00:00</StartDate>
//<TotalTripCost>94.0000</TotalTripCost>
//<TotalTripCostCrnCode>USD</TotalTripCostCrnCode>
//<TransactionId>5913</TransactionId>
//<TravelerCompanyId>408</TravelerCompanyId>
//<TravelerName>Thomas Creed</TravelerName>
//<TravelerUserId>55513</TravelerUserId>
//<TripId>3147</TripId>
//<TripName>Hotel Reservation at ALBUQUERQUE INTL ARPT, ALBUQUERQUE, NM</TripName>
//</TripToApprove>
//</TripsToApproveList>
//</Response>
//<Status>
//<IsSuccess>true</IsSuccess>
//<Message>Success</Message>
//</Status>
//</MWSResponse>


-(void) respondToXMLData:(NSData *)data
{
    NSString *theXML = [[NSString alloc] initWithBytes: [data bytes] length:[data length]  encoding:NSUTF8StringEncoding];
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:theXML encoding:NSUTF8StringEncoding];
    
    NSArray *list = [rootXML childrenWithRootXPath:@"/MWSResponse/Response/TripsToApproveList/TripToApprove"];
    aTripsForApproval = [[NSMutableArray alloc] init];
    
    for(RXMLElement *tripXML in list)
    {
        TripToApprove *trip = [[TripToApprove alloc] init] ;
        
        trip.travelerName = [[tripXML child:@"TravelerName"] text];
        trip.tripName = [[tripXML child:@"TripName"] text];
        trip.itinLocator = [[tripXML child:@"ItinLocator"] text];
        trip.travelerUserId = [[tripXML child:@"TravelerUserId"] text];
        trip.travelerCompanyId = [[tripXML child:@"TravelerCompanyId"] text];
        trip.approveByDate = [DateTimeFormatter getNSDateFromMWSDateString:[[tripXML child:@"ApproveByDate"] text]];//[DateTimeFormatter getNSDate:[[tripXML child:@"ApproveByDate"] text] Format:@"yyyy-MM-dd'T'HH:mm:ss"];
        trip.totalTripCost = @([[[tripXML child:@"TotalTripCost"] text] doubleValue]);
        trip.totalTripCostCrnCode = [[tripXML child:@"TotalTripCostCrnCode"] text];
        
        [aTripsForApproval addObject:trip];
    }
    [aTripsForApproval sortUsingComparator:^(TripToApprove *t1, TripToApprove *t2){ return [t1.approveByDate compare:t2.approveByDate]; }];
}

@end
