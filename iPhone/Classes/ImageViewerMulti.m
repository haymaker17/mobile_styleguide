//
//  ImageViewerMulti.m
//  ConcurMobile
//
//  Created by Paul Kramer on 12/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ImageViewerMulti.h"
#import "ExSystem.h" 

#import "ImageUtil.h"
#import "HotelImageData.h"
#import "iPadImageViewerVC.h"


@implementation ImageViewerMulti
@synthesize parentVC, aImageURLs;


#pragma mark -
#pragma mark Image Handlers

-(void)configureWithImagePairs:(NSArray *)propertyImagePairs Owner:(MobileViewController*)owner ImageViewer:(UIImageView*)ivFirst
{
	if (propertyImagePairs == nil)
		return;
	
	NSMutableArray *urls = [self getImageURLs:propertyImagePairs];
	[self fillImagesFromURLs:urls Owner:owner ImageViewer:ivFirst];
}

-(void)configureWithImagePairsForHotel:(EntityHotelBooking *)hotelBooking Owner:(MobileViewController*)owner ImageViewer:(UIImageView*)ivFirst
{
	if (hotelBooking.relHotelImage == nil)
		return;
	
	NSMutableArray *urls = [self getImageURLsForHotel:hotelBooking];
	[self fillImagesFromURLs:urls Owner:owner ImageViewer:ivFirst];
}

-(NSMutableArray*)getImageURLsForHotel:(EntityHotelBooking *)hotelBooking
{
	if (hotelBooking.relHotelImage == nil)
		return nil;
	
	NSUInteger numPropertyImagePairs = [hotelBooking.relHotelImage count];
	
	__autoreleasing NSMutableArray *urls = [[NSMutableArray alloc] initWithCapacity:numPropertyImagePairs];
	NSMutableDictionary *urlsAlreadySeen = [[NSMutableDictionary alloc] initWithCapacity:numPropertyImagePairs];
	
	for(EntityHotelImage *hotelImage in hotelBooking.relHotelImage)
	{
		HotelImageData *hid = [[HotelImageData alloc] init]; // [propertyImagePairs objectAtIndex:];
        hid.hotelThumbnail = hotelImage.thumbURI;
        hid.hotelImage = hotelImage.imageURI;
        
		NSString *url = hid.hotelImage;
		
		if(!urlsAlreadySeen[url])
		{
			urlsAlreadySeen[url] = @"YES";
			[urls addObject:url];
		}
	}
	
	
	return urls;
	
}

-(NSMutableArray*)getImageURLs:(NSArray *)propertyImagePairs
{
	if (propertyImagePairs == nil)
		return nil;
	
	NSUInteger numPropertyImagePairs = [propertyImagePairs count];
	
	__autoreleasing NSMutableArray *urls = [[NSMutableArray alloc] initWithCapacity:numPropertyImagePairs];
	NSMutableDictionary *urlsAlreadySeen = [[NSMutableDictionary alloc] initWithCapacity:numPropertyImagePairs];
	
	for(int i = 0; i < numPropertyImagePairs; i++)
	{
		HotelImageData *hid = propertyImagePairs[i];
		NSString *url = hid.hotelImage;
		
		if(!urlsAlreadySeen[url])
		{
			urlsAlreadySeen[url] = @"YES";
			[urls addObject:url];
		}
	}
	
	
	return urls;
	
}

-(void) fillImagesFromURLs:(NSMutableArray*) imageURLs Owner:(MobileViewController*)owner ImageViewer:(UIImageView*)ivFirst
{
	//we have the URLs, now get the images associated with those urls
	//float w = 70.0;
	//float h = 70.0;
	int iPos = 0;
	//NSMutableArray *images = [[NSMutableArray alloc] initWithObjects:nil];
	for(NSString *imageURL in imageURLs)
	{
		//UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
		
		if(iPos > 0)
		{
			/*
			UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
			[owner.[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageURL RespondToImage:img IV:iv MVC:owner]; //firing off the fetch, loads the image into the imageview
			[iv release];
			*/
			[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageURL RespondToImage:nil IV:nil MVC:owner]; //firing off the fetch, loads the image into the imageview
		}
		else 
		{
			UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
			[ivFirst setImage:img];
			[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageURL RespondToImage:img IV:ivFirst MVC:owner]; //firing off the fetch, loads the image into the imageview
		}
		
		iPos++;
	}
	//the images array should only have one imageview tucked inside of it
	
	//	cellHotel.imageArray = imageURLArray;// images; //here is where we pass off to the cell our images... what we really want to do is pass off the urls...
	//	[_indexPath2 release];
	//	[images release];
	//	[imageURLArray release];
}


-(IBAction) showHotelImages:(id)sender
{
	if (parentVC != nil)
	{
		iPadImageViewerVC *vc = [[iPadImageViewerVC alloc] init];
		[vc loadHotelImages:self.aImageURLs];
		vc.modalPresentationStyle = UIModalPresentationFormSheet;
		[parentVC presentViewController:vc animated:YES completion:nil];
	}
}

@end
