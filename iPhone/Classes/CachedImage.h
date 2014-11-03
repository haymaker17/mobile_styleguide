//
//  CachedImage.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class CachedImageView;

@interface CachedImage : NSObject
{
	NSString		*uri;
	NSURLConnection	*connection;
    NSMutableData	*data;
	NSError			*error;
	BOOL			finishedLoading;
	NSMutableArray	*listeners;
}

@property (nonatomic, strong) NSString			*uri;
@property (nonatomic, strong) NSURLConnection	*connection;
@property (nonatomic, strong) NSMutableData		*data;
@property (nonatomic, strong) NSError			*error;
@property (nonatomic) BOOL						finishedLoading;
@property (nonatomic, strong) NSMutableArray	*listeners;

- (BOOL)loadImageFromUri:(NSString*)imageUri listener:(CachedImageView*)listener;
- (void)notifyListener:(CachedImageView*)imageView;
- (void)notifyAllListeners;

@end
