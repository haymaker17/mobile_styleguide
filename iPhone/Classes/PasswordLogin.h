//
//  PasswordLogin.h
//  ConcurMobile
//
//  Created by yiwen on 4/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"

@interface PasswordLogin : MsgResponder {
	NSString				*currentElement;
	
	NSString				*path, *userName, *password, *regSessionID, *authenticated, *entityType;
	NSString				*timedOut;
	
}


@property (nonatomic, strong) NSString *path;
@property (nonatomic, strong) NSString *userName;
@property (nonatomic, strong) NSString *password;
@property (nonatomic, strong) NSString *regSessionID;
@property (nonatomic, strong) NSString *authenticated;
@property (nonatomic, strong) NSString *entityType;
@property (strong, nonatomic) NSString *timedOut;
@property (strong, nonatomic) NSString *currentElement;


-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *)makeXMLBody;


@end
