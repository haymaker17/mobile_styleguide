//
//  ReceiptStoreListCell.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ReceiptStoreListCell.h"

@implementation ReceiptStoreListCell
@synthesize thumbImageView, imageDateLbl,activityView,imageBackgroundView,tagLbl,receiptId;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) 
	{
        // Initialization code.
        [self setBackgroundColor:[ExSystem getBaseBackgroundColor]];
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
	[super setSelected:selected animated:animated];
	
    // Configure the view for the selected state
}




@end
