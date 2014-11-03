//
//  EntitySegmentLocation.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntitySegment;

@interface EntitySegmentLocation : NSManagedObject

@property (nonatomic, strong) NSString * address;
@property (nonatomic, strong) NSString * airportCity;
@property (nonatomic, strong) NSString * airportCountry;
@property (nonatomic, strong) NSString * airportCountryCode;
@property (nonatomic, strong) NSString * airportName;
@property (nonatomic, strong) NSString * airportState;
@property (nonatomic, strong) NSString * city;
@property (nonatomic, strong) NSString * cityCode;
@property (nonatomic, strong) NSString * cityCodeLocalized;
@property (nonatomic, strong) NSString * dateLocal;
@property (nonatomic, strong) NSString * dateUtc;
@property (nonatomic, strong) NSString * gate;
@property (nonatomic, strong) NSNumber * latitude;
@property (nonatomic, strong) NSNumber * longitude;
@property (nonatomic, strong) NSString * platform;
@property (nonatomic, strong) NSString * postalCode;
@property (nonatomic, strong) NSString * railStation;
@property (nonatomic, strong) NSString * railStationLocalized;
@property (nonatomic, strong) NSString * state;
@property (nonatomic, strong) NSString * terminal;
@property (nonatomic, strong) NSString * address2;
@property (nonatomic, strong) NSString * location;
@property (nonatomic, strong) NSString * country;
@property (nonatomic, strong) EntitySegment *relSegmentEnd;
@property (nonatomic, strong) EntitySegment *relSegmentStart;

@end
