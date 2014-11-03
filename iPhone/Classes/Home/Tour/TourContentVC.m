//
//  TourContentVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 5/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TourContentVC.h"

@implementation TourContentVC
@synthesize imageName;

-(id)initWithImageName:(NSString *)name
{
    if (self = [super initWithNibName:@"TourContentVC" bundle:nil])
    {
        self.imageName = name;
    }
    return self;
}

- (void)viewDidLoad
{
    [self.tourImage setImage:[UIImage imageNamed:imageName]];
    self.lblLower.text = @"";
    self.lblUpper.text = @"";
}

- (UIFont*)getFontForLabelWidth:(CGFloat)labelWidth labelHeight:(CGFloat)labelHeight minmumFontSize:(int)minfontSize fontName:(NSString*)fontName stringValue:(NSString*)value desiredFontSize:(int)desiredFontSize
{
    UIFont *finalFont = [UIFont fontWithName:fontName size:desiredFontSize];
    
    int i;
    for (i = desiredFontSize; i > minfontSize; i = i-2)
    {
        // Set the new font size.
        finalFont = [finalFont fontWithSize:i];
        // You can log the size you're trying: NSLog(@"Trying size: %u", i);
        
        /* This step is important: We make a constraint box
         using only the fixed WIDTH of the UILabel. The height will
         be checked later. */
        CGSize constraintSize = CGSizeMake(labelWidth, MAXFLOAT);
        
        // This step checks how tall the label would be with the desired font.
        CGSize labelSize;
        if ([ExSystem is7Plus]) {
            CGRect textRect = [value boundingRectWithSize:constraintSize options:NSStringDrawingUsesLineFragmentOrigin attributes:@{NSFontAttributeName:finalFont} context:nil];
            labelSize = textRect.size;
        }
        else
            labelSize = [value sizeWithFont:finalFont constrainedToSize:constraintSize lineBreakMode:NSLineBreakByWordWrapping];
        
        /* Here is where you use the height requirement!
         Set the value in the if statement to the height of your UILabel
         If the label fits into your required height, it will break the loop
         and use that font size. */
        if(labelSize.height <= labelHeight)
            break;
    }
    
    return finalFont;
}

@end
