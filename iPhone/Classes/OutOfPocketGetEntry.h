//
//  OutOfPocketGetEntry.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "MsgResponder.h"
#import "Msg.h"
#import "OOPEntry.h"

@interface OutOfPocketGetEntry : MsgResponder 
{
	NSXMLParser				*dataParser;
	NSString				*currentElement, *path;
	NSString				*isInElement;
	OOPEntry				*oope;

}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) OOPEntry					*oope;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;


@end
