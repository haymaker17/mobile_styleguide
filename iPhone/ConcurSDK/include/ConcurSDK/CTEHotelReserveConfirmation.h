//
//  CTEHotelReserveConfirmation.h
//  ConcurSDK
//
//  Created by Shifan Wu on 6/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEHotelReserveConfirmation : NSObject

@property (nonatomic, readonly, strong) NSString *recordLocator;
@property (nonatomic, readonly, strong) NSString *itineraryLocator;
@property (nonatomic, readonly, strong) NSString *confirmationNumber;

- (id)initWithJSON:(NSDictionary *)json;

@end
