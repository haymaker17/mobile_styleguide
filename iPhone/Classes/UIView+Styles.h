//
//  UIView+Styles.h
//  ConcurAuth
//
//  Created by Wanny Morellato on 11/5/13.
//  Copyright (c) 2013 Wanny Morellato. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIView (Styles)

/*
 * convinience method to set calayer corner radius
 */
-(void)setCornerRadius:(CGFloat)cornerRadius;
-(CGFloat)cornerRadius;

/*
 * convinience method to set calayer border color
 */
- (void)setBorderColor:(UIColor*)borderColor;
- (UIColor*)borderColor;

/*
 * convinience method to set calayer border width
 */
- (void)setBorderWidth:(CGFloat)borderWidth;
- (CGFloat)borderWidth;


/*
 * convinience method to set calayer shadow color
 */
- (void)setShadowColor:(UIColor*)shadowColor;
- (UIColor*)shadowColor;

/*
 * convinience method to set calayer shadow radius
 */
- (void)setShadowRadius:(CGFloat)shadowRadius;
- (CGFloat)shadowRadius;

/*
 * convinience method to set different border selected by a pattern 'LTRB'
 * example: 'tl' for set a border on top and left
 */
- (void)setBorders:(NSString*)pattern WithColor:(UIColor*)uicolor andBorderWidth:(CGFloat)borderWidth;

/*
 * convinience method to design all style for a workflow button
 * like submit button on travel request
 */
- (void)applyStyleButtonWorkflow;

/*
 * convinience method to design all style for a label over a workflow button
 * like total amount over submit button on travel request
 */
- (void)applyStyleForALabelOverButtonWorkflow;

/*
 * convinience method to design all style for a white bloc with border top and bottom
 * option: could specify a template to set border only on top for example : @"t"
 */
- (void)applyStyleWhiteBlocWithBorderTemplateOrNil:(NSString*)template;

/*
 * convinience method to adjust width size to its content bordered by maxwidth
 */
- (void)applyFitContentByConstrainte:(NSLayoutConstraint*)constrainte withMaxWidth:(CGFloat)maxWidth andMarge:(CGFloat)marge;

/*
 * convinience method to display approval Status bordered
 */
- (void)applyStyleForDisplayApprovalStatusBorderedByConstrainte:(NSLayoutConstraint*)constrainte withMaxWidth:(CGFloat)maxWidth;

-(CGFloat)getTextViewFitToContentWithHeightMax:(CGFloat)heightMax;

@end
