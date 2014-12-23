//
//  FadeTruncatingLabel.m
//  ConcurMobile
//
//  Created by Shifan Wu on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FadeTruncatingLabel.h"

@interface FadeTruncatingLabel ()
- (void)setup;
@end

@implementation FadeTruncatingLabel

- (void)setup {
    self.backgroundColor = [UIColor clearColor];
    self.truncateMode = FadeTruncatingTail;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        // Use clip as a default value.
        self.lineBreakMode = NSLineBreakByClipping;
        [self setup];
    }
    return self;
}

- (void)awakeFromNib {
    [self setup];
}

// Draw fade gradient mask if text is wider than rect.
- (void)drawTextInRect:(CGRect)requestedRect {
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSaveGState(context);
    
    CGSize size = [self.text sizeWithFont:self.font];
    if (size.width > requestedRect.size.width + 0.6) { // There is a diff of 0.5 between the auto adjusted label size and the sizeWithFont calculation
        UIImage* image = [[self class]
                          getLinearGradient:requestedRect
                          fadeHead:((self.truncateMode & FadeTruncatingHead) > 0)
                          fadeTail:((self.truncateMode & FadeTruncatingTail) > 0)];
        CGContextClipToMask(context, self.bounds, image.CGImage);
    }
    
    if (self.shadowColor) {
        CGContextSetFillColorWithColor(context, self.shadowColor.CGColor);
        CGRect shadowRect = CGRectOffset(requestedRect, self.shadowOffset.width,
                                         self.shadowOffset.height);
        [self.text drawInRect:shadowRect
                     withFont:self.font
                lineBreakMode:self.lineBreakMode
                    alignment:self.textAlignment];
    }
    
    CGContextSetFillColorWithColor(context, self.textColor.CGColor);
    [self.text drawInRect:requestedRect
                 withFont:self.font
            lineBreakMode:self.lineBreakMode
                alignment:self.textAlignment];
    
    CGContextRestoreGState(context);
}

// Create gradient opacity mask based on direction.
+ (UIImage*)getLinearGradient:(CGRect)rect
                     fadeHead:(BOOL)fadeHead
                     fadeTail:(BOOL)fadeTail {
    // Create an opaque context.
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceGray();
    CGContextRef context = CGBitmapContextCreate(NULL,
                                                 rect.size.width,
                                                 rect.size.height,
                                                 8,
                                                 4*rect.size.width,
                                                 colorSpace,
                                                 (CGBitmapInfo)kCGImageAlphaNone);
    
    // White background will mask opaque, black gradient will mask transparent.
    CGContextSetFillColorWithColor(context, [UIColor whiteColor].CGColor);
    CGContextFillRect(context, rect);
    
    // Create gradient from white to black.
    CGFloat locs[2] = { 0.0f, 1.0f };
    CGFloat components[4] = { 1.0f, 1.0f, 0.0f, 1.0f };
    CGGradientRef gradient =
    CGGradientCreateWithColorComponents(colorSpace, components, locs, 2);
    CGColorSpaceRelease(colorSpace);
    
    // Draw head and/or tail gradient.
    CGFloat fadeWidth = MAX(rect.size.height * 2, floor(rect.size.width / 2));
    CGFloat minX = CGRectGetMinX(rect);
    CGFloat maxX = CGRectGetMaxX(rect);
    if (fadeTail) {
        CGFloat startX = maxX - fadeWidth;
        CGPoint startPoint = CGPointMake(startX, CGRectGetMidY(rect));
        CGPoint endPoint = CGPointMake(maxX, CGRectGetMidY(rect));
        CGContextDrawLinearGradient(context, gradient, startPoint, endPoint, 0);
    }
    if (fadeHead) {
        CGFloat startX = minX + fadeWidth;
        CGPoint startPoint = CGPointMake(startX, CGRectGetMidY(rect));
        CGPoint endPoint = CGPointMake(minX, CGRectGetMidY(rect));
        CGContextDrawLinearGradient(context, gradient, startPoint, endPoint, 0);
    }
    CGGradientRelease(gradient);
    
    // Clean up, return image.
    CGImageRef ref = CGBitmapContextCreateImage(context);
    UIImage* image = [UIImage imageWithCGImage:ref];
    CGImageRelease(ref);
    CGContextRelease(context);
    return image;
}
@end
