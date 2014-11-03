//
//  ItinDetailsHeaderCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class ItinDetailsViewController;

@interface ItinDetailsHeaderCell : UITableViewCell {
	UILabel *labelTripName;
	UILabel *labelSegmentName;
	UILabel *labelStart;
	UILabel *labelEnd;
	UILabel *labelLocator;
	UILabel *labelStartLabel;
	UILabel *labelEndLabel;
	UILabel *labelLocatorLabel;
	UIButton *btnDetail;
	UIButton *btnDrill;
	ItinDetailsViewController *rootVC;
	NSUInteger currentRow;
	UIImageView		*imgHead;
}

@property (nonatomic, retain) IBOutlet UILabel *labelTripName;
@property (nonatomic, retain) IBOutlet UILabel *labelSegmentName;
@property (nonatomic, retain) IBOutlet UILabel *labelStart;
@property (nonatomic, retain) IBOutlet UILabel *labelEnd;
@property (nonatomic, retain) IBOutlet UILabel *labelLocator;
@property (nonatomic, retain) IBOutlet UILabel *labelStartLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelEndLabel;
@property (nonatomic, retain) IBOutlet UILabel *labelLocatorLabel;
@property (nonatomic, retain) IBOutlet UIButton *btnDetail;
@property (nonatomic, retain) IBOutlet UIButton *btnDrill;
@property (nonatomic, retain) IBOutlet UIImageView		*imgHead;
@property (nonatomic, retain) ItinDetailsViewController *rootVC;
@property (nonatomic) NSUInteger currentRow;

- (IBAction)buttonDetailPressed:(id)sender;
- (IBAction)buttonDrillPressed:(id)sender;

@end
