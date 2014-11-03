//
//  MobileExpneseSave.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Msg.h"
#import "MsgResponder.h"
#import "EntityMobileEntry.h"

@interface MobileExpenseSave : MsgResponder
{
	NSString				*currentElement, *path;
    
	NSString				*isInElement, *returnStatus, *meKey;
	EntityMobileEntry				*entry;
}

@property (nonatomic, copy) NSString				*currentElement;
@property (nonatomic, strong) NSString				*path;
@property (nonatomic, strong) EntityMobileEntry		*entry;
@property (nonatomic, strong) NSString				*returnStatus;
@property (nonatomic, strong) NSString				*meKey;


- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
