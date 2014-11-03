//
//  TrainStopsVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TrainStopCell.h"
#import "MobileViewController.h"
#import "MapViewController.h"
#import "RailChoiceData.h"
#import "RailChoiceSegmentData.h"
#import "RailChoiceTrainData.h"

@interface TrainStopsVC : MobileViewController <UITableViewDelegate, UITableViewDataSource> {
	NSMutableArray		*aStops;
	UITableView			*tableList;
	int selectedRow;
	
	RailChoiceData *rcd;
	RailChoiceSegmentData *segmentDepart, *segmentArrive;
	RailChoiceTrainData *train;
	
	UILabel				*lblStation, *lblTrain, *lblLegs;
}

@property (strong, nonatomic) NSMutableArray		*aStops;
@property (strong, nonatomic) IBOutlet 	UITableView			*tableList;
@property int selectedRow;

@property (strong, nonatomic) RailChoiceData *rcd;
@property (strong, nonatomic) RailChoiceSegmentData *segmentDepart;
@property (strong, nonatomic) RailChoiceSegmentData *segmentArrive;
@property (strong, nonatomic) RailChoiceTrainData *train;

@property (strong, nonatomic) IBOutlet 	UILabel		*lblStation;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblTrain;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblLegs;

-(void) initList;
-(IBAction)cancelView:(id)sender;
-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode;

@end
