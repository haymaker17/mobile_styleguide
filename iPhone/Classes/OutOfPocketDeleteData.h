//
//  OutOfPocketDeleteData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "ExpenseTypeData.h"

@interface OutOfPocketDeleteData  : MsgResponder
{
	NSString				*currentElement, *path;

	NSString				*isInElement, *returnStatus;
	NSMutableDictionary		*keysToKill, *returnFailures, *returnFailure;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSString					*returnStatus;
@property (nonatomic, strong) NSMutableDictionary		*keysToKill;
@property (nonatomic, strong) NSMutableDictionary		*returnFailures;
@property (nonatomic, strong) NSMutableDictionary		*returnFailure;


//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
//- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
