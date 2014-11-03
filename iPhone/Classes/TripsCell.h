//
//  TripsCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TripData.h"
@class TripsViewController;

@interface TripsCell : UITableViewCell 
{
	UILabel			*label;
	UILabel			*labelDateRange;
	UILabel			*labelLine3;
	UILabel			*labelLabelLine3;
	UILabel			*labelLine4;
	UILabel			*labelLabelLine4;
	UILabel			*labelLine5;
	UILabel			*labelLabelLine5;
	UILabel			*labelLine1Right;
	UIButton		*btnDetail;
	UIButton		*btnDrill;
	TripsViewController *rootVC;
	NSUInteger		currentRow;
	
	UILabel			*labelLine6;
	UILabel			*labelLabelLine6;
	UILabel			*labelLine7;
	UILabel			*labelLabelLine7;
	UILabel			*labelLine8;
	UILabel			*labelLabelLine8;
	UIImageView		*uiv1, *uiv2, *uiv3, *uiv4, *uiv5, *uiv6;
	
	UIActivityIndicatorView *activity;
	
	EntityTrip		*trip;	
    
    UILabel         *lblExpensed;
}

@property (nonatomic, strong) IBOutlet UILabel *lblExpensed;
@property (nonatomic, strong) IBOutlet UILabel *label;
@property (nonatomic, strong) IBOutlet UILabel *labelDateRange;
@property (nonatomic, strong) IBOutlet UILabel *labelLine3;
@property (nonatomic, strong) IBOutlet UILabel *labelLine4;
@property (nonatomic, strong) IBOutlet UILabel *labelLine5;
@property (nonatomic, strong) IBOutlet UILabel *labelLabelLine3;
@property (nonatomic, strong) IBOutlet UILabel *labelLabelLine4;
@property (nonatomic, strong) IBOutlet UILabel *labelLabelLine5;

@property (nonatomic, strong) IBOutlet UILabel *labelLine6;
@property (nonatomic, strong) IBOutlet UILabel *labelLine7;
@property (nonatomic, strong) IBOutlet UILabel *labelLine8;
@property (nonatomic, strong) IBOutlet UILabel *labelLabelLine6;
@property (nonatomic, strong) IBOutlet UILabel *labelLabelLine7;
@property (nonatomic, strong) IBOutlet UILabel *labelLabelLine8;

@property (nonatomic, strong) IBOutlet UILabel *labelLine1Right;
@property (nonatomic, strong) IBOutlet UIButton *btnDetail;
@property (nonatomic, strong) IBOutlet UIButton *btnDrill;
@property (nonatomic, strong) TripsViewController *tripsVC;
@property (nonatomic) NSUInteger currentRow;

@property (nonatomic, strong) IBOutlet UIImageView *uiv1;
@property (nonatomic, strong) IBOutlet UIImageView *uiv2;
@property (nonatomic, strong) IBOutlet UIImageView *uiv3;
@property (nonatomic, strong) IBOutlet UIImageView *uiv4;
@property (nonatomic, strong) IBOutlet UIImageView *uiv5;
@property (nonatomic, strong) IBOutlet UIImageView *uiv6;

@property (nonatomic, strong) EntityTrip *trip;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView *activity;

- (IBAction)buttonDetailPressed:(id)sender;
- (IBAction)buttonDrillPressed:(id)sender;


@end
