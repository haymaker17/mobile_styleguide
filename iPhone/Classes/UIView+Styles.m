//
//  UIView+Styles.m
//  ConcurAuth
//
//  Created by Wanny Morellato on 11/5/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

#import "UIView+Styles.h"
#import <CoreGraphics/CoreGraphics.h>

@implementation UIView (Styles)

/*
 * convinience method to set calayer corner radius
 */
- (void)setCornerRadius:(CGFloat)cornerRadius{
    self.layer.masksToBounds = YES;
    self.layer.cornerRadius = cornerRadius;
}
- (CGFloat)cornerRadius{
    return self.layer.cornerRadius;
}

/*
 * convinience method to set calayer border color
 */
- (void)setBorderColor:(UIColor*)borderColor{
    self.layer.borderColor = borderColor.CGColor;
}

- (UIColor*)borderColor{
    return [UIColor colorWithCGColor:self.layer.borderColor];
}


/*
 * convinience method to set calayer border width
 */
- (void)setBorderWidth:(CGFloat)borderWidth{
    self.layer.borderWidth = borderWidth;
}

- (CGFloat)borderWidth{
    return self.layer.borderWidth;
}

/*
 * convinience method to set calayer shadow color
 */
- (void)setShadowColor:(UIColor*)shadowColor{
    self.layer.shadowColor = shadowColor.CGColor;
}
- (UIColor*)shadowColor{
     return [UIColor colorWithCGColor:self.layer.shadowColor];
}

/*
 * convinience method to set calayer shadow radius
 */
- (void)setShadowRadius:(CGFloat)shadowRadius{
    self.layer.masksToBounds = NO;
    self.layer.shadowRadius = shadowRadius;
}
- (CGFloat)shadowRadius{
    return self.layer.shadowRadius;
}

/*
 * convinience method to set different border selected by a pattern 'LTRB'
 * pattern is 'LTRB' (for left, top, right, bottom), set only letter that you need
 * example: 'tl' for set a border on top and left
 * (we used clipping method on layer)
 * tip: lowercase or uppercase are accepted
 * tip: you can use multiple call to obtain different border size and color
 */
- (void)setBorders:(NSString*)pattern WithColor:(UIColor*)uicolor andBorderWidth:(CGFloat)borderWidth{

	CGFloat currentWidth = CGRectGetWidth(self.frame);
	CGFloat currentHeight = CGRectGetHeight(self.frame);
	CGFloat left = 0, top = 0, width = currentWidth, height = currentHeight;
	
	[pattern lowercaseString];
	
	NSRange range_t = [pattern rangeOfString:@"t" options: NSCaseInsensitiveSearch];
	NSRange range_b = [pattern rangeOfString:@"b" options: NSCaseInsensitiveSearch];
	
	if (range_t.location != NSNotFound && range_b.location != NSNotFound){
		
		//nothing to do
	}
	else if (range_t.location != NSNotFound){
		
		height = currentHeight + borderWidth;
	}
	else if (range_b.location != NSNotFound){
		
		top = -borderWidth;
		height = currentHeight + borderWidth;
	}
	else { // no top and bottom border
		
		top =  -borderWidth;
		height = currentHeight + (borderWidth * 2);
	}
	
	
	NSRange range_l = [pattern rangeOfString:@"l" options: NSCaseInsensitiveSearch];
	NSRange range_r = [pattern rangeOfString:@"r" options: NSCaseInsensitiveSearch];
	
	if (range_l.location != NSNotFound && range_r.location != NSNotFound){
		
		//nothing to do !
	}
	else if (range_l.location != NSNotFound){
		
		width = currentWidth + borderWidth;
	}
	else if (range_r.location != NSNotFound){
		
		left = -borderWidth;
		width = currentWidth + borderWidth;
	}
	else { // no left and right border
		
		left = -borderWidth;
		width = currentWidth + (borderWidth * 2);
	}
	
	self.clipsToBounds = YES;
	
	CALayer *addBorders = [CALayer layer];
	addBorders.borderColor = uicolor.CGColor;
	addBorders.borderWidth = borderWidth;
	addBorders.frame = CGRectMake(left, top, width, height);
	
	[self.layer addSublayer:addBorders];
}

-(void)applyStyleButtonWorkflow{
	
	[self setBackgroundColor:[UIColor backgroundForMainButtonWorkflow]];
	[self setTintColor:[UIColor whiteConcur]];
	[self setBorderColor:[UIColor borderForMainButtonWorkflow]];
	[self setBorderWidth:2];
	[self setCornerRadius:3.0];
}

- (void)applyStyleForALabelOverButtonWorkflow{
	
	[self setBackgroundColor:[UIColor backgroundToHighlightTextOverMainButtonWorkflow]];
	[self setBorderWidth:1];
	[self setCornerRadius:3.0];
	[self setBorderColor:[UIColor borderToHighlightTextOverMainButtonWorkflow]];
	[(UILabel*)self setTextColor:[UIColor whiteConcur]];
}

- (void)applyStyleWhiteBlocWithBorderTemplateOrNil:(NSString*)template{
	
	[self setBackgroundColor:[UIColor whiteConcur]];
	[self setTintColor:[UIColor textLightTitle]];
	
	if (template == nil) {
		
		template = @"tb"; //default value
	}
	[self setBorders:template WithColor:[UIColor borderViewToHighlightWhiteSubview] andBorderWidth:1];
}

- (void)applyFitContentByConstrainte:(NSLayoutConstraint*)constrainte withMaxWidth:(CGFloat)maxWidth andMarge:(CGFloat)marge{
	
	CGSize expectedSize = [self sizeThatFits:CGSizeMake(maxWidth, CGFLOAT_MAX)];
	CGFloat selfWidth = expectedSize.width + marge;
	
	if (maxWidth < selfWidth){
		
		selfWidth = maxWidth;
	}
	
	[constrainte setConstant:selfWidth];
	[self needsUpdateConstraints];
}

- (void)applyStyleForDisplayApprovalStatusBorderedByConstrainte:(NSLayoutConstraint*)constrainte withMaxWidth:(CGFloat)maxWidth{
	
	[self.layer setBorderWidth:0.5];
	[self.layer setCornerRadius:3.0];
	[self.layer setBorderColor:[UIColor borderWorkflowStatutInList].CGColor];
	
	[self applyFitContentByConstrainte:constrainte withMaxWidth:maxWidth andMarge:8.0];
}


-(CGFloat)getTextViewFitToContentWithHeightMax:(CGFloat)heightMax{
	
	NSLineBreakMode *lineBreakMode;
	UITextView *me = (UITextView*)self;
	
	if ([self isKindOfClass:[UILabel class]]){
		
		UILabel *label = (UILabel*)self;
		lineBreakMode = label.lineBreakMode;
	}
		 else {
			 
			 lineBreakMode = me.textContainer.lineBreakMode;
		 }
		 
		 
	CGFloat currentWidth = CGRectGetWidth(me.frame);
	UIFont *currentFont = me.font;
	CGSize CGText = [me.text sizeWithFont:currentFont
						constrainedToSize:CGSizeMake(currentWidth, heightMax)
							lineBreakMode:lineBreakMode];
	
	return CGText.height;
}

@end
