//
//  CCMicControl.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CCMicControl.h"

#import <QuartzCore/QuartzCore.h>
#import <AVFoundation/AVFoundation.h>
#import <AudioToolbox/AudioToolbox.h>

@interface CCMicControl ()

@property (strong,nonatomic) UIImageView *microphoneShape;
@property (strong,nonatomic) UIImageView *volumeIndicator;
@property (strong,nonatomic) UIImageView *activityIndicator;
@property CGFloat currentVolume;


@property (nonatomic,strong) AVPlayer *playerStartSound;
@property (nonatomic,strong) AVPlayer *playerStopSound;

@property (nonatomic,copy) void(^onPlayerDidReachEnd)(void);

@end

@implementation CCMicControl

- (id)initWithCoder:(NSCoder *)aDecoder{
    self = [super initWithCoder:aDecoder];
    if (self) {
        // Initialization code
        [self setup];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        [self setup];
    }
    return self;
}

+ (CGRect)subRectFromRect:(CGRect)rect{
    CGFloat maxDimension = MIN(rect.size.width, rect.size.height)*0.65;
    // subrect squared and centered
    //                                                                                    it \/ should be 2 but we want it a little bit more down
    return CGRectMake((rect.size.width - maxDimension)/2, (rect.size.height - maxDimension)/2, maxDimension, maxDimension);
}

- (void)setFrame:(CGRect)frame{
    [super setFrame:frame];
    CGRect subRect = [CCMicControl subRectFromRect:self.bounds]; //  CGRectMake((self.bounds.size.width - maxDimension)/2, (self.bounds.size.height - maxDimension)/2, maxDimension, maxDimension);
    [self.microphoneShape setFrame:subRect];
    
    // make the volume indicator drawbed square so circles are drawn inside
    float smallerDimension = MIN(frame.size.width, frame.size.height);
    float xOffsetForCentering = (frame.size.width - smallerDimension) / 2;
    float yOffsetForCentering = (frame.size.height - smallerDimension) / 2;
    [self.volumeIndicator setFrame:CGRectMake(xOffsetForCentering, yOffsetForCentering, smallerDimension, smallerDimension)];

    [self.activityIndicator setFrame:subRect];
    
    [self.message setFrame:CGRectMake(0, 0, self.bounds.size.width, self.bounds.size.height*0.2)];
}
- (void)setup{
    //subrect square max dimension
    
    CGRect subRect = [CCMicControl subRectFromRect:self.bounds];
    
    self.microphoneShape = [[UIImageView alloc] initWithFrame:subRect];
    self.volumeIndicator = [[UIImageView alloc] initWithFrame:subRect];
    self.volumeIndicator.contentMode = UIViewContentModeScaleAspectFit;
    self.activityIndicator = [[UIImageView alloc] initWithFrame:subRect];
    
    [self.volumeIndicator setHidden:YES];
    
    self.message = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, self.bounds.size.width, self.bounds.size.height*0.2)];
    self.message.textAlignment = NSTextAlignmentCenter;
    [self.message setBackgroundColor:[UIColor clearColor]];
    [self.message setTextColor:[UIColor whiteColor]];
    self.message.adjustsFontSizeToFitWidth = YES;
    [self.message setText:@"Tap microphone to get started"];
    [self endSpinning];
    [self setPlayerStartSound:[[AVPlayer alloc] initWithURL:[[NSBundle mainBundle] URLForResource: @"voice_high"
                                                                                withExtension: @"aif"]]];
    [self setPlayerStopSound:[[AVPlayer alloc] initWithURL:[[NSBundle mainBundle] URLForResource: @"voice_low"
                                                                                   withExtension: @"aif"]]];
    [self setAlpha:0.75];
}

- (void)willMoveToSuperview:(UIView *)newSuperview{
    [super willMoveToSuperview:newSuperview];
    
    [self.microphoneShape setImage:[UIImage imageNamed:@"button_voice_clear"]];
    [self.activityIndicator setImage:[UIImage imageNamed:@"button_voice_spinner"]];
    
    [self addSubview:self.microphoneShape];
    [self addSubview:self.volumeIndicator];
    [self addSubview:self.activityIndicator];
    [self addSubview:self.message];
        
}

- (void)setVolumeValue:(CGFloat)value{
    self.currentVolume = value;
    [self setNeedsDisplay];
}

- (void)beginSpinning{
    [self.volumeIndicator setHidden:YES];
    [self.activityIndicator setHidden:NO];
    [UIView animateWithDuration:0.3 animations:^{
        [self.microphoneShape setAlpha:0.0];
    }];
    CABasicAnimation* rotationAnimation;
    rotationAnimation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
    rotationAnimation.toValue = [NSNumber numberWithFloat: M_PI * 2.0 /* full rotation*/];
    rotationAnimation.duration = 2.0;
    rotationAnimation.cumulative = YES;
    rotationAnimation.repeatCount = HUGE_VALF;
    [self.activityIndicator.layer addAnimation:rotationAnimation forKey:@"rotationAnimation"];
}

- (void)endSpinning{
    [UIView animateWithDuration:0.3 animations:^{
        [self.microphoneShape setAlpha:1.0];
    }];
    [self.volumeIndicator setHidden:YES];
    [self.activityIndicator setHidden:YES];
    [self.activityIndicator.layer removeAllAnimations];
}

- (void)start:(BOOL)animated withSound:(BOOL)enabled onDidReachEnd:(void (^) (void))onDidReachEndBlock {
 
     [self setOnPlayerDidReachEnd:onDidReachEndBlock];
    if (enabled) {
        [self.playerStartSound seekToTime:kCMTimeZero];
        UInt32 sessionCategory = kAudioSessionCategory_PlayAndRecord;
        AudioSessionSetProperty(kAudioSessionProperty_AudioCategory, sizeof(sessionCategory), &sessionCategory);
        
        UInt32 audioRouteOverride = kAudioSessionOverrideAudioRoute_Speaker;
        AudioSessionSetProperty (kAudioSessionProperty_OverrideAudioRoute,sizeof (audioRouteOverride),&audioRouteOverride);
        
        if (self.onPlayerDidReachEnd != nil) {
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerItemDidReachEnd:) name:AVPlayerItemDidPlayToEndTimeNotification object:[self.playerStartSound currentItem]];
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(playerItemDidFailed:) name:AVPlayerItemFailedToPlayToEndTimeNotification object:[self.playerStartSound currentItem]];
        }

        
        [self.playerStartSound play];
        [self.playerStartSound setRate:1.5];
        
        [self.volumeIndicator setHidden:NO];
    }
}

- (void)stop:(BOOL)animated withSound:(BOOL)enabled {
    
    if (enabled) {
        [self.playerStopSound seekToTime:kCMTimeZero];
        UInt32 sessionCategory = kAudioSessionCategory_PlayAndRecord;
        AudioSessionSetProperty(kAudioSessionProperty_AudioCategory, sizeof(sessionCategory), &sessionCategory);
        
        UInt32 audioRouteOverride = kAudioSessionOverrideAudioRoute_Speaker;
        AudioSessionSetProperty (kAudioSessionProperty_OverrideAudioRoute,sizeof (audioRouteOverride),&audioRouteOverride);
        
        
        [self.playerStopSound play];
        [self.playerStartSound setRate:1.5];
        [self.volumeIndicator setHidden:YES];
    }
}

// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // volume 0 to 1
    float volumeScalar = self.currentVolume;
    
    // experimentally determiend to create a circle just outside the circle of the current mic image
    const float CIRCLE_MIN = .64;
    // allows circle to range from minimum circle size to 1
    float circleScalar = CIRCLE_MIN + volumeScalar * (1-CIRCLE_MIN);
    // experimentally determined to be the maximum circle scalar allowed given the current line width before clipping happens
    if( circleScalar > .98 )
        circleScalar = .98;
    
    //NSLog(@"Mic Volume %f", volumeScalar);
    
    self.volumeIndicator.image = [[UIImage alloc] init];

    UIGraphicsBeginImageContext(self.volumeIndicator.frame.size);
    {
        CGContextRef context = UIGraphicsGetCurrentContext();
        
        CGPoint center;
        center.x = self.volumeIndicator.frame.size.width/2;
        center.y = self.volumeIndicator.frame.size.height/2;
        
        CGContextSetLineWidth(context, 2.0);
        
        CGContextSetStrokeColorWithColor(context, [UIColor colorWithRed:223.0f/255.0f green:0 blue:0 alpha:1].CGColor);
        
        float volumeSizerX = self.volumeIndicator.frame.size.width * circleScalar;
        float volumeSizerY = self.volumeIndicator.frame.size.height * circleScalar;
        
        float ellipseOriginX = center.x - volumeSizerX/2;
        float ellipseOriginY = center.y - volumeSizerY/2;
        float ellipseWidth = volumeSizerX;
        float ellipseHeight = volumeSizerY;
        
        CGRect ellipseBounds = CGRectMake(ellipseOriginX, ellipseOriginY, ellipseWidth, ellipseHeight);
        
        CGContextAddEllipseInRect(context, ellipseBounds);
        
        CGContextStrokePath(context);
        
        self.volumeIndicator.image = UIGraphicsGetImageFromCurrentImageContext();
    }
    UIGraphicsEndImageContext();
}


- (void)playerItemDidReachEnd:(NSNotification *)notification {
    [[NSNotificationCenter defaultCenter] removeObserver:AVPlayerItemDidPlayToEndTimeNotification];
    if (self.onPlayerDidReachEnd) {
        self.onPlayerDidReachEnd();
        self.onPlayerDidReachEnd = nil;
    }
    
}

- (void)playerItemDidFailed:(NSNotification *)notification {
    NSLog(@"playerItemDidFailed current Item %@",[self.playerStartSound.currentItem debugDescription]);
    NSLog(@"playerItemDidFailed %@",[self.playerStartSound.error localizedDescription]);
    [[NSNotificationCenter defaultCenter] removeObserver:AVPlayerItemDidPlayToEndTimeNotification];
    
    if (self.onPlayerDidReachEnd) {
        self.onPlayerDidReachEnd();
        self.onPlayerDidReachEnd = nil;
    }
    
}

@end
