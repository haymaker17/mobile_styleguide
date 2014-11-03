//
//  CarType.h
//  ConcurMobile
//
//  Created by Shifan Wu on 9/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CarType : NSObject
{
    NSString *carTypeCode;
    NSString *carTypeName;
}

@property (nonatomic, strong) NSString              *carTypeCode;
@property (nonatomic, strong) NSString              *carTypeName;

@end
