//
//  EntryCellBig.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/12/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "EntryCellBig.h"
#import "EntryListViewController.h"


@implementation EntryCellBig

@synthesize label;
@synthesize labelAmount;
@synthesize labelStatus;
@synthesize labelStatusTwo;
@synthesize btnDetail;
@synthesize rootVC;
@synthesize currentRow;
@synthesize image1;
@synthesize image2;
@synthesize image3;
@synthesize image4;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}

- (IBAction)buttonDetailPressed:(id)sender
{
	[rootVC expandCell:sender detailType:@"Small" rowNumber:currentRow];
	
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[label dealloc];
	[labelAmount dealloc];
	[labelStatus dealloc];
	[labelStatusTwo dealloc];
	[btnDetail dealloc];
	[rootVC dealloc];
	[currentRow dealloc];
	[image1 dealloc];
	[image2 dealloc];
	[image3 dealloc];
	[image4 dealloc];
    [super dealloc];
}


@end
