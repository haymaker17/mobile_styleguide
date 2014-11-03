//
//  AttendeeGroup.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 4/3/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AttendeeGroup : NSObject
{
    NSString        *groupKey;
    NSString        *name;
}

@property(nonatomic, strong) NSString       *groupKey;
@property(nonatomic, strong) NSString       *name;

@property (weak, readonly, nonatomic) NSString	*firstName;
@property (weak, readonly, nonatomic) NSString	*lastName;

@end
