//
//  LocationSearchCellData.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "LocationSearchCellData.h"

@interface LocationSearchCellData ()
@property (nonatomic,strong) CTELocation *cteLocation;

@end

@implementation LocationSearchCellData

-(instancetype)initWithCTELocation:(CTELocation *)cteLocation
{
    self = [super init];
    if (!self) {
        return nil;
    }
    self.cellIdentifier = @"LocationSearchCell";
    self.cellHeight = 60.0 ;
    _cteLocation = cteLocation ;
    
    return self;
}
-(instancetype)initWithOfficeLocation:(OfficeLocationResult *)officeLocation
{
    self = [super init];
    if (!self) {
        return nil;
    }
    self.cellIdentifier = @"LocationSearchCell";
    self.cellHeight = 60.0 ;
    _cteLocation = [[CTELocation alloc] init];
    _cteLocation.latitude = [officeLocation.latitude doubleValue];
    _cteLocation.longitude = [officeLocation.longitude doubleValue];
    _cteLocation.location = [[officeLocation.location componentsSeparatedByCharactersInSet:[NSCharacterSet newlineCharacterSet]] componentsJoinedByString:@" "];
    _cteLocation.locationCode = officeLocation.locationCode;
    _cteLocation.countryAbbrev = officeLocation.countryAbbrev;
    _cteLocation.country = officeLocation.country;
    _cteLocation.city = officeLocation.city;
    _cteLocation.state = officeLocation.state;
    _cteLocation.zipCode = officeLocation.zipCode;
    _cteLocation.location = officeLocation.location;
    return self;
}

-(instancetype)init
{
    self = [self initWithCTELocation:nil];
    
    if (!self) {
        return nil;
    }
    return self;
}

-(CTELocation *)getCTELocation
{
    return self.cteLocation;
}

@end
