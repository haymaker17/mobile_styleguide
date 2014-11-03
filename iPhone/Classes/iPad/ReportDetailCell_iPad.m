//
//  ReportDetailCell_iPad.m
//  ConcurMobile
//
//  Created by charlottef on 3/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ReportDetailCell_iPad.h"
#import "ImageUtil.h"

@implementation ReportDetailCell_iPad

-(NSString*)reuseIdentifier
{
	return @"ReportDetailCell_iPad";
}

-(void) resetCellContent:(NSString*)title withAmount:(NSString*)amt withReqAmt:(NSString*)reqAmt withLine1:(NSString*)line1 withLine2:(NSString*)line2 withImage1:(NSString*)imgName1 withImage2:(NSString*)imgName2 withImage3:(NSString*)imgName3
{
    self.requestedLabel.text = reqAmt;
    self.iv0.image = [ImageUtil getImageByName:imgName1];
    self.iv1.image = [ImageUtil getImageByName:imgName2];
    self.iv2.image = [ImageUtil getImageByName:imgName3];
}

#pragma -mark utility function
- (NSObject*)attributedStringWithAmountForLabel:(UILabel*)label labelText:(NSString*)labelText valueText:(NSString*)valueText fontName:(NSString*)fontName fontSize:(float)fontSize
{
    if (![ExSystem is6Plus])
    {
        return [NSString stringWithFormat:@"%@%@", labelText, valueText];
    }
    else
    {
        if (label.attributedText == nil)
            return nil;
        
        // Get the attributes from the existing text inside the UILabel
        NSDictionary *labelAttributes = [label.attributedText attributesAtIndex:0 effectiveRange:nil];
        
        // Create a new label with the same attributes
        NSMutableAttributedString *labelString = [[NSMutableAttributedString alloc] initWithString:labelText attributes:labelAttributes];
        
        if (valueText != nil && valueText.length > 0)
        {
            // Create a new dictionary of attributes that is identical to the label attributes except for the font
            NSMutableDictionary *valueAttributes = [NSMutableDictionary dictionaryWithDictionary:labelAttributes];
            valueAttributes[NSFontAttributeName] = [UIFont fontWithName:fontName size:fontSize];
            
            // Create an attributed string for the value using the new attributes
            NSAttributedString *value = [[NSAttributedString alloc] initWithString:valueText attributes:valueAttributes];
            
            // Put the label and value together
            [labelString appendAttributedString:value];
        }
        
        return labelString;
    }
}

@end
