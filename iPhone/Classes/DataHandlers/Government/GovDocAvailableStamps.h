//
//  GovDocAvailableStamps.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocAvailableStamps : NSObject
{
    NSNumber                *sigRequired;
    NSString                *travelerId;
    NSString                *docType;
    NSString                *docName;
    NSString                *userId;

    NSMutableDictionary     *stampInfoList;
    NSMutableArray          *returnToInfoList;
}

@property (nonatomic, strong) NSString              *travelerId;
@property (nonatomic, strong) NSString              *docType;
@property (nonatomic, strong) NSString              *docName;
@property (nonatomic, strong) NSString              *userId;
@property (nonatomic, strong) NSNumber              *sigRequired;
@property (nonatomic, strong) NSMutableDictionary   *stampInfoList;
@property (nonatomic, strong) NSMutableArray        *returnToInfoList;

@end
