//
//  ActiveReportListCell.m
//  ConcurMobile
//
//  Created by yiwen on 4/20/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ActiveReportListCell.h"


@implementation ActiveReportListCell

@synthesize image1, image2, image3, image4;
@synthesize lblRptStatus;

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




@end
