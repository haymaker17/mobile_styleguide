//
//  MobileExpenseList.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ArchivedResponder.h"
#import "Msg.h"
#import "MsgResponder.h"
#import "EntityMobileEntry.h"

#import "ImageUtil.h"

@interface MobileExpenseList : MsgResponder <ArchivedResponder>
{
	NSString				*currentElement, *path;
	NSMutableString			*buildString;
	NSString				*isInElement;
	EntityMobileEntry		*entity;
	BOOL					isInCard;
	BOOL					isInOOP;
	BOOL					isInCorpCard;
    BOOL                    isInReceiptCapture;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) EntityMobileEntry			*entity;
@property (nonatomic, strong) NSMutableString			*buildString;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;


@end
