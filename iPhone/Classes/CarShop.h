//
//  CarShop.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@class Car;


@interface CarShop : NSObject
{
	NSString				*pickupIata;
	NSDate					*pickupDate;
	NSString				*dropoffIata;
	NSDate					*dropoffDate;
	NSMutableArray			*carResults;
	NSMutableDictionary		*carChains;
	NSMutableDictionary		*carDescriptions;
	NSMutableDictionary		*carLocations;
	NSMutableArray			*cars;
}


@property (nonatomic, strong) NSString				*pickupIata;
@property (nonatomic, strong) NSDate				*pickupDate;
@property (nonatomic, strong) NSString				*dropoffIata;
@property (nonatomic, strong) NSDate				*dropoffDate;
@property (nonatomic, strong) NSMutableArray		*carResults;
@property (nonatomic, strong) NSMutableDictionary	*carChains;
@property (nonatomic, strong) NSMutableDictionary	*carDescriptions;
@property (nonatomic, strong) NSMutableDictionary	*carLocations;
@property (nonatomic, strong) NSMutableArray		*cars;


-(void)didPopulate;


@end
