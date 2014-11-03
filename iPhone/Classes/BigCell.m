//
//  BigCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/10/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "BigCell.h"
#import "ExpenseListViewController.h"

@implementation BigCell
@synthesize label;
@synthesize labelTotal;
@synthesize labelStatus;
@synthesize labelReportDate;
@synthesize labelPayStatus;
@synthesize labelClaimed;
@synthesize labelApproved;
@synthesize btnDetail;
@synthesize rootVC;
@synthesize currentRow;
@synthesize purpose;


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}

- (IBAction)buttonDetailPressed:(id)sender
{
	[rootVC expandCell:sender detailType:@"Up" rowNumber:currentRow];
	
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[label dealloc];
	[labelTotal dealloc];
	[labelStatus dealloc];
	[labelReportDate dealloc];
	[labelPayStatus dealloc];
	[labelClaimed dealloc];
	[labelApproved dealloc];
	[purpose dealloc];
	[btnDetail dealloc];
	[rootVC dealloc];
	[currentRow dealloc];
    [super dealloc];
}


@end
