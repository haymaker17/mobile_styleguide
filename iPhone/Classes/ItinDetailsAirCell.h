//
//  ItinDetailsAirCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/11/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItinDetailsViewController;

@interface ItinDetailsAirCell : UITableViewCell 
{
	UILabel *labelDepartAirport, *labelDepartAirportLabel;
	UILabel *labelDepartTerminal, *labelDepartTerminalLabel;
	UILabel *labelDepartGate, *labelDepartGateLabel;
	UILabel *labelArriveAirport, *labelArriveAirportLabel;
	UILabel *labelStatus, *labelStatusLabel;
	UILabel *labelDuration, *labelDurationLabel;
	UILabel *labelStops, *labelStopsLabel;
	UILabel *labelTicketing, *labelTicketingLabel;
	UILabel *labelSeat, *labelSeatLabel;
	UILabel *labelPrice, *labelPriceLabel, *labelDepartArrive;
	UILabel *labelFareClass, *labelFareClassLabel;
	UILabel *labelAirplane, *labelAirplaneLabel;
	UIButton *btnAction;
	ItinDetailsViewController *rootVC;

}

@property (nonatomic, retain) IBOutlet UILabel *labelDepartAirport;
@property (nonatomic, retain) IBOutlet UILabel *labelDepartAirportLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelDepartTerminal;
@property (nonatomic, retain) IBOutlet UILabel *labelDepartTerminalLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelArriveAirport;
@property (nonatomic, retain) IBOutlet UILabel *labelArriveAirportLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelStatus;
@property (nonatomic, retain) IBOutlet UILabel *labelStatusLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelDuration;
@property (nonatomic, retain) IBOutlet UILabel *labelDurationLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelStops;
@property (nonatomic, retain) IBOutlet UILabel *labelStopsLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelTicketing;
@property (nonatomic, retain) IBOutlet UILabel *labelTicketingLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelSeat;
@property (nonatomic, retain) IBOutlet UILabel *labelSeatLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelPrice;
@property (nonatomic, retain) IBOutlet UILabel *labelPriceLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelDepartArrive;

@property (nonatomic, retain) IBOutlet UILabel *labelFareClass;
@property (nonatomic, retain) IBOutlet UILabel *labelFareClassLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelAirplane;
@property (nonatomic, retain) IBOutlet UILabel *labelAirplaneLabel;

@property (nonatomic, retain) IBOutlet UILabel *labelDepartGate;
@property (nonatomic, retain) IBOutlet UILabel *labelDepartGateLabel;

@property (nonatomic, retain) IBOutlet UIButton *btnAction;

@property (nonatomic, retain) ItinDetailsViewController *rootVC;

- (IBAction)buttonDrillPressed:(id)sender;

@end
