//
//  ApproveReportsData.h
//  ConcurMobile
//
//  Created by yiwen on 1/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@class MsgControl;
@class Msg;

@interface ApproveReportsData : MsgResponderCommon {
	NSMutableDictionary     *reports;
	NSMutableArray          *keys;

	// TODO - move to MsgResponder

	NSString				*uri;
    
	// Transient data used in SAX Parsing
	bool                    isInElement;
    NSMutableDictionary     *currentReport;
    
    ActionStatus            *reportStatus;
}

@property (nonatomic, strong) NSMutableDictionary       *reports;
@property (nonatomic, strong) NSMutableArray            *keys;

@property (nonatomic, strong) NSString					*uri;

@property (nonatomic)         bool                       isInElement;
@property (nonatomic, strong) NSMutableDictionary       *currentReport;
@property (nonatomic, strong) ActionStatus              *reportStatus;


-(Msg *) newApproveMsg:(NSMutableDictionary *)parameterBag;
-(Msg *) newRejectMsg:(NSMutableDictionary *)parameterBag;

- (void) parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;

-(void) flushData;

@end
