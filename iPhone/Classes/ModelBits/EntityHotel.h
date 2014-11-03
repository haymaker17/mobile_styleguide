//
//  EntityHotel.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/7/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityHotel : NSManagedObject {
@private
}
@property (nonatomic, strong) NSString * state;
@property (nonatomic, strong) NSString * isMetricDistance;
@property (nonatomic, strong) NSString * longitude;
@property (nonatomic, strong) NSDate * checkinDate;
@property (nonatomic, strong) NSString * latitude;
@property (nonatomic, strong) NSString * containingWords;
@property (nonatomic, strong) NSDate * checkoutDate;
@property (nonatomic, strong) NSString * smokingPreferenceCode;
@property (nonatomic, strong) NSString * location;
@property (nonatomic, strong) NSString * city;
@property (nonatomic, strong) NSString * distance;
@property (nonatomic, strong) NSString * country;

@end
