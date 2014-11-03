//
//  CachedImageView.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CachedImageView.h"
#import "ImageCache.h"
#import "CachedImage.h"

@implementation CachedImageView

@synthesize uri;
@synthesize imageLoadingView;
@synthesize imageNotAvailableView;

- (void)loadDataFromUri:(NSString*)imageUri cache:(ImageCache*)cache
{
	// Remove all views except the imageLoadingView and imageNotAvailableView.
	// This is necessary because this CachedImageView might be reused as it is
	// when it is part of a dequeued table cell
	for (UIView* subview in self.subviews)
	{
		if (subview != imageLoadingView && subview != imageNotAvailableView)
			[subview removeFromSuperview];
	}
	
	[imageNotAvailableView setHidden:YES];
	[imageLoadingView setHidden:NO];
	
	self.uri = imageUri;
	
	if (cache != nil)
	{
		[cache loadDataFromUri:imageUri listener:self];
	}
	else
	{
		CachedImage* imageData = [[CachedImage alloc] init];
		[imageData loadImageFromUri:imageUri listener:self];
	}
}

- (void)didLoadData:(NSData*)data fromUri:(NSString*)imageUri
{
	if (![imageUri isEqualToString:self.uri])
	{
		// This is not the image we're waiting for, so ignore it.  Note: this scenario can occur
		// when the CachedImageView is reused, as when it is part of a table cell that is dequeued.
		return;
	}
	
	[imageNotAvailableView setHidden:YES];
	[imageLoadingView setHidden:YES];
	
    UIImageView* imageView = [[UIImageView alloc] initWithImage:[UIImage imageWithData:data]];
	
    imageView.contentMode = UIViewContentModeScaleAspectFit;
    imageView.autoresizingMask = ( UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight );
	
    [self addSubview:imageView];
    imageView.frame = self.bounds;
    [imageView setNeedsLayout];
    [self setNeedsLayout];
}

- (void)didFailWithError:(NSError*)data fromUri:(NSString*)imageUri
{
	[imageLoadingView setHidden:YES];
	[imageNotAvailableView setHidden:NO];
}



@end
