//
//  OfferCell.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/26/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "OfferCell.h"

@implementation OfferCell
@synthesize lblTitle;
@synthesize ivIcon;
@synthesize activity;

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

-(void)configureLabelFontForLabel:(UILabel*)lbl WithText:(NSString*)txt
{
    [lbl setFont:[UIFont fontWithName:@"Helvetica Neue Medium" size:12]];
    UIFont *font = lbl.font;
    
    int i;
    /* Time to calculate the needed font size.
     This for loop starts at the largest font size, and decreases by two point sizes (i=i-2)
     Until it either hits a size that will fit or hits the minimum size we want to allow (i > 10) */
    for(i = 12; i > 8; i=i-2)
    {
        // Set the new font size.
        font = [font fontWithSize:i];
        // You can log the size you're trying: NSLog(@"Trying size: %u", i);
        
        /* This step is important: We make a constraint box 
         using only the fixed WIDTH of the UILabel. The height will
         be checked later. */ 
        CGSize constraintSize = CGSizeMake(267.0f, MAXFLOAT);
        
        // This step checks how tall the label would be with the desired font.
        CGSize labelSize = [txt sizeWithFont:font constrainedToSize:constraintSize lineBreakMode:NSLineBreakByWordWrapping];
        
        /* Here is where you use the height requirement!
         Set the value in the if statement to the height of your UILabel
         If the label fits into your required height, it will break the loop
         and use that font size. */
        if(labelSize.height <= 30.0f)
            break;
    } 
    
    [lbl setFont:font];
    [lbl setText:txt];
}

@end
