//
//  Fusion14FlightTripDetailsCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 4/26/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14FlightTripDetailsCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *lblRoundTripText;
@property (weak, nonatomic) IBOutlet UILabel *lblTravelPoints;
@property (weak, nonatomic) IBOutlet UILabel *lblTravelEarnedText;
@property (weak, nonatomic) IBOutlet UILabel *lblNumberofTravellers;
@property (weak, nonatomic) IBOutlet UILabel *lblFinalTripSummary;
@property (weak, nonatomic) IBOutlet UILabel *lblPrice;

@end
