//
//  CXPulseMic.h
//  FusionLab
//
//  Created by Richard Puckett on 4/14/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

extern float CX_PULSE_MIC_MIN_DB;
extern float CX_PULSE_MIC_MAX_DB;

typedef NS_ENUM(NSUInteger, CXPulseMicState) {
    CXPulseMicStateInactive,
    CXPulseMicStateRecording,
    CXPulseMicStateWaiting
};

@interface CXPulseMic : UIButton

- (void)setPowerLevel:(CGFloat)powerLevel;
- (void)setState:(CXPulseMicState)state;

@end
