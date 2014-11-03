//
//  TripItExpenserViewController.h
//  ConcurMobile
//
//  Created by  on 6/29/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface TripItExpenserViewController : MobileViewController
{
    NSString*   cacheKey;
    int         tripitTripId;
}

@property (nonatomic, strong) NSString*     cacheKey;
@property (nonatomic, assign) int           tripitTripId;

@end
