//
//  TripsCellBig.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class TripsViewController;

@interface TripsCellBig : UITableViewCell  <UITableViewDelegate, UITableViewDataSource>{
	UILabel *label;
	UILabel *labelDateRange;
	UILabel *labelLocator;
	UIButton *btnDetail;
	UIButton *btnDrill;
	TripsViewController *rootVC;
	NSUInteger *currentRow;
	
	//UITableView *table;
    NSDictionary *tripBits;
    NSArray  *keys;
	
}

@property (nonatomic, retain) IBOutlet UILabel *label;
@property (nonatomic, retain) IBOutlet UILabel *labelDateRange;
@property (nonatomic, retain) IBOutlet UILabel *labelLocator;
@property (nonatomic, retain) IBOutlet UIButton *btnDetail;
@property (nonatomic, retain) IBOutlet UIButton *btnDrill;
@property (nonatomic, retain) TripsViewController *rootVC;
@property (nonatomic) NSUInteger *currentRow;

@property (nonatomic, retain) NSDictionary *tripBits;
@property (nonatomic, retain) NSArray *keys;

- (IBAction)buttonDetailPressed:(id)sender;
- (IBAction)buttonDrillPressed:(id)sender;

@end
