//
//  SampleData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/20/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface SampleData : NSObject {
}

@property (strong, nonatomic) NSArray *logArray;
@property (strong, nonatomic) NSArray *trips;
@property (strong, nonatomic) NSMutableArray *tripKeys;

@property (strong, nonatomic) NSDictionary *trip1;
@property (strong, nonatomic) NSDictionary *trip2;
@property (strong, nonatomic) NSDictionary *trip3;

@property (strong, nonatomic) NSDictionary *flight1;
@property (strong, nonatomic) NSDictionary *flight2;
@property (strong, nonatomic) NSDictionary *flight3;

@property (strong, nonatomic) NSDictionary *car1;
@property (strong, nonatomic) NSDictionary *car2;

@property (strong, nonatomic) NSDictionary *hotel1;
@property (strong, nonatomic) NSDictionary *hotel2;

@property (strong, nonatomic) NSDictionary *itinDetails1;
@property (strong, nonatomic) NSDictionary *itinDetailsAir;

-(void) initLogs;
-(void) initTrips;

@end
