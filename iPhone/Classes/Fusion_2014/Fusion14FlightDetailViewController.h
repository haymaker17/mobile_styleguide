//
//  Fusion14FlightDetailViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/24/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExMsgRespondDelegate.h"
#import "AirShop.h"
#import "EntityAirShopResults.h"
#import "AirFilterManager.h"
#import "EntityAirFilterSummary.h"
#import "EntityAirFilter.h"
#import "EntityAirRules.h"
#import "AirRuleManager.h"
#import "CreditCard.h"
#import "PreSellOptions.h"
#import "OverlayView2.h"

@interface Fusion14FlightDetailViewController : UITableViewController <ExMsgRespondDelegate, NSFetchedResultsControllerDelegate, OverlayClickDelegate>

//@property (weak, nonatomic) IBOutlet UILabel *lblFlightDetailHeader;
//@property (weak, nonatomic) IBOutlet UILabel *lblDepartToCitySummaryText;
//@property (weak, nonatomic) IBOutlet UILabel *lblDividerLine;
//@property (weak, nonatomic) IBOutlet UILabel *lblDepartureAirportCode;
//@property (weak, nonatomic) IBOutlet UILabel *lblArrivalAirportCode;
//@property (weak, nonatomic) IBOutlet UILabel *lblDepartureTime;
//@property (weak, nonatomic) IBOutlet UILabel *lblArrivalTime;
//@property (weak, nonatomic) IBOutlet UILabel *lblPrice;
//@property (weak, nonatomic) IBOutlet UIImageView *ivAirlineLogo;
//@property (weak, nonatomic) IBOutlet UILabel *lblAirlineName;
//@property (weak, nonatomic) IBOutlet UILabel *lblFlightDuration;
//@property (weak, nonatomic) IBOutlet UILabel *lblNumberofStops;
//@property (weak, nonatomic) IBOutlet UILabel *lblDepartureCityName;
//@property (weak, nonatomic) IBOutlet UILabel *lblArrivalCityName;
//
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnToCitySummaryText;
//
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnDepartureAirportCode;
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnArrivalAirportCode;
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnDepartureTime;
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnArrivalTime;
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnFlightDuration;
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnNumberOfStops;
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnDepartureCityName;
//@property (weak, nonatomic) IBOutlet UILabel *lblReturnArrivalCityName;
//@property (weak, nonatomic) IBOutlet UIImageView *ivReturnAirlineLogo;
//
//@property (weak, nonatomic) IBOutlet UILabel *lblPointsEarnedText;
//@property (weak, nonatomic) IBOutlet UILabel *lblRoundTripText;
//@property (weak, nonatomic) IBOutlet UILabel *lblTravelPoints;
//@property (weak, nonatomic) IBOutlet UILabel *lblNumberofTravellers;
//@property (weak, nonatomic) IBOutlet UILabel *lblFinalTripSummary;
//
//@property (weak, nonatomic) IBOutlet UILabel *lblPaymentDetails;
//@property (weak, nonatomic) IBOutlet UILabel *lblCreditCardLastfour;
//
//
//@property (weak, nonatomic) IBOutlet UIImageView *ivTravelPointsLogo;

@property (nonatomic, strong)NSString           *travelDate;
@property (nonatomic, strong)NSString           *flightToCity;
@property (nonatomic, strong)NSString           *returnToCity;

@property (nonatomic, strong) EntityAirShopResults                  *airShopResults;
@property (nonatomic, strong) EntityAirFilterSummary                *airSummary;
@property (nonatomic, strong) AirShop                               *airShop;


@end
