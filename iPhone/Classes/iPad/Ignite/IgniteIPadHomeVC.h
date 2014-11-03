//
//  IgniteIPadHomeVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 7/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "iPadHomeVC.h"
#import "IgniteTripsDS.h"
#import "IgniteTripsDelegate.h"


@interface IgniteIPadHomeVC : iPadHomeVC <IgniteTripsDelegate>
{
    UIButton            *btnShowTrip;
    UITableView         *tblTrips;
    UIButton            *btnTrips;
    IgniteTripsDS       *dsTrips;
    UIImageView         *ivTripBack;
    
    EntityTrip          *selectedTrip;
    
    UIView              *viewWaitForSalesForce;
    BOOL                isLoadingSalesForceUserData;
    
    UIImageView         *ivProfile;
    UILabel             *lblUserName;
    
    UIImageView         *ivCity;
    UIImageView         *ivCity2;
    
    BOOL                introAnimationShown; // Out of date
    BOOL                stopIntroAnimation; // Stop fading, if view is not on top
}

@property (strong, nonatomic) IBOutlet UIButton             *btnShowTrip;
@property (strong, nonatomic) IBOutlet UIButton             *btnTrips;
@property (strong, nonatomic) IBOutlet UITableView          *tblTrips;
@property (strong, nonatomic) IgniteTripsDS                 *dsTrips;
@property (strong, nonatomic) IBOutlet UIImageView          *ivTripBack;

@property (strong, nonatomic) EntityTrip                    *selectedTrip;

@property (strong, nonatomic) IBOutlet UIView               *viewWaitForSalesForce;
@property (assign, nonatomic) BOOL                          isLoadingSalesForceUserData;

@property (strong, nonatomic) IBOutlet UIImageView          *ivProfile;
@property (strong, nonatomic) IBOutlet UILabel              *lblUserName;
@property (strong, nonatomic) IBOutlet UIImageView          *ivCity;
@property (strong, nonatomic) IBOutlet UIImageView          *ivCity2;

- (IBAction)buttonShowTripPressed:(id)sender;

+ (void)resetDemoData;

@end
