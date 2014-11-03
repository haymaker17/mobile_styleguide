//
//  UtilityMethods.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 4/27/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "UtilityMethods.h"


@implementation UtilityMethods

// Call this API to adjust amount field to fit the number, when the width is known upon orientation change
+ (void) drawNameAmountLabelsOrientationAdjusted:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LeftOffset:(int) lo RightOffset:(int) ro Width:(int) width
{
	//NSLog(@"Orient Adjusted view width %d", width);
	CGSize s = [headLabelAmt.text sizeWithFont:headLabelAmt.font];
	int nameW =  width - lo-ro-5 - s.width; 
	
	headLabel.frame= CGRectMake(lo, headLabel.frame.origin.y, nameW, headLabel.frame.size.height);
	headLabelAmt.textAlignment = NSTextAlignmentRight;
	headLabelAmt.frame = CGRectMake(nameW + lo+5, headLabelAmt.frame.origin.y, s.width, headLabelAmt.frame.size.height);
	headLabelAmt.autoresizingMask = UIViewAutoresizingNone; 
	headLabel.autoresizingMask = UIViewAutoresizingNone; 
}

// Call this API to adjust amount field to fit the number, when the width is known upon orientation change and outside of table
// It needs to resize mask upon orientation change
+ (void) drawNameAmountLabelsOrientationAdjustedWithResize:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LeftOffset:(int) lo RightOffset:(int) ro Width:(int) width
{
	//NSLog(@"Orient Adjusted view width %d", width);
	CGSize s = [headLabelAmt.text sizeWithFont:headLabelAmt.font];
	int nameW =  width - lo-ro-5 - s.width; 
	
	headLabel.frame= CGRectMake(lo, headLabel.frame.origin.y, nameW, headLabel.frame.size.height);
	headLabelAmt.textAlignment = NSTextAlignmentRight;
	headLabelAmt.frame = CGRectMake(nameW + lo+5, headLabelAmt.frame.origin.y, s.width, headLabelAmt.frame.size.height);
	headLabelAmt.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin; 
	headLabel.autoresizingMask = UIViewAutoresizingFlexibleWidth; 
}

// Call this API to adjust amount field to fit the number when both labels fill the width.
+ (void) drawNameAmountLabels:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt
{
	CGSize s = [headLabelAmt.text sizeWithFont:headLabelAmt.font];
	CGRect rAmt = headLabelAmt.frame;
	CGRect rName = headLabel.frame;
	float offset = s.width - rAmt.size.width;
	headLabelAmt.frame = CGRectMake(rAmt.origin.x-offset, rAmt.origin.y, rAmt.size.width+offset, rAmt.size.height);
	headLabel.frame = CGRectMake(rName.origin.x, rName.origin.y, rName.size.width - offset, rName.size.height);
}	
// Call this API to adjust amount field to fit the number, when the width is not easily obtained, e.g. in a table filling a view.
+ (void) drawNameAmountLabelsPortrait:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LeftOffset:(int) lo RightOffset:(int) ro
{
	CGSize s = [headLabelAmt.text sizeWithFont:headLabelAmt.font];
	int nameW =  320 - lo-ro-5 - s.width; 
	
	headLabel.frame= CGRectMake(lo, 0, nameW, 21);
	headLabelAmt.textAlignment = NSTextAlignmentRight;
	headLabelAmt.frame = CGRectMake(nameW + lo+5, 0, s.width, 21);
	headLabelAmt.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin; 
	headLabel.autoresizingMask = UIViewAutoresizingFlexibleWidth; 
}

@end
