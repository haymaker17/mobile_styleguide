//
//  UtilityMethods.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 4/27/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface UtilityMethods : NSObject {
    
}

#pragma mark -
#pragma mark View utility methods
+ (void) drawNameAmountLabels:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt;
+ (void) drawNameAmountLabelsOrientationAdjusted:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LeftOffset:(int) lo RightOffset:(int) ro Width:(int) width;
+ (void) drawNameAmountLabelsOrientationAdjustedWithResize:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LeftOffset:(int) lo RightOffset:(int) ro Width:(int) width;
+ (void) drawNameAmountLabelsPortrait:(UILabel *)headLabel AmountLabel:(UILabel *)headLabelAmt LeftOffset:(int) lo RightOffset:(int) ro;
@end
