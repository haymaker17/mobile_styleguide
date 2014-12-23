//
//  CTEProfile.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEProfile : NSObject

@property (nonatomic,strong) NSString *firstName;
@property (nonatomic,strong) NSString *lastName;
@property (nonatomic,strong) NSString *phoneNo;
@property (nonatomic,strong) NSString *workAddress;
@property (nonatomic,strong) NSString *city;
@property (nonatomic,strong) NSString *state;
@property (nonatomic,strong) NSString *country;
@property (nonatomic,strong) NSString *zipCode;


@end
