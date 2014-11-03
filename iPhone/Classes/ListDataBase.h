//
//  ListDataBase.h
//  ConcurMobile
//
//  Created by yiwen on 8/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"

@interface ListDataBase : MsgResponder 
{
	NSMutableDictionary		*objDict;
	NSMutableArray			*keys;
	BOOL					 isInList;
}

@property (nonatomic, strong) NSMutableDictionary		*objDict;
@property (nonatomic, strong) NSMutableArray			*keys;
@property					  BOOL						 isInList;

-(void) flushData;

@end
