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

@property (strong, nonatomic) IBOutlet 	UILabel						*lblDepartAirport;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblDepartTime;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblDepartAMPM;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblDepartDate;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblDepartGateTerminal;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblArriveAirport;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblArriveTime;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblArriveAMPM;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblArriveDate;
@property (strong, nonatomic) IBOutlet 	UILabel						*lblArriveGateTerminal;

@property (strong, nonatomic) IBOutlet 	UILabel						*lblVendor;

@property (strong, nonatomic) IBOutlet 	UIImageView					*ivVendor;

@end