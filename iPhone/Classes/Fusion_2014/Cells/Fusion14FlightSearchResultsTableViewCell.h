//
//  Fusion14FlightSearchResultsTableViewCell.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14FlightSearchResultsTableViewCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *lblDividerLine;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureTime;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalTime;
@property (weak, nonatomic) IBOutlet UILabel *lblPrice;
@property (weak, nonatomic) IBOutlet UIImageView *ivAirlineLogo;
@property (weak, nonatomic) IBOutlet UILabel *lblAirlineName;
@property (weak, nonatomic) IBOutlet UILabel *lblFlightDuration;
@property (weak, nonatomic) IBOutlet UILabel *lblNumberofStops;

@property (weak, nonatomic) IBOutlet UILabel *lblReturnDepartureAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblReturnArrivalAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblReturnDepartureTime;
@property (weak, nonatomic) IBOutlet UILabel *lblReturnArrivalTime;
@property (weak, nonatomic) IBOutlet UILabel *lblReturnFlightDuration;
@property (weak, nonatomic) IBOutlet UILabel *lblReturnNumberOfStops;

@property (weak, nonatomic) IBOutlet UILabel *lblRecommended;
@property (weak, nonatomic) IBOutlet UILabel *lblTravelPoints;

@end
