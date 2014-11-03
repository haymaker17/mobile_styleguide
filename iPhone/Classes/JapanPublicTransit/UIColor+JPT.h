//
//  UIColor+JPT.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIColor (JPT)

+ (UIColor *)colorWithHueDegrees:(CGFloat)hue saturation:(CGFloat)saturation brightness:(CGFloat)brightness;

+ (UIColor *)cxBlack;
+ (UIColor *)routeHeaderColor;
+ (UIColor *)routeSubheaderColor;

@end
