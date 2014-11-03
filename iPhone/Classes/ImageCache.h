//
//  ImageCache.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class CachedImageView;

@interface ImageCache : NSObject
{
	NSMutableDictionary *cachedImageDictionary;
}

@property (nonatomic, strong) NSMutableDictionary *cachedImageDictionary;

- (void)loadDataFromUri:(NSString*)imageUri listener:(CachedImageView*)listener;
- (void)clear;

@end
