//
//  ReceiptButtonCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ReceiptButtonCell.h"

@implementation ReceiptButtonCell

@synthesize 	btnCamera/*, btnPhotoAlbum, btnReceiptFolder, btnClear*/;
@synthesize     lblUpdateReceipt;

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



#pragma mark -
#pragma mark Button Methods
-(IBAction)buttonCameraPressed:(id)sender
{
//	[oopeForm buttonReceiptActionsPressed:nil];
}

/*
-(IBAction)buttonAlbumPressed:(id)sender
{
	[oopeForm buttonAlbumPressed:sender];
}


-(IBAction)btnClearReceipt:(id)sender
{
	[oopeForm btnClearReceipt:sender];
}


-(IBAction)btnReceiptManager:(id)sender
{
	[oopeForm btnReceiptManager:sender];
}
*/

@end
