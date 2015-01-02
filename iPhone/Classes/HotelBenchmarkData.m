//
//  HotelBenchmarkData.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 06/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelBenchmarkData.h"
#import "HotelBenchmark.h"
#import "RXMLElement.h"
#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "FormatUtils.h"

@implementation HotelBenchmarkData

-(NSString *)getMsgIdKey
{
	return HOTEL_BENCHMARK_DATA;
}


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag
{//set up the message
	
	NSString *path = [NSString stringWithFormat:@"%@/Mobile/Hotel/GetBenchmarks",[ExSystem sharedInstance].entitySettings.uri];
	Msg *msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setHeader:[ExSystem sharedInstance].sessionID];
	[msg setContentType:@"application/xml"];
	[msg setMethod:@"POST"];
	[msg setBody:[self makeXMLBody:parameterBag]];
	return msg;
}

-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag
{//knows how to make a post
	NSMutableString *bodyXML = [[NSMutableString alloc] initWithString:@"<PriceToBeatHotelCriteria>"];
	[bodyXML appendString:@"<Lat>%@</Lat>"];
	[bodyXML appendString:@"<Lon>%@</Lon>"];
    [bodyXML appendString:@"<MonthNumber>%@</MonthNumber>"];
	[bodyXML appendString:@"<Radius>%i</Radius>"];
	[bodyXML appendString:@"<Scale>%@</Scale>"];
	[bodyXML appendString:@"</PriceToBeatHotelCriteria>"];
	
	
	NSString* monthNumber = (NSString*)parameterBag[@"MonthOfStay"];
	NSString* latitude = (NSString*)parameterBag[@"Lat"];
	NSString* longitude = (NSString*)parameterBag[@"Lon"];
	int distance = [(NSNumber*)parameterBag[@"Radius"] intValue];
	NSString* distanceUnit  = (NSString*)parameterBag[@"Scale"];
	
	NSString* formattedBodyXml = [NSString stringWithFormat:bodyXML,
                                  latitude,
                                  longitude,
                                  monthNumber,
                                  distance,
                                  distanceUnit
                                  ];
	
	return formattedBodyXml;
}

-(NSString *)makeDateString:(NSDate*)date
{
	NSCalendar *calendar = [NSCalendar currentCalendar];
	calendar.timeZone = [NSTimeZone localTimeZone];
	NSDateComponents *components = [calendar components:(NSYearCalendarUnit | NSMonthCalendarUnit | NSDayCalendarUnit) fromDate:date];
	NSString *dateString = [NSString stringWithFormat:@"%li/%li/%li", (long)components.month, (long)components.day, (long)components.year];
	return dateString;
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
        self.benchmarksList = [HotelBenchmarkData getHotelBenchmarksFromXml:theXML atPath:@"/MWSResponse/Response/Benchmarks/HotelBenchmark"];
    }
}

+(NSArray *)getHotelBenchmarksFromXml:(NSString *)xml atPath:(NSString *)path
{
    RXMLElement *rootXML = [RXMLElement elementFromXMLString:xml encoding:NSUTF8StringEncoding];
    NSArray *list = [rootXML childrenWithRootXPath:path];
    NSMutableArray *benchmarks = [[NSMutableArray alloc] initWithCapacity:[list count]];
    for(RXMLElement *tripXML in list)
    {
        HotelBenchmark *benchmarkData = [[HotelBenchmark alloc] init] ;
        
        benchmarkData.currency = [[tripXML child:@"Currency"] text];
        benchmarkData.distanceAmount = [[tripXML child:@"Distance.Amount"] textAsDouble];
        benchmarkData.distanceUnits = [[tripXML child:@"Distance.Units"] text];
        benchmarkData.location = [[tripXML child:@"Location"] text];
        benchmarkData.name = [[tripXML child:@"Name"] text];
        benchmarkData.price = [[tripXML child:@"Price"] textAsDouble];
        benchmarkData.subdivCode = [[tripXML child:@"SubdivCode"] text];
        [benchmarks addObject:benchmarkData];
    }
    return [benchmarks copy];
}

+(NSString *)getBenchmarkRangeFromBenchmarks:(NSArray *)benchmarks
{
    HotelBenchmark *minPriceBenchmark;
    HotelBenchmark *maxPriceBenchmark;
    for (id benchmark in benchmarks) {
        if ([benchmark isKindOfClass:[HotelBenchmark class]]) {
            double price = [(HotelBenchmark *)benchmark price];
            if (price > 0) {
                if (!minPriceBenchmark || price < minPriceBenchmark.price) {
                    minPriceBenchmark = benchmark;
                }
                if (!maxPriceBenchmark || price > maxPriceBenchmark.price) {
                    maxPriceBenchmark = benchmark;
                }
            }
        }
    }
    if (!minPriceBenchmark && !maxPriceBenchmark) {
        return @"";
    }
    else if (minPriceBenchmark == maxPriceBenchmark) {
        return [FormatUtils formatMoney:[@(minPriceBenchmark.price) stringValue] crnCode:minPriceBenchmark.currency];
    }
    else {
        return [NSString stringWithFormat:@"%@-%@",[FormatUtils formatMoney:[@(minPriceBenchmark.price) stringValue] crnCode:minPriceBenchmark.currency],[FormatUtils formatMoney:[@(maxPriceBenchmark.price) stringValue] crnCode:maxPriceBenchmark.currency]];
    }
}


@end
