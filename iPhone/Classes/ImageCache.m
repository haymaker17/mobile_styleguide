//
//  ImageCache.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ImageCache.h"
#import "CachedImageView.h"
#import "CachedImage.h"

@implementation ImageCache

@synthesize cachedImageDictionary;

- (void)loadDataFromUri:(NSString*)imageUri listener:(CachedImageView*)listener
{
	// If we don't already have a dictionary of cached images, then create one.
	if (cachedImageDictionary == nil)
	{
		NSMutableDictionary* newCachedImagedDictionary = [[NSMutableDictionary alloc] init];
		self.cachedImageDictionary = newCachedImagedDictionary;
	}
	
	// If a cached image for imageUri does not already exist in the dictionary,
	// then create and add one.
	if (cachedImageDictionary[imageUri] == nil)
	{
		CachedImage* newCachedImage = [[CachedImage alloc] init];
		cachedImageDictionary[imageUri] = newCachedImage;
	}
	CachedImage *cachedImage = cachedImageDictionary[imageUri];
	
	// Tell the image to load and to notify the listener when it's done loading.
	[cachedImage loadImageFromUri:imageUri listener:listener];
}

- (void) clear
{
	self.cachedImageDictionary = nil;
}


@end
