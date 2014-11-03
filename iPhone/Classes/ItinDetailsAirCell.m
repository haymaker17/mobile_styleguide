//
//  ItinDetailsAirCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/11/09.
//  Copyright 2009 Concur. All rights reserved.
//

#import "ItinDetailsAirCell.h"


@implementation ItinDetailsAirCell

@synthesize labelDepartAirport;
@synthesize labelDepartAirportLabel;
@synthesize labelDepartTerminal;
@synthesize labelDepartTerminalLabel;
@synthesize labelArriveAirport;
@synthesize labelArriveAirportLabel;
@synthesize labelStatus;
@synthesize labelStatusLabel;
@synthesize labelDuration;
@synthesize labelDurationLabel;
@synthesize labelStops;
@synthesize labelStopsLabel;
@synthesize labelTicketing;
@synthesize labelTicketingLabel;
@synthesize labelSeat;
@synthesize labelSeatLabel;
@synthesize labelPrice;
@synthesize labelPriceLabel;
@synthesize btnAction;
@synthesize rootVC;
@synthesize labelDepartArrive;

@synthesize labelFareClass;
@synthesize labelFareClassLabel;
@synthesize labelAirplane;
@synthesize labelAirplaneLabel;

@synthesize labelDepartGate;
@synthesize labelDepartGateLabel;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}

- (IBAction)buttonDrillPressed:(id)sender
{
	//[rootVC switchViews:sender ParameterBag:nil];	
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc 
{
	[ labelDepartAirport release];
	[ labelDepartAirportLabel release];
	[ labelDepartTerminal release];
	[ labelDepartTerminalLabel release];
	[ labelArriveAirport release];
	[ labelArriveAirportLabel release];
	[ labelStatus release];
	[ labelStatusLabel release];
	[ labelDuration release];
	[ labelDurationLabel release];
	[ labelStops release];
	[ labelStopsLabel release];
	[ labelTicketing release];
	[ labelTicketingLabel release];
	[ labelSeat release];
	[ labelSeatLabel release];
	[ labelPrice release];
	[ labelPriceLabel release];
	[labelDepartArrive release];
	[btnAction release];
	[ labelDepartGate release];
	[ labelDepartGateLabel release];
	
	[ labelFareClass release];
	[ labelFareClassLabel release];
	[ labelAirplane release];
	[ labelAirplaneLabel release];
	
	[rootVC release];
    [super dealloc];
}


@end
