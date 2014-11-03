//
//  UIColor+JPT.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/20/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "UIColor+JPT.h"

@implementation UIColor (JPT)

+ (UIColor *)colorWithHueDegrees:(CGFloat)hue saturation:(CGFloat)saturation brightness:(CGFloat)brightness {
    return [UIColor colorWithHue:(hue/360) saturation:saturation brightness:brightness alpha:1.0];
}

+ (UIColor *)cxBlack {
    return [UIColor colorWithHueDegrees:217 saturation:.77 brightness:.19];
}

+ (UIColor *)routeHeaderColor {
    return [UIColor colorWithHueDegrees:0 saturation:0 brightness:.2];
}

+ (UIColor *)routeSubheaderColor {
    return [UIColor colorWithHueDegrees:0 saturation:0 brightness:.5];
}

@end
