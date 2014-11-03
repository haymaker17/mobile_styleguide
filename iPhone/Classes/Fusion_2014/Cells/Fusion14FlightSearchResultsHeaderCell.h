//
//  Fusion14FlightSearchResultsHeaderCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 4/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14FlightSearchResultsHeaderCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *lblDepartureAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureCity;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalCity;

@property (weak, nonatomic) IBOutlet UILabel *lblTravelDates;
@property (weak, nonatomic) IBOutlet UIButton *btnPriceToBeat;
- (IBAction)btnPriceToBeatTapped:(id)sender;

@end
