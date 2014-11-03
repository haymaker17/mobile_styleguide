//
//  GovFindLocation.h
//  ConcurMobile
//
//  Created by Shifan Wu on 12/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "FindLocation.h"

@interface GovFindDutyLocation : FindLocation

-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;

@end
