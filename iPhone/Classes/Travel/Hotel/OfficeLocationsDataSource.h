//
//  OfficeLocationsDataSource.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 06/08/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AbstractDataSource.h"

@interface OfficeLocationsDataSource : AbstractDataSource

@property (nonatomic, strong) NSArray			*officeLocations;
@property (nonatomic, strong) NSMutableArray	*filteredOfficeLocations;

-(instancetype)initWithOfficeLocationArray:(NSArray *)officeLocations;
-(void)searchLocation:(NSString *)searchString;

@end