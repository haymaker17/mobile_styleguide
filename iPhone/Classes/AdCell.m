//
//  AdCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 1/13/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AdCell.h"


@implementation AdCell

@synthesize rootVC;
@synthesize currentRow;

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
	[rootVC dealloc];
	[currentRow dealloc];
    [super dealloc];
}


@end
