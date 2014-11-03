//
//  CCActivityIndicatorView.m
//  ConcurMobile
//
//  Created by Wanny Morellato on 12/2/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CCActivityIndicatorView.h"

@implementation CCActivityIndicatorView

- (void)startAnimating{
    static NSMutableArray *frames;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        frames = [NSMutableArray arrayWithCapacity:50];
        NSBundle *imagesBundle = [NSBundle bundleWithURL:[NSURL URLWithString:@"CCActivityIndicatorView.bundle" relativeToURL:[[NSBundle mainBundle]resourceURL]]];
        NSArray *imagesPath = [imagesBundle pathsForResourcesOfType:@"png" inDirectory:nil];
        for (NSString *path in imagesPath){
            [frames addObject:[UIImage imageWithContentsOfFile:path]];
        }

    });
    self.animationImages = frames;
    self.animationDuration = 1.2;
    [self setHidden:NO];
    [super startAnimating];
}

- (void)stopAnimating{
    [super stopAnimating];
    [self setHidden:self.hidesWhenStopped];
}


@end
