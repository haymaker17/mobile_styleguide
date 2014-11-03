//
//  MixedFontLabeler.m
//  ConcurMobile
//
//  Created by charlottef on 3/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MixedFontLabeler.h"

@implementation MixedFontLabel
+(MixedFontLabel*) labelWithText:(NSString*)labelText bold:(BOOL)labelBold
{
    MixedFontLabel *label = [[MixedFontLabel alloc] init];
    label.text = labelText;
    label.bold = labelBold;
    return label;
}
@end

@interface MixedFontLabeler ()
@property (strong, nonatomic) UIFont* regularFont;
@property (strong, nonatomic) UIFont* boldFont;
@end

@implementation MixedFontLabeler

+(MixedFontLabeler*) mixedFontLabelerWithRegularFont:(UIFont*)labelerRegularFont boldFont:(UIFont*)labelerBoldFont;
{
    MixedFontLabeler *labeler = [[MixedFontLabeler alloc] init];
    labeler.regularFont = labelerRegularFont;
    labeler.boldFont = labelerBoldFont;
    return labeler;
}

-(void) addLabels:(NSArray*)labels toView:(UIView*)view yPos:(float)yPos
{
    //for (UIView *subview in view.subviews)
            //subview.hidden = YES;

    UIColor *backgroundColor = [UIColor clearColor];
    
    const float maxLabelWidth = view.frame.size.width;
    float x = 0.0;
    
    for (NSObject *obj in labels)
    {
        if ([obj isKindOfClass:[MixedFontLabel class]])
        {
            MixedFontLabel *label = (MixedFontLabel*)obj;
            UILabel *uiLabel = [self makeUILabel:label xPos:x yPos:yPos maxWidth:maxLabelWidth backgroundColor:backgroundColor];
            uiLabel.tag = 666;			//tag for label added
            [view addSubview:uiLabel];
            x += uiLabel.frame.size.width;
        }
        else if ([obj isKindOfClass:[NSNumber class]])
        {
            float numPixels = [(NSNumber*)obj floatValue];
            x += numPixels;
        }
    }
}

-(UILabel*) makeUILabel:(MixedFontLabel*)label xPos:(float)xPos yPos:(float)yPos maxWidth:(float)maxWidth backgroundColor:(UIColor*)backgroundColor
{
    UIFont *font = (label.bold ? self.boldFont : self.regularFont);
    
    CGSize textSize = [label.text sizeWithFont:font constrainedToSize:CGSizeMake(maxWidth, CGFLOAT_MAX) lineBreakMode:NSLineBreakByWordWrapping];
    
    CGRect uiLabelFrame = CGRectMake (xPos, yPos, textSize.width, textSize.height);
    
    UILabel *uiLabel = [[UILabel alloc] initWithFrame:uiLabelFrame];
    [uiLabel setFont:font];
    [uiLabel setText:label.text];
    [uiLabel setBackgroundColor:backgroundColor];
    return uiLabel;
}

@end
