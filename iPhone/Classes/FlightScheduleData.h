//
//  FlightScheduleData.h
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MsgResponder.h"

@interface FlightScheduleData : MsgResponder
{
    
    NSString *path;
    NSXMLParser *dataParser;
    NSMutableString *buildString;
    
    BOOL inSegment, inSegmentOption, inFlight, inClassOfService;
    
    
    NSMutableArray *segments;
}

@property (nonatomic, strong) NSString *path;
@property (nonatomic, strong) NSMutableArray *segmentOptions;

-(void) appendOptions:(NSMutableArray*)ary;


- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

-(void)startTag:(NSString*)tag;
-(void)endTag:(NSString*)tag withText:(NSString*)text;

@end
