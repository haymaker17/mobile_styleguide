//
//  AirShopFilteredCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AirShopFilteredCell : UITableViewCell
{
    UILabel     *lblAirline, *lblCost, *lblStarting, *lblResultCount;
    UIImageView *ivLogo, *ivPref, *ivOvernight;
    
    UILabel     *lblDepartIata, *lblDepartTime, *lblArriveIata, *lblArriveTime, *lblDurationStops;
    UILabel     *lblRoundDepartIata, *lblRoundDepartTime, *lblRoundArriveIata, *lblRoundArriveTime, *lblRoundDurationStops, *lblOperatedBy;
    
    UIView      *viewDetails;
}

@property (strong, nonatomic) IBOutlet UIView       *viewDetails;
@property (strong, nonatomic) IBOutlet UILabel     *lblAirline;
@property (strong, nonatomic) IBOutlet UILabel     *lblCost;
@property (strong, nonatomic) IBOutlet UILabel     *lblStarting;
@property (strong, nonatomic) IBOutlet UILabel     *lblResultCount;
@property (strong, nonatomic) IBOutlet UIImageView *ivLogo;
@property (strong, nonatomic) IBOutlet UIImageView *ivPref;
@property (strong, nonatomic) IBOutlet UIImageView *ivOvernight;

@property (strong, nonatomic) IBOutlet UILabel     *lblDepartIata;
@property (strong, nonatomic) IBOutlet UILabel     *lblDepartTime;
@property (strong, nonatomic) IBOutlet UILabel     *lblArriveIata;
@property (strong, nonatomic) IBOutlet UILabel     *lblArriveTime;
@property (strong, nonatomic) IBOutlet UILabel     *lblDurationStops;
@property (strong, nonatomic) IBOutlet UILabel     *lblRoundDepartIata;
@property (strong, nonatomic) IBOutlet UILabel     *lblRoundDepartTime;
@property (strong, nonatomic) IBOutlet UILabel     *lblRoundArriveIata;
@property (strong, nonatomic) IBOutlet UILabel     *lblRoundArriveTime;
@property (strong, nonatomic) IBOutlet UILabel     *lblRoundDurationStops;

@property (strong, nonatomic) IBOutlet UILabel     *lblOperatedBy;
@end
