//
//  CTEHotelViolationReason.h
//  ConcurSDK
//
//  Created by ernest cho on 10/10/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEHotelViolationReason : NSObject

@property (nonatomic, readonly, strong) NSNumber *violationId;
@property (nonatomic, readonly, strong) NSString *reasonCode;
@property (nonatomic, readonly, strong) NSString *justification;

- (id)initWithViolationId:(NSNumber *)violationId reasonCode:(NSString *)reasonCode justification:(NSString *)justification;

@end
