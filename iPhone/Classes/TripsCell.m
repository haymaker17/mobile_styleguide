//
//  TripsCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/13/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "TripsCell.h"
#import "TripsViewController.h"


@implementation TripsCell
@synthesize label;
@synthesize labelDateRange;
@synthesize labelLine3;
@synthesize labelLine4;
@synthesize labelLine5;
@synthesize labelLabelLine3;
@synthesize labelLabelLine4;
@synthesize labelLabelLine5;
@synthesize labelLine6;
@synthesize labelLine7;
@synthesize labelLine8;
@synthesize labelLabelLine6;
@synthesize labelLabelLine7;
@synthesize labelLabelLine8;
@synthesize btnDetail;
@synthesize tripsVC;
@synthesize currentRow;
@synthesize btnDrill;
@synthesize labelLine1Right;

@synthesize uiv1;
@synthesize uiv2;
@synthesize uiv3;
@synthesize uiv4;
@synthesize uiv5;
@synthesize uiv6;

@synthesize trip;
@synthesize activity, lblExpensed;

- (IBAction)buttonDetailPressed:(id)sender
{
	//[rootVC expandCell:sender detailType:@"Justin" rowNumber:currentRow];
}

- (IBAction)buttonDrillPressed:(id)sender
{
	//[activity startAnimating];
	//NSLog(@"trip.tripKey=%@", trip.tripKey);
	//[rootVC doTripDrill:trip.tripKey Trip:trip];
	
    [tripsVC displayTripOniPhone:trip withLoadedTrip:YES];
	//[rootVC switchViews:sender ParameterBag:nil];
	//[rootVC switchToView:TRIP_DETAILS viewFrom:TRIPS ParameterBag:nil];
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) 
	{
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated 
{
    [super setSelected:selected animated:animated];
    // Configure the view for the selected state
}




@end
