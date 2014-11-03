//
//  RegisterData.h
//  ConcurMobile
//
//  Created by yiwen on 4/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@interface RegisterData : MsgResponder {
	NSString				*currentElement;
	NSString				*path, *pin, *status, *errMsg;
}


@property (nonatomic, strong) NSString *path;
@property (nonatomic, strong) NSString *pin;
@property (nonatomic, strong) NSString *status;
@property (nonatomic, strong) NSString *errMsg;
@property (strong, nonatomic) NSString *currentElement;


-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody;


@end
