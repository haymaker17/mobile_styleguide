//
//  TrainBookListingVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TrainBooking.h"
#import "RailChoiceData.h"
#import "RailChoiceSegmentData.h"
#import "RailChoiceTrainData.h"

@interface TrainBookListingVC : MobileViewController <UITableViewDelegate, UITableViewDataSource, UIAlertViewDelegate> {
	NSMutableArray		*aList;
	UITableView			*tableList;
	int					selectedRow, numItems;
	TrainBooking		*trainBooking;
	BOOL				isReturn, isRound;
	NSString			*stationDepart, *stationArrive;
	UIView				*noView;
}

@property (strong, nonatomic) NSMutableArray	*aList;
@property (strong, nonatomic) IBOutlet 	UITableView			*tableList;
@property int									selectedRow;
@property int									numItems;
@property (strong, nonatomic) TrainBooking		*trainBooking;
@property BOOL									isReturn;
@property BOOL									isRound;
@property (strong, nonatomic) NSString			*stationDepart;
@property (strong, nonatomic) NSString			*stationArrive;
@property (strong, nonatomic) IBOutlet 	UIView				*noView;

-(void) initList;
-(void)configureToolbar;
-(void)selectBooking:(id)sender;
-(NSString *)formatDuration:(int)duration;

-(NSString *)getSeatClass:(NSString *)seatClass;
-(UIBarButtonItem *)makeColoredButton:(UIColor *)btnColor W:(float)w H:(float)h Text:(NSString *)btnTitle SelectorString:(NSString *)selectorString;

-(IBAction) launchStops:(id)sender;
@end
