//
//  CurrencyData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Currency.h"
#import "Msg.h"

@interface CurrencyData : MsgResponder 
{
	NSString				*currentElement, *path;
	
	NSString				*isInElement;
	NSMutableDictionary		*dataDict;
	Currency				*objData;
	NSMutableArray			*keys;
	int						errorCount;
}

@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*dataDict;
@property (nonatomic, strong) Currency					*objData;
@property (nonatomic, strong) NSMutableArray			*keys;
@property int errorCount;

- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
