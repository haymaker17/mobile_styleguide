//
//  TripAirVendorCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TripAirVendorCell : UITableViewCell {
	UILabel				*lblAirFlightNum, *lblOperatedByAirFlightNum;
	UIImageView			*ivVendor, *ivOperatedByVendor, *ivBackground;
}

@property (strong, nonatomic) IBOutlet UILabel				*lblAirFlightNum;
@property (strong, nonatomic) IBOutlet UILabel				*lblOperatedByAirFlightNum;
@property (strong, nonatomic) IBOutlet UIImageView			*ivVendor;
@property (strong, nonatomic) IBOutlet UIImageView			*ivOperatedByVendor;
@property (strong, nonatomic) IBOutlet UIImageView			*ivBackground;

@end
