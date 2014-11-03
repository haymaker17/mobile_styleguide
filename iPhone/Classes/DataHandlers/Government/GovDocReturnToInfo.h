//
//  GovDocReturnToInfo.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocReturnToInfo : NSObject
{
    NSString            *returnToId;
    NSString            *returnToSSN;
    NSString            *returnToName;
}

@property (nonatomic, strong) NSString              *returnToId;
@property (nonatomic, strong) NSString              *returnToSSN;
@property (nonatomic, strong) NSString              *returnToName;

@end
