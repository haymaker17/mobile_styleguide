//
//  CachedImageView.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ImageCache;

@interface CachedImageView : UIView
{
	NSString	*uri;
	UIView		*imageLoadingView;
	UIView		*imageNotAvailableView;
}

@property (nonatomic, strong) NSString			*uri;
@property (nonatomic, strong) IBOutlet UIView	*imageLoadingView;
@property (nonatomic, strong) IBOutlet UIView	*imageNotAvailableView;

- (void)loadDataFromUri:(NSString*)imageUri cache:(ImageCache*)cache;
- (void)didLoadData:(NSData*)data fromUri:(NSString*)imageUri;
- (void)didFailWithError:(NSError*)data fromUri:(NSString*)imageUri;

@end
