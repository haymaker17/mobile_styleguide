//
//  ImageDownloaderOperation.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/11/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ImageDownloaderOperation.h"

// Private properties

@interface ImageDownloaderOperation ()

@property (nonatomic, readwrite, strong) NSIndexPath *indexPathInTableView;
@property (nonatomic, readwrite, strong) DownloadableUIImage *downloadableImage;

@end

@implementation ImageDownloaderOperation

-(id)initWithDownloadableImage:(DownloadableUIImage *)downloadableImage atIndexPath:(NSIndexPath *)indexPath delegate:(id<ImageDownloaderOperationDelegate>)theDelegate
{
    if (self = [super init]) {
        // 2
        self.delegate = theDelegate;
        self.indexPathInTableView = indexPath;
        self.downloadableImage = downloadableImage;
    }
    return self;
    
}

-(void)main
{
    if (self.isCancelled)
        return;
    
    NSData *imageData = [[NSData alloc] initWithContentsOfURL:self.downloadableImage.URL];
    
    if (self.isCancelled) {
        imageData = nil;
        return;
    }
    
    if ([imageData length]) {
        UIImage *downloadedImage = [UIImage imageWithData:imageData];
        self.downloadableImage.image = downloadedImage;
    }
    else {
        self.downloadableImage.failed = YES;
    }
    
    imageData = nil;
    
    if (self.isCancelled)
        return;
    // notify the delegate so the corresponding index is updated
    
    if(self.delegate != nil)
    {
        
     [(NSObject *)self.delegate performSelectorOnMainThread:@selector(ImageDownloaderOperationDidFinish:) withObject:self waitUntilDone:NO];
    }
}

- (BOOL)hasImage {
    return _downloadableImage.image != nil;
}


@end
