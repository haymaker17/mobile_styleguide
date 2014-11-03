//
//  CXSpeechBubbleView.m
//  FusionLab
//
//  Created by Richard Puckett on 4/10/14.
//  Copyright (c) 2014 Creative Technologies Group. All rights reserved.
//

#import "CXSpeechBubbleView.h"

@interface CXSpeechBubbleView ()

@property (strong, nonatomic) UIImageView *background;

@end

@implementation CXSpeechBubbleView

+ (float)heightForText:(NSString *)text withWidth:(float) width {
    UIFont *font = [UIFont fontWithName:@"HelveticaNeue-Light" size:17];
    
    float textPadding = 10;
    float bubbleConnectorWidth = 13;

    float verticalPadding = textPadding * 2;
    float horizontalPadding = (textPadding * 2) + bubbleConnectorWidth;
    
    float maxFrameWidth = width - horizontalPadding;
    
    CGSize textSize = [text sizeWithFont:font
                       constrainedToSize:CGSizeMake(maxFrameWidth, 10000)
                           lineBreakMode:NSLineBreakByWordWrapping];
    
    return textSize.height + verticalPadding;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
    }
    
    return self;
}

- (id)initWithFrame:(CGRect)frame
       andStatement:(CXStatement *)statement {

    self = [super initWithFrame:frame];
    
    if (self) {
        self.label = [[UILabel alloc] init];
        self.label.text = statement.text;
        self.label.font = [UIFont fontWithName:@"HelveticaNeue-Light" size:17];
        self.label.lineBreakMode = NSLineBreakByWordWrapping;
        self.label.numberOfLines = 0;
        self.label.textColor = [UIColor whiteColor];

        UIEdgeInsets edgeInsets;

        float textPadding = 10;
        float leftTextOffset = 0;
        float rightTextOffset = 0;
        
        float bubbleConnectorWidth = 13;
        
        if (statement.participant == CXParticipantComputer) {
            leftTextOffset = bubbleConnectorWidth;
            edgeInsets = UIEdgeInsetsMake(20, 20, 20, 15);
            UIImage *sourceImage = [UIImage imageNamed:@"text_bubble_gray"];
            UIImage *resizeableImage = [sourceImage resizableImageWithCapInsets:edgeInsets];
            self.background = [[UIImageView alloc] initWithImage:resizeableImage];
        } else {
            rightTextOffset = bubbleConnectorWidth;
            edgeInsets = UIEdgeInsetsMake(20, 15, 20, 20);
            UIImage *sourceImage = [UIImage imageNamed:@"text_bubble_blue"];
            UIImage *resizeableImage = [sourceImage resizableImageWithCapInsets:edgeInsets];
            self.background = [[UIImageView alloc] initWithImage:resizeableImage];
        }
        
        float verticalPadding = textPadding * 2;
        float horizontalPadding = (textPadding * 2) + leftTextOffset + rightTextOffset;
        
        float maxFrameWidth = frame.size.width - horizontalPadding;
        
        CGSize textSize = [statement.text sizeWithFont:self.label.font
                           constrainedToSize:CGSizeMake(maxFrameWidth, 10000)
                               lineBreakMode:NSLineBreakByWordWrapping];

        self.frame = CGRectMake(0, 0,
                                frame.size.width,
                                textSize.height + verticalPadding);
        
        self.label.frame = CGRectMake(textPadding + leftTextOffset,
                                      textPadding,
                                      textSize.width,
                                      textSize.height);
        
        float bubbleWidth = textSize.width + horizontalPadding;
        float bubbleHeight = textSize.height + verticalPadding;
        
        float leftOffset = 0;
        
        if (statement.participant == CXParticipantHuman) {
            leftOffset = frame.size.width - bubbleWidth;
        }
        
        self.background.frame = CGRectMake(leftOffset, 0, bubbleWidth, bubbleHeight);
        
        [self.background addSubview:self.label];
        
        [self addSubview:self.background];
    }
    
    return self;
}

@end
