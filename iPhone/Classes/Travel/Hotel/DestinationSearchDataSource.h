//
//  DestinationSearchDataSource.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AbstractDataSource.h"

@interface DestinationSearchDataSource : AbstractDataSource

@property BOOL showCurrentLocation;

-(void)searchLocation:(NSString *)searchString;

-(void)beginEditing;
-(void)endEditing;

@end
