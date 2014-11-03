//
//  DistractorImageView.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/30/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/**
 The code is copied from Richard Puckett's work CXDistractor.m
 */

#import "DistractorImageView.h"

@implementation DistractorImageView

- (id)initWithCoder:(NSCoder *)decoder {
    self = [super initWithCoder:decoder];
    
    if (self) {
        [self loadFrames];
    }
    
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    
    if (self) {
        [self loadFrames];
    }
    
    return self;
}

- (void)loadFrames {
    self.animationDuration = 1;
    
    NSMutableArray *imagesFrames = [[NSMutableArray alloc] init];
    
    for (int i = 0; i < 20; i++) {
        NSString *imageSource = [NSString stringWithFormat:@"spinner18fps_%03d.png", i];
        UIImage *image = [UIImage imageNamed:imageSource];
        [imagesFrames addObject:image];
    }
    
    self.animationImages = imagesFrames;
}

@end
