//
//  HotelSearchCriteriaV2.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/**
 HotelSearchCriteriaV2 has the similar functionality as HotelSearchCriteria. 
 This class is being used for the new hotel search instead of HotelSearchCriteria.
 */

#import "HotelSearchCriteriaV2.h"

@implementation HotelSearchCriteriaV2

-(BOOL) isHotelSearchCriteriaValid

{
    if (!self.checkinDate || !self.checkoutDate || !self.longitude || !self.latitude) {
        return NO;
    }
    return YES;
}

@end
