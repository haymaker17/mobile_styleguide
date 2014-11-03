//
//  HotelCancel.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@interface HotelCancel : MsgResponder {
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	
	BOOL					isInElement, isSuccess;
	NSMutableDictionary		*items;
	NSMutableArray			*keys;
	NSObject				*obj;
	
}
@property BOOL isSuccess;
@property (nonatomic, strong) NSString                  *errorMessage;
@property (nonatomic, strong) NSString                  *cancellationNumber;
@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) NSObject					*obj;
@property (nonatomic, strong) NSMutableArray			*keys;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;
@end
