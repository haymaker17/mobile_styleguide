//
//  TripAirSegmentCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface TripAirSegmentCell : UITableViewCell {
	UILabel						*lblDepartAirport, *lblDepartTime, *lblDepartAMPM, *lblDepartDate, *lblDepartGateTerminal;
	UILabel						*lblArriveAirport, *lblArriveTime, *lblArriveAMPM, *lblArriveDate, *lblArriveGateTerminal;
	UILabel						*lblVendor;
	UIImageView					*ivVendor;
}

@property (retain, nonatomic) IBOutlet 	UILabel						*lblDepartAirport;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblDepartTime;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblDepartAMPM;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblDepartDate;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblDepartGateTerminal;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblArriveAirport;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblArriveTime;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblArriveAMPM;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblArriveDate;
@property (retain, nonatomic) IBOutlet 	UILabel						*lblArriveGateTerminal;

@property (retain, nonatomic) IBOutlet 	UILabel						*lblVendor;

@property (retain, nonatomic) IBOutlet 	UIImageView					*ivVendor;

@end
