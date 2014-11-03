//
//  CXPulseMic.m
//  FusionLab
//
//  Created by Richard Puckett on 4/14/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import <QuartzCore/QuartzCore.h>

#import "CXPulseMic.h"

// Empirical bounds on mic power that seem to give
// good visual feedback for conversational speech.
//
float CX_PULSE_MIC_MIN_DB = -30.0;
float CX_PULSE_MIC_MAX_DB = -5.0;

float CX_PULSE_MIC_HALO_SIZE = 85.0;

float CX_PULSE_MIC_ICON_WIDTH = 35.0;
float CX_PULSE_MIC_ICON_HEIGHT = 50.0;

// Smooth out the impulses just a small amount.
//
float CX_PULSE_MIC_LOWPASS_ALPHA = 0.8;

typedef NS_ENUM(NSUInteger, CXPulseMicInternalState) {
    CXPulseMicInternalStateWaitStarting,
    CXPulseMicInternalStateWaitEnding
};

@interface CXPulseMic ()

@property (assign) CGFloat decibels;
@property (assign) CGFloat powerRange;
@property (strong, nonatomic) UIImage *micImage;
@property (assign) CXPulseMicState micState;
@property (strong, nonatomic) NSMutableArray *waitFrames;
@property (strong, nonatomic) NSMutableArray *waitEndFrames;
@property (assign) float frameInterval;
@property (strong, nonatomic) NSTimer *updateTimer;
@property (assign) NSUInteger curWaitFrameIndex;
@property (assign) float haloPadding;
@property (assign) float minPulseTravelValue;
@property (assign) float micImageWidth;
@property (assign) float micImageHeight;

@end

@implementation CXPulseMic

- (id)initWithCoder:(NSCoder *)decoder {
    self = [super initWithCoder:decoder];
    
    if (self) {
        [self setup];
    }
    
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
        [self setup];
    }
    
    return self;
}

- (void)setup {
    self.frameInterval = 1.0 / 15; // 15 FPS
    self.micState = CXPulseMicStateInactive;
    self.powerRange = CX_PULSE_MIC_MAX_DB - CX_PULSE_MIC_MIN_DB;
    self.micImage = [UIImage imageNamed:@"fusion2014_mic_wait"];
    self.decibels = CX_PULSE_MIC_MIN_DB;
    self.haloPadding = 40;
    self.minPulseTravelValue = 1.5;
    
    self.micImageHeight = self.micImage.size.height;
    self.micImageWidth = self.micImage.size.width;
    
    self.waitFrames = [[NSMutableArray alloc] init];
    for (int i = 0; i < 20; i++) {
        NSString *filename = [NSString stringWithFormat:@"sp_normal_%02d", i];
        [self.waitFrames addObject:[UIImage imageNamed:filename]];
    }
    
    self.waitEndFrames = [[NSMutableArray alloc] init];
    for (int i = 0; i < 20; i++) {
        NSString *filename = [NSString stringWithFormat:@"sp_close_%02d", i];
        [self.waitEndFrames addObject:[UIImage imageNamed:filename]];
    }
}

- (void)drawRect:(CGRect)rect {
    switch (self.micState) {
        case CXPulseMicStateInactive:
        case CXPulseMicStateRecording:
            [self drawActiveState:rect];
            break;
        case CXPulseMicStateWaiting:
            [self drawWaitState:rect];
            break;
    }
}

- (void)drawActiveState:(CGRect)rect {
    CGRect micRect = rect;
    
    micRect.size.width = CX_PULSE_MIC_ICON_WIDTH;
    micRect.size.height = CX_PULSE_MIC_ICON_HEIGHT;
    
    float xOffset = (rect.size.width - CX_PULSE_MIC_ICON_WIDTH) / 2;
    float yOffset = (rect.size.height - CX_PULSE_MIC_ICON_HEIGHT) / 2;
    
    micRect.origin.x += xOffset;
    micRect.origin.y += yOffset;

    float curPowerPercentage = (self.decibels - CX_PULSE_MIC_MIN_DB) / self.powerRange;
    float pulseTravelRange = (self.frame.size.height - CX_PULSE_MIC_HALO_SIZE) / 2;
    float pulseTravelValue = pulseTravelRange * curPowerPercentage;
    
    if (pulseTravelValue < self.minPulseTravelValue) {
        pulseTravelValue = self.minPulseTravelValue;
    }

    float minLength = fmax(CGRectGetHeight(micRect), CGRectGetWidth(micRect)) + self.haloPadding/2;
    CGRect minFrame = CGRectMake(0, 0, minLength, minLength);
    
    xOffset = (rect.size.width - minLength) / 2;
    yOffset = (rect.size.height - minLength) / 2;
    
    minFrame.origin.x += xOffset;
    minFrame.origin.y += yOffset;
    
    CGRect pulseRect = CGRectInset(minFrame, -pulseTravelValue, -pulseTravelValue);

    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetStrokeColor(context, CGColorGetComponents([[UIColor colorWithRed:0 green:120/255.0 blue:200/255.0 alpha:1] CGColor]));
    CGContextSetLineWidth(context, pulseTravelValue);
    CGContextStrokeEllipseInRect(context, pulseRect);
    
    [self.micImage drawInRect:micRect];
}

- (void)drawWaitState:(CGRect)rect {
    //NSLog(@"drawWaitStartState %@", NSStringFromCGRect(rect));
    
    CGRect micRect = rect;
    
    micRect.size.width = CX_PULSE_MIC_ICON_WIDTH;
    micRect.size.height = CX_PULSE_MIC_ICON_HEIGHT;
    
    float xOffset = (rect.size.width - CX_PULSE_MIC_ICON_WIDTH) / 2;
    float yOffset = (rect.size.height - CX_PULSE_MIC_ICON_HEIGHT) / 2;
    
    micRect.origin.x += xOffset;
    micRect.origin.y += yOffset;
    
    [self.micImage drawInRect:micRect];
    
    // A little hand-tuning here to account for intrinsic size issues in the wait frames.
    //
    float minLength = fmax(CGRectGetHeight(micRect), CGRectGetWidth(micRect)) + (self.haloPadding+8)/2;
    CGRect minFrame = CGRectMake(0, 0, minLength, minLength);
    
    xOffset = (rect.size.width - minLength) / 2;
    yOffset = (rect.size.height - minLength) / 2;
    
    minFrame.origin.x += xOffset;
    minFrame.origin.y += yOffset;
    
    UIImage *img = [self.waitFrames objectAtIndex:self.curWaitFrameIndex];
    self.curWaitFrameIndex = self.curWaitFrameIndex + 1;
    self.curWaitFrameIndex %= 20;
    
    [img drawInRect:minFrame];
}

- (void)setPowerLevel:(CGFloat)decibels {
    if (self.micState == CXPulseMicStateRecording) {
        // Cap the input so we can display it properly for our bounds.
        //
        float cappedDecibels = decibels;
        
        if (decibels < CX_PULSE_MIC_MIN_DB) {
            cappedDecibels = CX_PULSE_MIC_MIN_DB;
        } else if (decibels > CX_PULSE_MIC_MAX_DB) {
            cappedDecibels = CX_PULSE_MIC_MAX_DB;
        }
        
        // Run new value through low pass filter. Removes jitter.
        //
        self.decibels = CX_PULSE_MIC_LOWPASS_ALPHA * cappedDecibels +
        (1.0 - CX_PULSE_MIC_LOWPASS_ALPHA) * self.decibels;
        
        [self setNeedsDisplay];
    }
}

- (void)setInactive {
    self.micState = CXPulseMicStateInactive;
    [self setNeedsDisplay];
}

- (void)setRecording {
    self.micState = CXPulseMicStateRecording;
    [self setNeedsDisplay];
}

- (void)setWaiting {
    if (self.micState != CXPulseMicStateWaiting) {
        self.micState = CXPulseMicStateWaiting;
        
        self.updateTimer = [NSTimer scheduledTimerWithTimeInterval:self.frameInterval
                                                            target:self
                                                          selector:@selector(updateWaitView:)
                                                          userInfo:nil
                                                           repeats:YES];
    }
}

- (void)setState:(CXPulseMicState)state {
    switch (state) {
        case CXPulseMicStateInactive:
            [self setInactive];
            break;
        case CXPulseMicStateRecording:
            [self setRecording];
            break;
        case CXPulseMicStateWaiting:
            [self setWaiting];
            break;
    }
}

- (void)updateWaitView:(NSTimer *)timer {
    //NSLog(@"updateView");
    
    // We just switched away from waiting. Stop the timer.
    //
    if (self.micState != CXPulseMicStateWaiting) {
        [timer invalidate];
    } else {
        [self setNeedsDisplay];
    }
}

@end
