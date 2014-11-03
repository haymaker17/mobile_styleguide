//
//  GovDocAvailableStampsData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GovDocAvailableStamps.h"
#import "GovDocStampInfo.h"
#import "GovDocReturnToInfo.h"

@interface GovDocAvailableStampsData : MsgResponderCommon
{
    NSString                        *travelerId;
    NSString                        *docType;
    NSString                        *docName;

    GovDocAvailableStamps           *availStamps;
    GovDocStampInfo                 *stampInfo;
    GovDocReturnToInfo              *returnToInfo;
    BOOL                            inReturnToInfo, inStampInfo;
}

@property (nonatomic, strong) NSString                          *travelerId;
@property (nonatomic, strong) NSString                          *docType;
@property (nonatomic, strong) NSString                          *docName;
@property (nonatomic, strong) GovDocAvailableStamps             *availStamps;
@property (nonatomic, strong) GovDocStampInfo                   *stampInfo;
@property (nonatomic, strong) GovDocReturnToInfo                *returnToInfo;

- (Msg*) newMsg:(NSMutableDictionary*)parameterBag;

@end
