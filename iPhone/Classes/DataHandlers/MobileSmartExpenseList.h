//
//  MobileSmartExpenseList.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 9/30/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MobileEntryManager.h"

@interface MobileSmartExpenseList : MsgResponder <ArchivedResponder>


- (void)parseXMLFileAtData:(NSData *)webData;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end
