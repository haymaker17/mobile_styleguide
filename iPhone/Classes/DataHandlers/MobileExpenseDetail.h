//
//  MobileExpenseDetail.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityMobileEntry.h"

@interface MobileExpenseDetail : MsgResponder
{
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	NSString				*isInElement;
	EntityMobileEntry		*oope;
    
}

@property (nonatomic, copy) NSString				*currentElement;
@property (nonatomic, strong) NSString				*path;
@property (nonatomic, strong) EntityMobileEntry		*entity;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
