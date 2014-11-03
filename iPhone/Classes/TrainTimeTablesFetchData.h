//
//  TrainTimeTablesFetchData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "TrainStationData.h"
#import "RailChoiceData.h"
#import "RailChoiceSegmentData.h"
#import "RailChoiceTrainData.h"
#import "HotelViolation.h"

@interface TrainTimeTablesFetchData : MsgResponder 
{
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableDictionary		*items, *railChoices;
	NSMutableArray			*keys;
	RailChoiceData			*obj;
    HotelViolation			*currentViolation; // Hotel violation data structure is used for rail as well
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) RailChoiceData			*obj;
@property (nonatomic, strong) NSMutableArray			*keys;
@property (nonatomic, strong) NSMutableDictionary		*railChoices;
@property (nonatomic, strong) HotelViolation            *currentViolation;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
-(NSString *)makeXMLBody:(NSMutableDictionary *)parameterBag;

@end
