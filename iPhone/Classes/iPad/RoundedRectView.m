//
//  RoundedRectView.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "RoundedRectView.h"

@implementation RoundedRectView

- (id)initWithCoder:(NSCoder *)decoder
{
    if (self = [super initWithCoder:decoder])
    {
        self.strokeColor = kDefaultStrokeColor;
        super.backgroundColor = [UIColor clearColor];
        self.strokeWidth = kDefaultStrokeWidth;
        self.rectangleColor = kDefaultRectColor;
        self.cornerRadius = kDefaultCornerRadius;
		self.isSheenEnabled = YES;
    }
    return self;
}
- (id)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame])
    {
        self.opaque = NO;
        self.strokeColor = kDefaultStrokeColor;
        super.backgroundColor = [UIColor clearColor];
        self.rectangleColor = kDefaultRectColor;
        self.strokeWidth = kDefaultStrokeWidth;
        self.cornerRadius = kDefaultCornerRadius;
		self.isSheenEnabled = YES;
    }
    return self;
}
- (void)setBackgroundColor:(UIColor *)newBGColor
{
    // Ignore any attempt to set background color - backgroundColor must stay set to clearColor
    // We could throw an exception here, but that would cause problems with IB, since backgroundColor
    // is a palletized property, IB will attempt to set backgroundColor for any view that is loaded
    // from a nib, so instead, we just quietly ignore this.
    //
    // Alternatively, we could put an NSLog statement here to tell the programmer to set rectColor...
}
- (void)setOpaque:(BOOL)newIsOpaque
{
    // Ignore attempt to set opaque to YES.
}
- (void)drawRect:(CGRect)rect {
	
	CGContextRef context = UIGraphicsGetCurrentContext();
	CGContextSaveGState(context);
	
	CGContextSetLineWidth(context, self.strokeWidth);
	CGContextSetStrokeColorWithColor(context, self.strokeColor.CGColor);
	CGContextSetFillColorWithColor(context, self.rectangleColor.CGColor);
	
	CGRect rrect = self.bounds;

	CGFloat radius = self.cornerRadius;
	CGFloat width = CGRectGetWidth(rrect);
	CGFloat height = CGRectGetHeight(rrect);
	
	// Make sure corner radius isn't larger than half the shorter side
	if (radius > width/2.0)
		radius = width/2.0;
	if (radius > height/2.0)
		radius = height/2.0;    
	
	/* Set Clip */
	CGFloat minx = CGRectGetMinX(rrect);
	CGFloat midx = CGRectGetMidX(rrect);
	CGFloat maxx = CGRectGetMaxX(rrect);
	CGFloat miny = CGRectGetMinY(rrect);
	CGFloat midy = CGRectGetMidY(rrect);
	CGFloat maxy = CGRectGetMaxY(rrect);
	
	if (!self.isRoundingDisabled)
	{
		CGContextMoveToPoint(context, minx, midy);
		CGContextAddArcToPoint(context, minx, miny, midx, miny, radius);
		CGContextAddArcToPoint(context, maxx, miny, maxx, midy, radius);
		CGContextAddArcToPoint(context, maxx, maxy, midx, maxy, radius);
		CGContextAddArcToPoint(context, minx, maxy, minx, midy, radius);
		CGContextClosePath(context);
		CGContextClip(context);
	}
	
	/* Draw Fill */
	CGRect sheenRect = self.bounds;
	CGContextMoveToPoint(context, 0, 0);
	CGContextAddRect(context, sheenRect);
	CGContextSetFillColorWithColor(context, self.rectangleColor.CGColor);
	CGContextDrawPath(context, kCGPathFill);
	
	/* Draw Sheen */
	if(self.isSheenEnabled){
		CGColorSpaceRef cspace =  CGColorSpaceCreateDeviceRGB();
		UIColor* lightColor =  [UIColor colorWithRed:1 green:1 blue:1 alpha:0.4];
		UIColor* darkColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.1];
		NSMutableArray * colors = [NSMutableArray arrayWithObjects: (__bridge id) lightColor.CGColor, (__bridge id) darkColor.CGColor, nil];
		CGGradientRef gradient = CGGradientCreateWithColors(cspace, (__bridge CFArrayRef) colors, NULL);
		CGContextDrawLinearGradient(context, gradient, CGPointMake(self.frame.origin.x,0), CGPointMake(self.frame.origin.x, self.frame.size.height/2), 0);
		CGColorSpaceRelease(cspace);
		CGGradientRelease(gradient);
	}
	
	/* Draw Stroke */
	if (!self.isRoundingDisabled)
	{
		CGContextMoveToPoint(context, minx, midy);
		CGContextAddArcToPoint(context, minx, miny, midx, miny, radius);
		CGContextAddArcToPoint(context, maxx, miny, maxx, midy, radius);
		CGContextAddArcToPoint(context, maxx, maxy, midx, maxy, radius);
		CGContextAddArcToPoint(context, minx, maxy, minx, midy, radius);
	}
	CGContextClosePath(context);
	if (!self.isRoundingDisabled)
	{
		CGContextSetStrokeColorWithColor(context, self.strokeColor.CGColor);
		CGContextStrokePath(context);
	}	
}

- (void) setRectColor: (UIColor*) rectColor
{
	self.rectangleColor = rectColor;
	[self setNeedsDisplay];
}


@end

@interface RotatingRoundedRectView()
@property (nonatomic, strong)   UIView *parentView;
@property (nonatomic)           float height;
@property (nonatomic)           float width;
@end

@implementation RotatingRoundedRectView
- (id)initCenteredWithParentView:(UIView *)pv withHeight:(float)height withWidth:(float)width
{
    // we store reference to the view itself, as the bounds change during rotation
    self.parentView = pv;
    self.height = height;
    self.width = width;

    self = [self initWithFrame:self.getCenteredFrame];
    return self;
}
- (CGRect)getCenteredFrame
{
    CGRect frame = self.frame;
    // only change the frame if a reference to the parent view was stored, so only new code which
    // calls the new initialiser will use this.
    if (self.parentView != nil)
    {
        float pw = self.parentView.bounds.size.width;
        float ph = self.parentView.bounds.size.height;
        
        // Create the connection view
        frame = CGRectMake((pw - self.width) / 2, (ph - self.height) / 2, self.width, self.height);
    }
    return frame;
}

- (void)drawRect:(CGRect)rect {
	[super drawRect:rect];
	[self rotateToInterfaceOrientation];
}

- (void)rotateToInterfaceOrientation {
    if (!self.isRotatingDisabled)
    {
        self.transform = CGAffineTransformIdentity;
        
        UIInterfaceOrientation orientation = [UIApplication sharedApplication].statusBarOrientation;
        if ([ExSystem is8Plus] && [UIDevice isPad]) {
            // MOB-21236 iOS8 snafu for iPad, iOS8 likes to make the choice itself,
            // if you set this to anything but 0 then iOS8 won't let the view rotate.
            // Limiting this to just iPad for now, if similar problems are noticed elsewhere
            // we will need to do more testing around here.
            CGAffineTransform affine = CGAffineTransformMakeRotation (0.0);
            self.transform = affine;

            // re-center on the view - needed for iOS8
            [self setFrame:self.getCenteredFrame];
        }else if (orientation == UIInterfaceOrientationPortrait) {
            CGAffineTransform affine = CGAffineTransformMakeRotation (0.0);
            self.transform = affine;
        }else if (orientation == UIInterfaceOrientationPortraitUpsideDown) {
            CGAffineTransform affine = CGAffineTransformMakeRotation (M_PI * 180 / 180.0f);
            self.transform = affine;
        }else if (orientation == UIInterfaceOrientationLandscapeLeft) {
            CGAffineTransform affine = CGAffineTransformMakeRotation ( M_PI * 270 / 180.0f);
            self.transform = affine;
        }else if (orientation == UIInterfaceOrientationLandscapeRight) {
            CGAffineTransform affine = CGAffineTransformMakeRotation (M_PI * 90 / 180.0f);  
            self.transform = affine;
        }
    }
}

- (void)onUIInterfaceOrientationChanged {
    [self rotateToInterfaceOrientation];
}
	
@end
