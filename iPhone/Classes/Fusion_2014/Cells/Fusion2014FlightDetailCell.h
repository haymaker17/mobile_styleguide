//
//  Fusion2014FlightDetailCell.h
//  ConcurMobile
//
//  Created by Shifan Wu on 4/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion2014FlightDetailCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *lblSegmentTitle;
@property (weak, nonatomic) IBOutlet UILabel *lblTitleDividerLine;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureTime;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalTime;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalAirportCode;
@property (weak, nonatomic) IBOutlet UILabel *lblDepartureCity;
@property (weak, nonatomic) IBOutlet UILabel *lblArrivalCity;
@property (weak, nonatomic) IBOutlet UILabel *lblflighSummaryDividerline;
@property (weak, nonatomic) IBOutlet UIImageView *ivAirlineLogo;
@property (weak, nonatomic) IBOutlet UILabel *lblFlightNumber;
@property (weak, nonatomic) IBOutlet UILabel *lblFlightDuration;
@property (weak, nonatomic) IBOutlet UILabel *lblNumberofStops;
@property (weak, nonatomic) IBOutlet UILabel *lblTerminalNumber;
@property (weak, nonatomic) IBOutlet UILabel *lblGateNumber;
@property (weak, nonatomic) IBOutlet UILabel *lblSeatNumber;
@property (weak, nonatomic) IBOutlet UILabel *lblTerminalText;
@property (weak, nonatomic) IBOutlet UILabel *lblGateText;
@property (weak, nonatomic) IBOutlet UILabel *lblSeatText;
@property (weak, nonatomic) IBOutlet UILabel *lblConfirmationNumber;
@property (weak, nonatomic) IBOutlet UILabel *lblFinalDividerLine;
@property (weak, nonatomic) IBOutlet UIButton *btnCancel;
@property (weak, nonatomic) IBOutlet UIButton *btnChange;
@property (weak, nonatomic) IBOutlet UILabel *lblVerticalTripContinueline;

@end
