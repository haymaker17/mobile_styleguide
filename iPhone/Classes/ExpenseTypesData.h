//
//  ExpenseTypesData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "ExpenseTypeData.h"


@interface ExpenseTypesData : MsgResponder 
{
	NSString				*currentElement, *path, *version, *polKey;

	NSString				*isInElement;
	NSMutableDictionary		*ets;
	NSMutableArray			*keys;
	ExpenseTypeData			*et;
	NSMutableString			*buildString;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSString					*version;
@property (nonatomic, strong) NSString					*polKey;
@property (nonatomic, strong) NSMutableDictionary		*ets;
@property (nonatomic, strong) ExpenseTypeData			*et;
@property (nonatomic, strong) NSMutableArray			*keys;
@property (nonatomic, strong) NSMutableString			*buildString;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;


@end
