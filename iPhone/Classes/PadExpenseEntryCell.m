//
//  PadExpenseEntryCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 10/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PadExpenseEntryCell.h"


@implementation PadExpenseEntryCell
@synthesize 	iv1, iv2, iv3, iv4, iv5, ivTop, ivBody;
@synthesize		lblExpenseType, lblAmountLabel, lblAmount, lblRequestLabel, lblRequest, lblLine1, lblLine2, lblSep;
@synthesize		lblLabelExtra1, lblValueExtra1, lblLabelExtra2, lblValueExtra2, lblLabelExtra3, lblValueExtra3;

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
