//
//  TripDetailHeaderCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class TripDetailsViewController;

@interface TripDetailHeaderCell : UITableViewCell {
	UILabel *labelTripName;
	UILabel *labelStart;
	UILabel *labelEnd;
	UILabel *labelLocator;
	UIButton *btnDetail;
	UIButton *btnDrill;
	TripDetailsViewController *rootVC;
	NSUInteger currentRow;
}

@property (nonatomic, retain) IBOutlet UILabel *labelTripName;
@property (nonatomic, retain) IBOutlet UILabel *labelStart;
@property (nonatomic, retain) IBOutlet UILabel *labelEnd;
@property (nonatomic, retain) IBOutlet UILabel *labelLocator;
@property (nonatomic, retain) IBOutlet UIButton *btnDetail;
@property (nonatomic, retain) IBOutlet UIButton *btnDrill;
@property (nonatomic, retain) TripDetailsViewController *rootVC;
@property (nonatomic) NSUInteger currentRow;

- (IBAction)buttonDetailPressed:(id)sender;
- (IBAction)buttonDrillPressed:(id)sender;

@end
