//
//  AyncImageView.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AsyncImageView.h"


@implementation AsyncImageView

- (void)loadImageFromURL:(NSURL*)url {
    if (connection!=nil) { [connection cancel];  }
    NSURLRequest* request = [NSURLRequest requestWithURL:url
											 cachePolicy:NSURLRequestUseProtocolCachePolicy
										 timeoutInterval:60.0];
	data = nil;
    connection = [[NSURLConnection alloc]
				  initWithRequest:request delegate:self];
    //TODO error handling, what if connection is nil?
}

- (void)connection:(NSURLConnection *)theConnection
	didReceiveData:(NSData *)incrementalData {
    if (data==nil) {
		data =
		[[NSMutableData alloc] initWithCapacity:2048];
    }
    [data appendData:incrementalData];
}

- (void)connectionDidFinishLoading:(NSURLConnection*)theConnection {
    connection=nil;
	
    if ([[self subviews] count] > 0) {
        [[self subviews][0] removeFromSuperview];
    }
	
    UIImageView* imageView = [[UIImageView alloc] initWithImage:[UIImage imageWithData:data]];
	
    imageView.contentMode = UIViewContentModeScaleAspectFit;
    imageView.autoresizingMask = ( UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight );
	
    [self addSubview:imageView];
    imageView.frame = self.bounds;
    [imageView setNeedsLayout];
    [self setNeedsLayout];
    data=nil;
}

- (UIImage*) image {
    UIImageView* iv = [self subviews][0];
    return [iv image];
}

- (void)dealloc {
    [connection cancel];
}

@end
