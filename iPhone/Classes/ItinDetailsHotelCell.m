//
//  ItinDetailsHotelCell.m
//  ConcurMobile
//
//  Created by yiwen on 12/15/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "ItinDetailsHotelCell.h"


@implementation ItinDetailsHotelCell

@synthesize txtBooking;
@synthesize txtCheckIn;
@synthesize txtCheckOut;
@synthesize txtStatus;
@synthesize txtPhone;
@synthesize txtCancellation;
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
	[rootVC switchViews:sender];	
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


- (void)dealloc {
	[txtBooking dealloc];
	[txtCheckIn dealloc];
	[txtCheckOut dealloc];
	[txtStatus dealloc];
	[txtPhone dealloc];
	[txtCancellation dealloc];
	[btnAction dealloc];
	[rootVC dealloc];
    [super dealloc];
}


@end
