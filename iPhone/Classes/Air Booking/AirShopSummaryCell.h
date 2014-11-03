//
//  AirShopSummaryCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AirShopSummaryCell : UITableViewCell
{
    UILabel     *lblAirline, *lblCost, *lblStarting, *lblResultCount;
    UIImageView *ivLogo, *ivPref;
}


@property (strong, nonatomic) IBOutlet UILabel     *lblAirline;
@property (strong, nonatomic) IBOutlet UILabel     *lblCost;
@property (strong, nonatomic) IBOutlet UILabel     *lblStarting;
@property (strong, nonatomic) IBOutlet UILabel     *lblResultCount;
@property (strong, nonatomic) IBOutlet UIImageView *ivLogo;
@property (strong, nonatomic) IBOutlet UILabel *lblTravelPoints;
@property (strong, nonatomic) IBOutlet UIImageView *ivPref;
@end
