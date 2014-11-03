//
//  CorpSSOQueryData.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 3/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"

@interface CorpSSOQueryData : MsgResponderCommon {
    NSString            *companyCode;
    NSMutableString     *ssoUrl;
    BOOL                isSSOEnabled;
    NSString            *status;
    NSString            *serverUrl;
}

@property (nonatomic, strong) NSString          *companyCode;
@property (nonatomic, strong) NSMutableString   *ssoUrl;
@property (nonatomic, strong) NSString          *status;
@property BOOL isSSOEnabled;
@property (nonatomic, strong) NSString          *serverUrl;

-(id)init;
-(NSString *)makeXMLBody;
@end
