//
//  DetailDoubleColCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "DetailDoubleColCell.h"
#import "ExpenseTypesViewController.h"

@implementation DetailDoubleColCell

@synthesize lblCol1, lblCol2;
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
@synthesize pickerPopOver;
#endif

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




- (IBAction)setColorButtonTapped:(id)sender 
{
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	ExpenseTypesViewController *c = [[ExpenseTypesViewController alloc] init];
        self.pickerPopOver = [[UIPopoverController alloc] 
									initWithContentViewController:c];    
    [self.pickerPopOver presentPopoverFromRect:CGRectMake(0, 0, 200, 30) inView:self.contentView permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
#endif
}


@end
