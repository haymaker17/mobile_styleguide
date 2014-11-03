//
//  ApproveExpenseDetail.h
//  ConcurMobile
//
//  Created by Yuri on 2/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"

@class RootViewController;
@class MsgControl;
@class Msg;

@interface ApproveExpenseDetailData : MsgResponder {
	NSMutableDictionary		*parameterBag;
	NSMutableDictionary     *entries;
	NSMutableArray          *exceptions;
	NSMutableArray          *fields;
	
	// TODO - move to MsgResponder
	Msg						*msg;
	NSString				*path;
    
	// Transient data used in SAX Parsing
	bool                    isInElement;
	NSString                *currentElement;
    NSMutableDictionary     *currentEntry;
}

@property (nonatomic, retain) NSMutableDictionary       *parameterBag;
@property (nonatomic, retain) NSMutableDictionary       *entries;
@property (nonatomic, retain) NSMutableArray            *exceptions;
@property (nonatomic, retain) NSMutableArray            *fields;

@property (nonatomic, retain) Msg						*msg;
@property (nonatomic, retain) NSString					*path;

@property (nonatomic)         bool                       isInElement;
@property (nonatomic, copy)   NSString                  *currentElement;
@property (nonatomic, retain) NSMutableDictionary       *currentEntry;

- (void)parseXMLFileAtData:(NSData *)webData;
- (void) init:(MsgControl *)msgControl mainRootViewController:(RootViewController *)mainRootVC ParameterBag:(NSMutableDictionary *)parameterBag;
- (void) getData:(MsgControl *)msgControl;

-(void) flushData;

@end
