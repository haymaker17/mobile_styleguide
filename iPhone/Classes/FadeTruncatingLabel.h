//
//  FadeTruncatingLabel.h
//  ConcurMobile
//
//  Created by Shifan Wu on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef enum {
    FadeTruncatingTail = 0x1,
    FadeTruncatingHead = 0x2,
    FadeTruncatingHeadAndTail = FadeTruncatingHead | FadeTruncatingTail
} FadeTruncatingMode;

@interface FadeTruncatingLabel : UILabel

// Which side(s) to truncate.
@property(nonatomic, assign) FadeTruncatingMode truncateMode;

// Returns a linear gradient mask suitable to use with CGContextClipToMask() to
// fade the ends of a rectangle the same way this class does it.
+ (UIImage*)getLinearGradient:(CGRect)rect
                     fadeHead:(BOOL)fadeHead
                     fadeTail:(BOOL)fadeTail;

@end
