//
//  HotelCancellationPolicyData.h
//  ConcurSDK
//
//  Created by Sally Yan on 7/28/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/**
 This class might need to re-write later.
 */
 

#import <Foundation/Foundation.h>

@interface CTEHotelCancellationPolicy : NSObject

// parse response from server for hotel cancellation policy
+(NSArray *)parseListOfHotelCancellationPolicy:(NSDictionary *)responseObject;

@end
