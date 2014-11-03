//
//  ItinDetailsCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "ItinDetailsCell.h"
#import "ItinDetailsViewController.h"


@implementation ItinDetailsCell

@synthesize labelSection1;
@synthesize labelSection2;
@synthesize labelSection3;
@synthesize labelSection4;
@synthesize label1Section1;
@synthesize label2Section1;
@synthesize label1Section2;
@synthesize label2Section2;
@synthesize label1Section3;
@synthesize label2Section3;
@synthesize label3Section3;
@synthesize label1Section4;
@synthesize labelVendor;
@synthesize imgVendor;
@synthesize imgDetail;
@synthesize btnAction;
@synthesize rootVC;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        // Initialization code
    }
    return self;
}

- (IBAction)buttonDrillPressed:(id)sender
{
	//[rootVC switchViews:sender ParameterBag:nil];	
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[labelSection1 dealloc];
	[labelSection2 dealloc];
	[labelSection3 dealloc];
	[labelSection4 dealloc];
	[label1Section1 dealloc];
	[label2Section1 dealloc];
	[label1Section2 dealloc];
	[label2Section2 dealloc];
	[label1Section3 dealloc];
	[label2Section3 dealloc];
	[label3Section3 dealloc];
	[label1Section4 dealloc];
	[labelVendor dealloc];
	[imgVendor dealloc];
	[imgDetail dealloc];
	[btnAction dealloc];
	[rootVC dealloc];
    [super dealloc];
}


@end
