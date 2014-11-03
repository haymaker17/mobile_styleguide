//
//  CCMicControl.h
//  ConcurMobile
//
//  Created by Wanny Morellato on 7/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CCMicControl : UIControl

@property (strong,nonatomic) UILabel *message;

- (void)beginSpinning;
- (void)endSpinning;

/*
 * set the volume indicator to the new value 
 * value should be in [0.0-1.0] interval
 */
- (void)setVolumeValue:(CGFloat)value;

//- (void)start:(BOOL)animated withSound:(BOOL)enabled;
- (void)stop:(BOOL)animated withSound:(BOOL)enabled;
- (void)start:(BOOL)animated withSound:(BOOL)enabled onDidReachEnd:(void (^) (void))onDidReachEndBlock;


@end
