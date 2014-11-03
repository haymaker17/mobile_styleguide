//
//  TripItCacheData.h
//  ConcurMobile
//
//  Created by  on 3/22/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"

@class MsgControl;
@class Msg;
@class TripItEmailAddress;

@interface TripItCacheData : MsgResponderCommon{
    NSMutableArray      *emailAddressList;
    TripItEmailAddress  *currentEmailAddress;
    NSString            *loginIdOfLinkedAccount;
    NSString            *tripId;
    NSString            *tripItOAuthRequestToken;
}

@property (nonatomic, strong) NSMutableArray        *emailAddressList;
@property (nonatomic, strong) TripItEmailAddress    *currentEmailAddress;
@property (nonatomic, strong) NSString              *loginIdOfLinkedAccount;
@property (nonatomic, strong) NSString              *tripId;
@property (nonatomic, strong) NSString              *tripItOAuthRequestToken;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(NSArray*) getPreVerifiedUserIds;

@end


@interface TripItEmailAddress : NSObject{
    NSString*   emailAddress;
    BOOL        emailAddressMatchesLoginId;
}

@property (nonatomic, strong) NSString* emailAddress;
@property (nonatomic, assign) BOOL      emailAddressMatchesLoginId;

@end
