//
//  AirBenchmarkData.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 06/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AirBenchmarkData.h"
#import "RXMLElement.h"

@implementation AirBenchmarkData

-(NSString *)getMsgIdKey
{
	return AIR_BENCHMARK_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	NSString *path = [NSString stringWithFormat:@"%@/Mobile/Air/GetBenchmarks",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}


-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{
    NSString* formattedBodyXml = @"<PriceToBeatAirCriteria><DepartDate>%@</DepartDate><EndIata>%@</EndIata><RoundTrip>%@</RoundTrip><StartIata>%@</StartIata></PriceToBeatAirCriteria>";
    
    NSString *departDate = parameterBag[@"Date"];
    NSString *endIata = parameterBag[@"EndIata"];
    NSString *startIata = parameterBag[@"StartIata"];
    NSString *roundTrip = [parameterBag[@"isRound"] boolValue] ? @"true" : @"false";
    
    return [NSString stringWithFormat:formattedBodyXml, departDate, endIata, roundTrip, startIata];
}

/*
 <MWSResponse xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
 <Response>
 <Currency>USD</Currency>
 <Date>2014-03-28T00:00:00</Date>
 <Destination>PRG</Destination>
 <Origin>LHR</Origin>
 <Price>456.06</Price>
 <RoundTrip>true</RoundTrip>
 </Response>
 <Status>
 <IsSuccess>true</IsSuccess>
 <Message/>
 </Status>
 </MWSResponse>
 */

-(void) respondToXMLData:(NSData *)data
{
    NSString *theXML = [[NSString alloc] initWithBytes: [data bytes] length:[data length]  encoding:NSUTF8StringEncoding];
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:theXML encoding:NSUTF8StringEncoding];
    
    self.isSuccess = [[[rootXML child:@"Status.IsSuccess"] text] boolValue];
    self.message = [[rootXML child:@"Status.Message"] text];
    if (self.isSuccess)
    {
        NSArray *list = [rootXML childrenWithRootXPath:@"/MWSResponse/Response/AirBenchmarks/AirBenchmark"];
        for(RXMLElement *tripXML in list)
        {
            Benchmark *benchmarkData = [[Benchmark alloc] init] ;
            
            benchmarkData.crnCode = [[tripXML child:@"Currency"] text];
            benchmarkData.date = [DateTimeFormatter getNSDateFromMWSDateString:[[tripXML child:@"Date"] text]];
            benchmarkData.destination = [[tripXML child:@"Destination"] text];
            benchmarkData.origin = [[tripXML child:@"Origin"] text];
            benchmarkData.price = @([[tripXML child:@"Price"] textAsDouble]);
            benchmarkData.roundtrip = [[[tripXML child:@"RoundTrip"] text] boolValue];
            self.benchmark = benchmarkData;
        }
    }
}

@end
