//
//  Fusion14FlightDetailsCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 4/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14FlightDetailsCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *lblDepartToCitySummaryText;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureTime;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalTime;
@property (weak, nonatomic) IBOutlet UIImageView *ivAirlineLogo;
@property (weak, nonatomic) IBOutlet UILabel *lblAirlineName;
@property (weak, nonatomic) IBOutlet UILabel *lblFlightDuration;
@property (weak, nonatomic) IBOutlet UILabel *lblNumberofStops;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureCityName;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalCityName;

@end
