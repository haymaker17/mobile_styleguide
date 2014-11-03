//
//  EntityRail.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/7/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityRail : NSManagedObject {
@private
}
@property (nonatomic, strong) NSString * from;
@property (nonatomic, strong) NSString * stationDepartCode;
@property (nonatomic, strong) NSString * stationArriveTZ;
@property (nonatomic, strong) NSDate * departDate;
@property (nonatomic, strong) NSString * roundTrip;
@property (nonatomic, strong) NSDate * arriveDate;
@property (nonatomic, strong) NSNumber * arriveTime;
@property (nonatomic, strong) NSString * stationArriveCode;
@property (nonatomic, strong) NSNumber * departTime;
@property (nonatomic, strong) NSString * to;
@property (nonatomic, strong) NSString * stationDepartTZ;

@end
