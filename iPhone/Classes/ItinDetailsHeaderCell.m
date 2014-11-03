//
//  ItinDetailsHeaderCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "ItinDetailsHeaderCell.h"
#import "ItinDetailsViewController.h";


@implementation ItinDetailsHeaderCell

@synthesize labelTripName;
@synthesize labelSegmentName;
@synthesize labelStart;
@synthesize labelEnd;
@synthesize labelLocator;
@synthesize btnDetail;
@synthesize rootVC;
@synthesize currentRow;
@synthesize btnDrill;
@synthesize imgHead;

@synthesize labelStartLabel;
@synthesize labelEndLabel;
@synthesize labelLocatorLabel;

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


- (void)dealloc 
{
	[labelTripName release];
	[labelSegmentName release];
	[labelStart release];
	[labelEnd release];
	[labelLocator release];
	
	[labelStartLabel release];
	[labelEndLabel release];
	[labelLocatorLabel release];
	
	[btnDetail release];
	[rootVC release];
	//[currentRow release];
	[imgHead release];
    [super dealloc];
}


@end
