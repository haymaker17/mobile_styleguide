//
//  GovLocationsData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "GovLocation.h"

@interface GovLocationsData : MsgResponderCommon
{
    NSString                *countryCode;
    NSString                *stateCode;
    NSString                *city;
    NSString                *latitude;
    NSString                *longitude;
    NSString                *zipCode; // zipCode, or (lat, lon)
    NSDecimalNumber         *range;
    
    NSMutableArray          *locations;
    GovLocation             *currentLoc;
}

@property (nonatomic, strong) NSMutableArray            *locations;
@property (nonatomic, strong) GovLocation               *currentLoc;
@property (nonatomic, strong) NSString                  *countryCode;
@property (nonatomic, strong) NSString                  *city;
@property (nonatomic, strong) NSString                  *stateCode;
@property (nonatomic, strong) NSString                  *latitude;
@property (nonatomic, strong) NSString                  *longitude;
@property (nonatomic, strong) NSString                  *zipCode;
@property (nonatomic, strong) NSDecimalNumber           *range;

@end
