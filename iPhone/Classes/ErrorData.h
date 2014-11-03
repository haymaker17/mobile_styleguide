//
//  ErrorData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "ExSystem.h" 

#import "Msg.h"


@interface ErrorData : MsgResponder 
{
	RootViewController		*rootVC;
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableDictionary		*errors, *error;
	NSMutableArray			*keys;
	int						errorCount;
}

@property (strong, nonatomic) RootViewController		*rootVC;

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*errors;
@property (nonatomic, strong) NSMutableDictionary		*error;
@property (nonatomic, strong) NSMutableArray			*keys;
@property int errorCount;

//- (void)parseXMLFileAtURL:(NSString *)URL;
//- (void)parseXML:(NSString *)XML;
- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
