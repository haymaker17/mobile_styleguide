//
//  MyCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/10/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "MyCell.h"
#import "ExpenseListViewController.h"


@implementation MyCell
@synthesize label;
@synthesize labelTotal;
@synthesize labelStatus;
@synthesize btnDetail;
@synthesize rootVC;
@synthesize currentRow;
@synthesize btnDrill;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}

- (IBAction)buttonDetailPressed:(id)sender
{
	[rootVC expandCell:sender detailType:@"Justin" rowNumber:currentRow];
	
}

- (IBAction)buttonDrillPressed:(id)sender
{
	[rootVC switchViews:sender ParameterBag:nil];	
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[label dealloc];
	[labelTotal dealloc];
	[labelStatus dealloc];
	[btnDetail dealloc];
	[rootVC dealloc];
	[currentRow dealloc];
    [super dealloc];
}


@end
