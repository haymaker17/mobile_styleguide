//
//  GovDocException.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocException : NSObject
{
    NSString                *name;
    NSString                *errorStatus;
    NSString                *comments;
}

@property (nonatomic, strong) NSString                  *name;
@property (nonatomic, strong) NSString                  *errorStatus;
@property (nonatomic, strong) NSString                  *comments;

@end
