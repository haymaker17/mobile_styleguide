//
//  AmtrakCancel.h
//  ConcurMobile
//
//  Created by charlottef on 1/14/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"

@interface AmtrakCancel : MsgResponderCommon
{
    NSXMLParser				*dataParser;
	BOOL					isSuccess;
    BOOL                    canceledEntireTrip;
    NSString                *errorMessage;
}

@property BOOL isSuccess;
@property BOOL canceledEntireTrip;
@property (nonatomic, copy) NSString *errorMessage;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
