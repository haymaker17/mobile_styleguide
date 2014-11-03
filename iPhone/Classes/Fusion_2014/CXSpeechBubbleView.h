//
//  CXSpeechBubbleView.h
//  FusionLab
//
//  Created by Richard Puckett on 4/10/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import "CXStatement.h"

@interface CXSpeechBubbleView : UIView

@property (strong, nonatomic) UILabel *label;

+ (float)heightForText:(NSString *)text withWidth:(float) width;

- (id)initWithFrame:(CGRect)frame
       andStatement:(CXStatement *)statement;

@end
