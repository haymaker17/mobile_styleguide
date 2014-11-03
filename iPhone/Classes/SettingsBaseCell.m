//
//  SettingsBaseCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "SettingsBaseCell.h"


@implementation SettingsBaseCell
@synthesize lblHeading, lblSubheading, switchView, value, rowKey, dictRowData, lblRefundable;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}


-(IBAction) switchChanged:(id)sender
{
    if(switchView.on == YES)
        self.value = @"YES";
    else
        self.value = @"NO";
    
    if (switchView.tag == 905) 
    {
        [ExSystem sharedInstance].offersValidityChecking = value;
    }
    else
    {
        dictRowData[rowKey] = value;
    }
}

@end
