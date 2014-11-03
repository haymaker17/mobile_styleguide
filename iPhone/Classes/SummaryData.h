//
//  SummaryData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "PersonalCardData.h"

@interface SummaryData : MsgResponder 
{
	NSString				*currentElement, *path;

	NSString				*isInElement;
	NSMutableDictionary		*dict;

}


@property (nonatomic, copy) NSString					*currentElement;
@property (nonatomic, strong) NSString					*path;
@property (nonatomic, strong) NSMutableDictionary		*dict;
@property (nonatomic, strong) NSString                  *isInElement;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;

-(void) flushData;


@end
