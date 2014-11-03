//
//  TrainPassengersVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "BookingCellData.h"


@interface TrainPassengersVC : MobileViewController {

	UITableView	*tableList;
	NSMutableArray	*aSections, *aList;
	BookingCellData		*bcdPass;
}
@property (strong, nonatomic) IBOutlet UITableView	*tableList;
@property (strong, nonatomic) NSMutableArray	*aSections;
@property (strong, nonatomic) NSMutableArray	*aList;
@property (strong, nonatomic) BookingCellData		*bcdPass;

-(IBAction)cancelView:(id)sender;
-(IBAction)doneView:(id)sender;
-(void) initPassengerList;
@end
