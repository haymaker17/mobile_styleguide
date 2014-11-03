//
//  DownloadViolationReasons.h
//  ConcurMobile
//
//  Created by ernest cho on 8/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class TravelViolationReasons;
@class ViolationReason;

@interface DownloadViolationReasons : MsgResponderCommon
{
    TravelViolationReasons  *travelViolationReasons;
    ViolationReason         *currentReason;
}

@property (nonatomic, strong) TravelViolationReasons    *travelViolationReasons;
@property (nonatomic, strong) ViolationReason           *currentReason;

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;

@end
