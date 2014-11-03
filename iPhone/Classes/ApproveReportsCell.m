//
//  ApproveReportsCell.m
//  ConcurMobile
//
//  Created by yiwen on 1/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ApproveReportsCell.h"
#import "ApproveReportsViewController.h"


@implementation ApproveReportsCell
@synthesize labelName;
@synthesize labelTotal;
@synthesize labelDate;
@synthesize labelEmployee;
@synthesize rootVC;
@synthesize currentRow;
@synthesize icon1;
@synthesize icon2;
@synthesize icon3;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;

	// Icon1: 212.0 35.0 24.0 24.0
	// Icon2: 240.0 35.0 24.0 24.0
	// Icon3: 268.0 35.0 24.0 24.0
}

- (NSString *) reuseIdentifier {
	return @"ApproveReportsCell";
}

- (IBAction)buttonDrillPressed:(id)sender
{
	[rootVC viewReportDetails:currentRow];
}

/*- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
	
    [super setSelected:selected animated:animated];
	
    // Configure the view for the selected state
}*/


- (void)dealloc {
	[labelEmployee release];
	[labelName release];
	[labelTotal release];
	[labelDate release];
    [icon1 release];
    [icon2 release];
    [icon3 release];
	
	[rootVC release];
    [super dealloc];
}


@end
