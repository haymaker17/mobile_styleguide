//
//  OutOfPocketSaveData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "ExpenseTypeData.h"
#import "OOPEntry.h"

@interface OutOfPocketSaveData : MsgResponder 
{
	NSString				*currentElement, *path;

	NSString				*isInElement, *returnStatus, *meKey;
	OOPEntry				*entry;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) OOPEntry					*entry;
@property (nonatomic, strong) NSString					*returnStatus;
@property (nonatomic, strong) NSString					*meKey;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
