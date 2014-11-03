//
//  ImageControl.m
//  ConcurMobile
//
//  Created by Paul Kramer on 2/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ImageControl.h"
#import "ExSystem.h"


@implementation ImageControl
@synthesize imageURL;
@synthesize imageName;
@synthesize imageType;
@synthesize imageDimension;
@synthesize vendorCode;
@synthesize isVendor;
@synthesize vendorType;
@synthesize isCliqImage;
@synthesize image;
@synthesize exSys;
@synthesize vendorImageDict;

-(NSData *)fetchFromDisk:(NSString *)imageFileName FolderType:(NSString *)folderType
{//retrieves the cached image, if available
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"[%@]%@",folderType, imageFileName]];
	NSData *data = [NSData dataWithContentsOfFile:initFilePath]; 
	//NSLog(@"Len=%d", [data length]);
	return data;
}

-(NSData *)fetchFromDiskPure:(NSString *)imageFileName
{//retrieves the cached image, if available
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",imageFileName]];
	NSData *data = [NSData dataWithContentsOfFile:initFilePath]; 
	//NSLog(@"Len=%d", [data length]);
	return data;
}


-(NSData *)fetchFromURL:(NSString *)imageFileName FetchURL:(NSString *)xURL VendorType:(NSString *)vType
{//retrieves the cached image, if available
	NSData *data = [NSData dataWithContentsOfURL:[NSURL URLWithString:xURL]];
	if (data != nil)
	{
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"[%@]%@",vType, imageFileName]];
		[data writeToFile:initFilePath atomically:YES]; //dump the data that we just snagged out to the disk
	}
	return data;
}


//-(void) makeMsg:(NSString *)path ImageFileNameWithType:(NSString *)imageFileNameWithType ParameterBag:(NSMutableDictionary *)parameterBag
//{	
////	self.path = [NSString stringWithFormat:@"http://weather.yahooapis.com/forecastrss?w=%@&u=f", mainexSys.findMe.woeid];
////	Msg *msg = [[Msg alloc] init];
//	return [Msg init:imageFileNameWithType State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
//}


-(UIImage *)getVendorImage:(NSString *)vCode VendorType:(NSString *)vType
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
	NSString *imageFileName = [NSString stringWithFormat:@"%@.gif", vCode];
	NSData *imageData = [self fetchFromDisk:imageFileName FolderType:vType];

	//i don't know why, but the data is not actually found in the cache, but it is returning a size of 42
	if (imageData == nil || ([imageData length] < 50))
	{//not cached...	
		NSString *xURL = [NSString stringWithFormat:@"%@/Images/trav/%@/%@", [ExSystem sharedInstance].entitySettings.uriNonSSL, vType, imageFileName]; //mobile/
		//NSLog(@"imageURL=%@", xURL);
		imageData = [self fetchFromURL:imageFileName FetchURL:xURL VendorType:vType];
//		imageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:xURL]];
//		if (imageData != nil)
//		{
//			NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
//			NSString *documentsDirectory = [paths objectAtIndex:0];
//			NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"[%@]%@",vType, imageFileName]];
//			[imageData writeToFile:initFilePath atomically:YES]; //dump the data that we just snagged out to the disk
//		}
	}
	image = [[UIImage alloc] initWithData:imageData];	
	return image;
}




-(UIImage *)getVendorImageAsynch:(NSString *)vCode VendorType:(NSString *)vType RespondToCell:(UITableViewCell *)cell
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
	NSString *imageFileName = [NSString stringWithFormat:@"%@.gif", vCode];
	NSString *xURL = [NSString stringWithFormat:@"%@/Images/trav/%@/%@", [ExSystem sharedInstance].entitySettings.uriNonSSL, vType, imageFileName];
	//NSLog(@"xURL=%@", xURL);
	NSString *imageFileNameWithType = [NSString stringWithFormat:@"[%@]%@.gif",vType, vCode];
	Msg *msg = [[Msg alloc] init];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:imageFileNameWithType, VENDOR_IMAGE, nil ];
	
	NSData *imageData = [self fetchFromDiskPure:imageFileNameWithType];
	if (imageData == nil || ([imageData length] < 50))
	{
		//[msg init:VENDOR_IMAGE State:@"" Position:nil MessageData:nil URI:xURL MessageResponder:self ParameterBag:pBag];
		msg.idKey = VENDOR_IMAGE;
		msg.uri = xURL;
		msg.parameterBag = pBag;
		[msg setCell:cell];
		[exSys.msgControl add:msg];
		imageData = nil;
	}
	
	if (imageData != nil || ([imageData length] > 50))
	{
		image = [[UIImage alloc] initWithData:imageData];	
		return image;
	}
	else 
	{
		return nil;
	}
}

// Restart vendor image cache
-(void) startVendorImageCache
{
    self.vendorImageDict = [[NSMutableDictionary alloc] init];
}

-(void) stopVendorImageCache
{
    self.vendorImageDict = nil;
}

-(UIImage *)getVendorImageAsynchForImageView:(NSString *)vCode VendorType:(NSString *)vType RespondToIV:(UIImageView *)iv cellId:(NSString*)cellId
{
	NSString *imageFileName = [NSString stringWithFormat:@"%@.gif", vCode];
	NSString *xURL = [NSString stringWithFormat:@"%@/Images/trav/%@/%@", [ExSystem sharedInstance].entitySettings.uriNonSSL, vType, imageFileName];
    
	NSString *imageFileNameWithType = [NSString stringWithFormat:@"[%@]%@.gif",vType, vCode];
    
	Msg *msg = [[Msg alloc] init];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:imageFileNameWithType, VENDOR_IMAGE, vType, @"VendorType", vCode, @"VCode", iv, @"IMAGE_VIEW", nil ];
	
	NSData *imageData = [self fetchFromDiskPure:imageFileNameWithType];
	if (imageData == nil || ([imageData length] < 50))
	{
        // Check dict before do a fetch
        NSString* vendorImageKey = [NSString stringWithFormat:@"%@_%@", imageFileNameWithType, cellId];
        if (vendorImageDict == nil || vendorImageDict[vendorImageKey] == nil)
        {
            msg.idKey = VENDOR_IMAGE;
            msg.uri = xURL;
            msg.parameterBag = pBag;
            msg.skipCache = YES;
            //[msg setCell:cell];
            [exSys.msgControl add:msg];
            // MOB-8907 Instead of repeating fetch, just update the destination UIImageView
            vendorImageDict[vendorImageKey] = pBag;
        }
        else
        {
            // MOB-8907 Instead of repeating fetch, just update the destination UIImageView
            NSMutableDictionary* oldPBag = vendorImageDict[vendorImageKey];
            if (iv != nil)
            {
                oldPBag[@"IMAGE_VIEW"] = iv;
//                NSLog(@"Update imageview passed for %@", vendorImageKey);
            }
            else
            {
//                NSLog(@"Nil imageview passed for %@", vendorImageKey);
            }
        }
		imageData = nil;
	}
	
	if (imageData != nil || ([imageData length] > 50))
	{
    //    NSLog(@"image found for %@", cellId);
		image = [[UIImage alloc] initWithData:imageData];	
		return image;
	}
	else 
	{
		return nil;
	}
    
}

//let's go and get a vendor image, but tie it to an image view...
-(UIImage *)getVendorImageAsynchForImageView:(NSString *)vCode VendorType:(NSString *)vType RespondToIV:(UIImageView *)iv
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
	NSString *imageFileName = [NSString stringWithFormat:@"%@.gif", vCode];
	NSString *xURL = [NSString stringWithFormat:@"%@/Images/trav/%@/%@", [ExSystem sharedInstance].entitySettings.uriNonSSL, vType, imageFileName];

	NSString *imageFileNameWithType = [NSString stringWithFormat:@"[%@]%@.gif",vType, vCode];
	Msg *msg = [[Msg alloc] init];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:imageFileNameWithType, VENDOR_IMAGE, vType, @"VendorType", vCode, @"VCode", iv, @"IMAGE_VIEW", nil ];
	
	NSData *imageData = [self fetchFromDiskPure:imageFileNameWithType];
	if (imageData == nil || ([imageData length] < 50))
	{

		msg.idKey = VENDOR_IMAGE;
		msg.uri = xURL;
		msg.parameterBag = pBag;
		msg.skipCache = YES;
		//[msg setCell:cell];
		[exSys.msgControl add:msg];
		imageData = nil;
	}
	
	if (imageData != nil || ([imageData length] > 50))
	{
		image = [[UIImage alloc] initWithData:imageData];	
		return image;
	}
	else 
	{
		return nil;
	}
}


//let's go and get a vendor image, but tie it to an image view...
-(void)getImageAsynchForImage:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)iv
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image

	Msg *msg = [[Msg alloc] init];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:img, @"UIIMAGE", iv, @"IMAGE_VIEW", url, @"IMAGE_NAME", nil ];
	
//	NSData *imageData = [self fetchFromDiskPure:imageFileNameWithType];
//	if (imageData == nil || ([imageData length] < 50))
//	{
		msg.idKey = IMAGE;
		msg.uri = url;
	msg.skipCache = YES;
		msg.parameterBag = pBag;
		[exSys.msgControl add:msg];
//	}
	
//	if (imageData != nil || ([imageData length] > 50))
//	{
//		image = [[UIImage alloc] initWithData:imageData];	
//		return [image autorelease];
//	}
//	else 
//	{
//		return nil;
//	}
}


-(BOOL)fetchCachedImage:(NSString *)cacheName RespondToImage:(UIImage *)img IV:(UIImageView *)iv
{

	//checks to see if there is cached data for this msg
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:cacheName];

	//NSLog(@"initFilePath = %@", initFilePath);
	
	NSData *data = [NSData dataWithContentsOfFile:initFilePath]; //on the device, no cached data is found!
	if (data != nil && [data length] > 49)
	{
		UIImage *gotImg = [[UIImage alloc] initWithData:data];
		
		[iv setImage:gotImg];
		return YES;
	}
	
	return NO;
}


//let's go and get a vendor image, but tie it to an image view...
-(void)getImageAsynchForImageMVC:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)imageView MVC:(MobileViewController *)mvc
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
    
    if (![url length])
    {
        return;
    }
    
	//NSLog(@"url = %@" , url);
	//http://www.vfmii.com/medlib2/marriott_website_still/0/30/11/30011686/shvcy_phototour01.jpg
	NSString *imageCacheName = nil; //[[NSMutableString alloc] init];
	//[imageCacheName appendString:url];
	imageCacheName = [url stringByReplacingOccurrencesOfString:@"http://www.vfmii.com" withString:@""];
	imageCacheName = [imageCacheName stringByReplacingOccurrencesOfString:@"/" withString:@""];
	//imageCacheName = [imageCacheName stringByReplacingOccurrencesOfString:@"http://www.vfmii.com" withString:@""];
	//NSLog(@"imageCacheName = %@", imageCacheName);
	
    [self getImageAsynchForImageMVC:url RespondToImage:img IV:imageView MVC:mvc ImageCacheName:imageCacheName OAuth2AccessToken:nil];
}


//let's go and get a vendor image, but tie it to an image view...
-(void)getImageAsynchForImageMVC:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)iv MVC:(MobileViewController *)mvc ImageCacheName:(NSString*)imageCacheName OAuth2AccessToken:(NSString*)oauth2AccessToken
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
    
    if (![url length]) 
    {
        return;
    }
    
	if([self fetchCachedImage:imageCacheName RespondToImage:img IV:iv] == YES)
		return;
	
    Msg *msg = [[Msg alloc] init];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] init]; 
	if (img != nil)
		pBag[@"UIIMAGE"] = img;
	if (iv != nil)
		pBag[@"IMAGE_VIEW"] = iv;
	if (url != nil)
		pBag[@"IMAGE_NAME"] = url;
	if (imageCacheName != nil)
		pBag[@"IMAGE_CACHE_NAME"] = imageCacheName;
	
	msg.idKey = IMAGE;
	msg.uri = url;
	msg.skipCache = NO;
	msg.parameterBag = pBag;
    msg.oauth2AccessToken = oauth2AccessToken;
	[exSys.msgControl add:msg];
    
	
}

//let's go and get a vendor image, but tie it to an image view...
-(UIImage *)getVendorImageAsynchForImage:(NSString *)vCode VendorType:(NSString *)vType RespondToImage:(UIImage *)img
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
	NSString *imageFileName = [NSString stringWithFormat:@"%@.gif", vCode];
	NSString *xURL = [NSString stringWithFormat:@"%@/Images/trav/%@/%@", [ExSystem sharedInstance].entitySettings.uriNonSSL, vType, imageFileName];
	
	NSString *imageFileNameWithType = [NSString stringWithFormat:@"[%@]%@.gif",vType, vCode];
	Msg *msg = [[Msg alloc] init];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:imageFileNameWithType, VENDOR_IMAGE, img, @"UIIMAGE", nil ];
	
	NSData *imageData = [self fetchFromDiskPure:imageFileNameWithType];
	if (imageData == nil || ([imageData length] < 50))
	{
		
		msg.idKey = VENDOR_IMAGE;
		msg.uri = xURL;
		msg.parameterBag = pBag;
		//[msg setCell:cell];
		[exSys.msgControl add:msg];
		imageData = nil;
	}
	
	if (imageData != nil || ([imageData length] > 50))
	{
		image = [[UIImage alloc] initWithData:imageData];	
		return image;
	}
	else 
	{
		return nil;
	}
}


-(UIImage *)getCarImageAsynch:(NSString *)vCode CountryCode:(NSString *)countryCode ClassOfCar:(NSString *)classOfCar BodyType:(NSString *)bodyType RespondToCell:(UITableViewCell *)cell FetchURI:(NSString *)fetchURI
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
	NSString *imageFileName = [NSString stringWithFormat:@"%@%@%@%@999.jpg", vCode, countryCode, classOfCar, bodyType];
	//NSString *xURL = [NSString stringWithFormat:@"%@/Images/trav/CarTypes/%@", exSys.settings.uriNonSSL, imageFileName];
	NSString *xURL = [NSString stringWithFormat:@"%@%@", [ExSystem sharedInstance].entitySettings.uriNonSSL, fetchURI];
	//NSLog(@"xURL=%@", xURL);
	//NSLog(@"fetchURI=%@", fetchURI);
	//NSString *imageFileNameWithType = [NSString stringWithFormat:@"[%@]%@.gif",vType, vCode];
	Msg *msg = [Msg alloc];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:imageFileName, @"IMAGE_NAME", @"INFO", @"GOES_TO", vCode, @"VendorCode", nil ];
	
	NSData *imageData = [self fetchFromDiskPure:imageFileName];
	if (imageData == nil || ([imageData length] < 50))
	{
		//[msg init:IMAGE State:@"" Position:nil MessageData:nil URI:xURL MessageResponder:self ParameterBag:pBag];
		msg.idKey = IMAGE;
		msg.uri = xURL;
		msg.parameterBag = pBag;
		[msg setCell:cell];
		msg.onlyCached = @"NO";

		msg.data = nil;
		msg.responder = nil;
		msg.method = @"GET";
		msg.skipCache = NO;
		[exSys.msgControl add:msg];
		imageData = nil;
	}
	
	if (imageData != nil || ([imageData length] > 50))
	{
		image = [[UIImage alloc] initWithData:imageData];	
		return image;
	}
	else 
	{
		return nil;
	}
}


-(UIImage *)getCarImage:(NSString *)vCode
{
	imageURL = [NSString stringWithFormat:@"%@/Images/trav/mobile/CarTypes/%@.png", [ExSystem sharedInstance].entitySettings.uriNonSSL, vCode];
	//NSLog(@"imageURL=%@", imageURL);
	image = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:imageURL]]];	
	return image;
}

-(UIImage *)getCarImageSynch:(NSString *)vCode CountryCode:(NSString *)countryCode ClassOfCar:(NSString *)classOfCar BodyType:(NSString *)bodyType FetchURI:(NSString *)fetchURI
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
	//NSString *imageFileName = [NSString stringWithFormat:@"%@%@%@%@999.jpg", vCode, countryCode, classOfCar, bodyType];
	//NSString *xURL = [NSString stringWithFormat:@"%@/Images/trav/CarTypes/%@", exSys.settings.uriNonSSL, imageFileName];
	NSString *xURL = [NSString stringWithFormat:@"%@%@", [ExSystem sharedInstance].entitySettings.uri, fetchURI];
	image = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:xURL]]];	
	return image;
}


#pragma mark - methods for fusion only
//let's go and get a vendor image, but tie it to an image view...
-(void)getImageAsynchWithUrl:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)imageView
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
    
    if (![url length])
    {
        return;
    }
    
	//NSLog(@"url = %@" , url);
	//http://www.vfmii.com/medlib2/marriott_website_still/0/30/11/30011686/shvcy_phototour01.jpg
	NSString *imageCacheName = nil; //[[NSMutableString alloc] init];
	//[imageCacheName appendString:url];
	imageCacheName = [url stringByReplacingOccurrencesOfString:@"http://www.vfmii.com" withString:@""];
	imageCacheName = [imageCacheName stringByReplacingOccurrencesOfString:@"/" withString:@""];
	//imageCacheName = [imageCacheName stringByReplacingOccurrencesOfString:@"http://www.vfmii.com" withString:@""];
	//NSLog(@"imageCacheName = %@", imageCacheName);
	
    [self getImageAsynchWithUrl:url RespondToImage:img IV:imageView ImageCacheName:imageCacheName OAuth2AccessToken:nil];
}


//let's go and get a vendor image, but tie it to an image view...
-(void)getImageAsynchWithUrl:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)iv ImageCacheName:(NSString*)imageCacheName OAuth2AccessToken:(NSString*)oauth2AccessToken
{//gets the vendor image from the vendor image server, but first checks to see if we have cached this image
    
    if (![url length])
    {
        return;
    }
    
	if([self fetchCachedImage:imageCacheName RespondToImage:img IV:iv] == YES)
		return;
	
    Msg *msg = [[Msg alloc] init];
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] init];
	if (img != nil)
		pBag[@"UIIMAGE"] = img;
	if (iv != nil)
		pBag[@"IMAGE_VIEW"] = iv;
	if (url != nil)
		pBag[@"IMAGE_NAME"] = url;
	if (imageCacheName != nil)
		pBag[@"IMAGE_CACHE_NAME"] = imageCacheName;
	
	msg.idKey = IMAGE;
	msg.uri = url;
	msg.skipCache = NO;
	msg.parameterBag = pBag;
    msg.oauth2AccessToken = oauth2AccessToken;
	[exSys.msgControl add:msg];
    
	
}



@end
