//
//  AffinityProgram.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AffinityProgram : NSObject
{
    NSString        *accountNumber;
    NSString        *description;
    NSString        *programName;
    NSString        *programType;
    NSString        *programId;
    NSString        *vendor;
    NSString        *vendorAbbrev;
}

@property (nonatomic, strong) NSString					*accountNumber;
@property (nonatomic, strong) NSString					*description;
@property (nonatomic, strong) NSString					*programName;
@property (nonatomic, strong) NSString					*programType;
@property (nonatomic, strong) NSString					*programId;
@property (nonatomic, strong) NSString					*vendor;
@property (nonatomic, strong) NSString					*vendorAbbrev;

+ (NSMutableDictionary*) getXmlToPropertyMap;

@end
