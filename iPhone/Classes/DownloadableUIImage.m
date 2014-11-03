//
//  DownloadableUIImage.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/11/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "DownloadableUIImage.h"

@implementation DownloadableUIImage


- (BOOL)hasImage {
    return _image != nil;
}


- (BOOL)isFailed {
    return _failed;
}


@end
