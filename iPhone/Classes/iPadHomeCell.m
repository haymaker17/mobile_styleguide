//
//  iPadHomeCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 9/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "iPadHomeCell.h"


@implementation iPadHomeCell
@synthesize lblText, iv, lblLoading, loadingView;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if ((self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}




@end
