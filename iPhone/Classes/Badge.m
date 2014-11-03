//
//  Badge.m
//  ConcurMobile
//
//  Created by ernest cho on 10/31/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Badge.h"

@interface Badge()
@property (nonatomic, readwrite, strong) NSNumber *badgeCount;
@property (nonatomic, readwrite, strong) NSNumber *badgeWidth;
@property (nonatomic, readwrite, strong) NSNumber *badgeHeight;
@end

@implementation Badge

/**
 Init for Interface Builder
 */
- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self sharedInit];
    }
    return self;
}

/**
 Init for programmatically built UIs
 */
- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self sharedInit];
    }
    return self;
}

- (void)sharedInit
{
    self.badgeCount = [NSNumber numberWithInt:0];

    // I subtract 2 from the size.  Otherwise rounding might lose the edge
    float height = self.frame.size.height - 2;
    self.badgeHeight = [NSNumber numberWithFloat:height];

    // same thing here with the width.  This is the max width
    float width = self.frame.size.width - 2;
    self.badgeWidth = [NSNumber numberWithFloat:width];
}

/**
 Sets the badge count and invalidates the draw
 */
- (void)updateBadgeCount:(NSNumber *)count
{
    self.badgeCount = count;
    [self setNeedsDisplay];
}

/**
 Returns display text
 */
- (NSString *)displayText
{
    // Hard code a cap.  I would prefer not to have this...
    if (self.badgeCount.integerValue > 999) {
        return @"999+";
    }
    return [self.badgeCount stringValue];
}

/**
 The badge font
 */
- (UIFont *)badgeFont
{
    float tmp = self.badgeHeight.floatValue;

    // the best font size appears to be 4/5 of the height of the view
    float margin = tmp/10;
    int fontSize = (int)(tmp - margin - margin);

    return [UIFont systemFontOfSize:fontSize];
}

/**
 Attributes for the badge text
 */
- (NSDictionary *)badgeTextAttributes
{
    NSMutableParagraphStyle *textStyle = [[NSMutableParagraphStyle defaultParagraphStyle] mutableCopy];
    textStyle.lineBreakMode = NSLineBreakByWordWrapping;
    textStyle.alignment = NSTextAlignmentRight;

    // TEXT Color is set here!!!
    NSDictionary *dictionary = @{ NSFontAttributeName: [self badgeFont],
                                  NSParagraphStyleAttributeName: textStyle,
                                  NSForegroundColorAttributeName: [UIColor whiteColor] };

    return dictionary;
}

/**
 Override the default drawing of this UIView.
 */
- (void)drawRect:(CGRect)rect
{
    if (self.badgeCount.intValue > 0) {
        [self drawBadge];
    } else {
        [self drawNothing];
    }
}

/**
 Draws nothing
 */
- (void)drawNothing
{
    // make view transparent
    self.alpha = 0;
}

/**
 Draws a badge
 
 Just learned that UILabel has a nice little corner circle feature.  Could have avoided a lot of geometry with that knowledge...
 */
- (void)drawBadge
{
    // make view visible
    self.alpha = 1;
    [self drawBadgeBackground];
    [self drawBadgeText];
}

/**
 Draw text over the badge
 */
- (void)drawBadgeText
{
    float height = self.badgeHeight.floatValue;
    // the text is inset from the edge by (height/4) on each side
    float width = self.badgeWidth.floatValue - (height/2);

    // the text is inset from the edge by (height/4)
    float x = (height/4);
    float y = 1;

    NSString *text = [self displayText];

    // iOS7 way of drawing this
    //[text drawInRect:CGRectMake(x, y, width, height) withAttributes:[self badgeTextAttributes]];

    // iOS6 way of drawing this, yea this sucks
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetStrokeColorWithColor(context, [UIColor whiteColor].CGColor);
    CGContextSetFillColorWithColor(context, [UIColor whiteColor].CGColor);

    [text drawInRect:CGRectMake(x, y, width, height) withFont:[self badgeFont] lineBreakMode:NSLineBreakByWordWrapping alignment:NSTextAlignmentRight];
}

/**
 Draw the capsule background
 */
- (void)drawBadgeBackground
{
    float height = self.badgeHeight.floatValue;
    // the box width, remove the half circle on each end.  The diameter is height.
    float width = self.badgeWidth.floatValue - height;

    // try to get a more reasonable width.  I get the text width and use that instead of the view width.
    // iOS7 way of getting size
    //CGSize textSize = [[self displayText] sizeWithAttributes:[self badgeTextAttributes]];
    // iOS6 way of getting size
    CGSize textSize = [[self displayText] sizeWithFont:[self badgeFont]];

    // the text box is a little wider than the background box cause we start the text within the circles
    // subtract (height/2) to adjust for it
    float boxSize = textSize.width - (height/2);
    float differenceBetweenBoxSizes = 0;

    if (boxSize < width) {
        differenceBetweenBoxSizes = width - boxSize;
        width = boxSize;

        // enforce min width, we don't want to go below a circle.  This happens with 1.
        if (width < 0) {
            differenceBetweenBoxSizes = differenceBetweenBoxSizes + width;
            width = 0;
        }
    }

    // the box starts after the half circle
    float x = (height/2) + differenceBetweenBoxSizes;
    float y = 1;

    CGContextRef context = UIGraphicsGetCurrentContext();

    // Make path
    CGContextBeginPath(context);

    // rectangle
    CGRectMake(x, y, width, height);

    // right curve
    CGContextAddArc(context, x + width, y + (height/2), (height/2), M_PI/2, M_PI * 3/2, YES);

    // left curve
    CGContextAddArc(context, x, y + (height/2), (height/2), M_PI * 3/2, M_PI/2, YES);

    // finish path
    CGContextClosePath(context);

    // BACKGROUND Color is set here!!!
    // Set line and fill color to the orange color in spec
    CGContextSetRGBFillColor(context, 247.0/255.0, 113.0/255.0, 19.0/255.0, 1.0);
    CGContextSetRGBStrokeColor(context, 247.0/255.0, 113.0/255.0, 19.0/255.0, 1.0);
    
    // Draw
    CGContextDrawPath(context, kCGPathFillStroke);
}

@end
