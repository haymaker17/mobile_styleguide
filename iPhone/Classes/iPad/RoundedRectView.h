//
//  RoundedRectView.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/12/10.
//  Copyright 2010 Concur. All rights reserved.
//


#define kDefaultStrokeColor         [UIColor blueConcur]
#define kDefaultRectColor           [UIColor blueConcur]
#define kDefaultSheenColor          [UIColor colorWithRed:1 green:1 blue:1 alpha:0.2]
#define kDefaultStrokeWidth         2.0
#define kDefaultCornerRadius        10.0

@interface RoundedRectView : UIView {}

@property (nonatomic, strong) UIColor *strokeColor;
@property (nonatomic, strong) UIColor *rectangleColor;
@property CGFloat strokeWidth;
@property CGFloat cornerRadius;
@property BOOL isSheenEnabled;
@property BOOL isRoundingDisabled;
@property BOOL isRotatingDisabled;
@end

@interface RotatingRoundedRectView : RoundedRectView {
}
- (void)rotateToInterfaceOrientation;
- (void)onUIInterfaceOrientationChanged;
- (id)initCenteredWithParentView:(UIView *)pv withHeight:(float)height withWidth:(float)width;
@end
