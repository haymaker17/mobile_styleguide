//
//  TripDetailHeaderCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "TripDetailHeaderCell.h"
#import "TripDetailsViewController.h"

@implementation TripDetailHeaderCell
@synthesize labelTripName;
@synthesize labelStart;
@synthesize labelEnd;
@synthesize labelLocator;
@synthesize btnDetail;
@synthesize rootVC;
@synthesize currentRow;
@synthesize btnDrill;

- (IBAction)buttonDetailPressed:(id)sender
{
	//[rootVC expandCell:sender detailType:@"Justin" rowNumber:currentRow];
	
}

- (IBAction)buttonDrillPressed:(id)sender
{
	//[rootVC switchViews:sender ParameterBag:nil];	
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[labelTripName dealloc];
	[labelStart dealloc];
	[labelEnd dealloc];
	[labelLocator dealloc];
	[btnDetail dealloc];
	[rootVC dealloc];
	//[currentRow dealloc];
    [super dealloc];
}


@end
