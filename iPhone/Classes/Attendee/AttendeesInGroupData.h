//
//  AttendeesInGroupData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/2/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AttendeeBaseData.h"

@interface AttendeesInGroupData : AttendeeBaseData
{
    NSString        *groupKey;
}

@property (nonatomic, strong) NSString			*groupKey;

@end
