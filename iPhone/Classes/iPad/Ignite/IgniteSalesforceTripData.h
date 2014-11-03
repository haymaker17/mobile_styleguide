//
//  IgniteSalesforceTripData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface IgniteSalesforceTripData : MsgResponder
{
    NSString    *cliqbookTripId;    // Passed in by the creator of the IgniteSalesforceTripData object
    NSString    *salesforceTripId;  // Determined from HTTP response
}

@property (nonatomic, strong) NSString  *cliqbookTripId;
@property (nonatomic, strong) NSString  *salesforceTripId;
@property (nonatomic, strong) NSManagedObjectContext *managedObjectContext;

@end
