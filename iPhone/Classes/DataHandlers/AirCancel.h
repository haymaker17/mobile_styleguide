//
//  AirCancel.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/16/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@interface AirCancel : MsgResponder {
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	BOOL					isInElement, isSuccess;
	NSMutableDictionary		*items;
	NSMutableArray			*keys;
	NSObject				*obj;
	
}
@property BOOL isSuccess;
@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) NSObject					*obj;
@property (nonatomic, strong) NSMutableArray			*keys;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;


@end
