//
//  AirShopFilterCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/9/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AirShopFilterCell : UITableViewCell
{
    UILabel     *lblAirline, *lblCost, *lblStarting, *lblResultCount;
    UIImageView *ivLogo, *ivPref, *ivOvernight, *ivRoundOvernight, *ivRule, *ivRefundable;
    
    UILabel     *lblDepartIata, *lblDepartTime, *lblArriveIata, *lblArriveTime, *lblDurationStops;
    UILabel     *lblRoundDepartIata, *lblRoundDepartTime, *lblRoundArriveIata, *lblRoundArriveTime, *lblRoundDurationStops, *lblRefundable, *lblGdsName;
}

@property (strong, nonatomic) IBOutlet UILabel     *lblRefundable;
@property (strong, nonatomic) IBOutlet UILabel     *lblAirline;
@property (strong, nonatomic) IBOutlet UILabel     *lblCost;
@property (strong, nonatomic) IBOutlet UILabel     *lblStarting;
@property (strong, nonatomic) IBOutlet UILabel     *lblResultCount;
@property (strong, nonatomic) IBOutlet UIImageView *ivLogo;
@property (strong, nonatomic) IBOutlet UIImageView *ivPref;
@property (strong, nonatomic) IBOutlet UIImageView *ivOvernight;
@property (strong, nonatomic) IBOutlet UIImageView *ivRoundOvernight;
@property (strong, nonatomic) IBOutlet UIImageView *ivRule;
@property (strong, nonatomic) IBOutlet UIImageView *ivRefundable;
@property (strong, nonatomic) IBOutlet UILabel *lblTravelPoints;

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
@property (strong, nonatomic) IBOutlet UILabel     *lblGdsName;
@end