//
//  ImageDownloaderOperation.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/11/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AbstractDataSourceDelegate.h"
#import "DownloadableUIImage.h"

@protocol ImageDownloaderOperationDelegate;

@interface ImageDownloaderOperation : NSOperation

@property (nonatomic, assign) id <ImageDownloaderOperationDelegate> delegate;

@property (nonatomic, readonly, strong) NSIndexPath *indexPathInTableView;
@property (nonatomic, readonly, strong) DownloadableUIImage  *downloadableImage;

- (id)initWithDownloadableImage:(DownloadableUIImage *)downloadableImage atIndexPath:(NSIndexPath *)indexPath delegate:(id<ImageDownloaderOperationDelegate>) theDelegate;


@end


// Delegate is used to notify the implementing class when download is complete
@protocol ImageDownloaderOperationDelegate <NSObject>

// call back method to be implemented
- (void)ImageDownloaderOperationDidFinish:(ImageDownloaderOperation *)downloader;
@end
