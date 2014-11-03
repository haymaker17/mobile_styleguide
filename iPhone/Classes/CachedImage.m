//
//  CachedImage.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "CachedImage.h"
#import "CachedImageView.h"

@implementation CachedImage

@synthesize uri;
@synthesize connection;
@synthesize data;
@synthesize error;
@synthesize finishedLoading;
@synthesize listeners;

- (BOOL)loadImageFromUri:(NSString*)imageUri listener:(CachedImageView*)listener
{
	if (uri != nil && ![uri isEqualToString:imageUri])
	{
		// We're already bound to a different URI.  Return fail.
		return NO;
	}
	
	if (finishedLoading || error)
	{
		// We already have the data (or there was an error), so notify the listener immediately
		[self notifyListener:listener];
	}
	else
	{
		// Add the listener to a waiting list to be notified as soon as the data is loadeded (or fails to load)
		if (listeners == nil)
		{
			self.listeners = [[NSMutableArray alloc] init];
		}
		[listeners addObject:listener];

		// If the uri is nil, then the data has not been requested.  Request it now.
		if (self.uri == nil)
		{
			self.uri = imageUri;
			NSURL *url = [NSURL URLWithString:imageUri];
			
			// Create a new connection and request the data
			NSURLRequest* request = [NSURLRequest requestWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:60.0];
			NSURLConnection* newConnection = [[NSURLConnection alloc] initWithRequest:request delegate:self];
			self.connection = newConnection;
		}
	}

	return YES;
}

- (void)connection:(NSURLConnection *)theConnection didReceiveData:(NSData *)incrementalData
{
    if (data == nil)
	{
		self.data = [[NSMutableData alloc] initWithCapacity:2048];
    }
    [data appendData:incrementalData];
}

- (void)connectionDidFinishLoading:(NSURLConnection*)theConnection
{
	self.finishedLoading = true;
    self.connection = nil;
	[self notifyAllListeners];
}

- (void)connection:(NSURLConnection *)theConnection didFailWithError:(NSError *)theError
{
	self.error = theError;
	self.connection = nil;
	[self notifyAllListeners];
}

- (void)notifyAllListeners
{
	// Notify every listener on the waiting list
	for (CachedImageView* listener in listeners)
	{
		[self notifyListener:listener];
	}
	
	// Clear the waiting list
	self.listeners = nil;
}

- (void)notifyListener:(CachedImageView*)imageView
{
	if (error == nil && [data length] > 0)
	{
		[imageView didLoadData:data fromUri:uri];
	}
	else
	{
		[imageView didFailWithError:error fromUri:uri];
	}

}

- (void)dealloc 
{
	if (connection != nil)
	{
		[connection cancel];
	}
}

@end
