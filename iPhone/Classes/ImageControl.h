//
//  ImageControl.h
//  ConcurMobile
//
//  Created by Paul Kramer on 2/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class ExSystem;
#import "Msg.h"
#import "MobileViewController.h"

@interface ImageControl : MsgResponder 
{
	NSString				*imageURL, *imageName, *imageType, *imageDimension, *vendorCode, *isVendor, *vendorType, *isCliqImage;
	UIImage					*image;
	ExSystem				*exSys;
    
    NSMutableDictionary     *vendorImageDict; //vendor, iv (UIImageView) map
}

@property (nonatomic, strong) NSString *imageURL;
@property (nonatomic, strong) NSString *imageName;
@property (nonatomic, strong) NSString *imageType;
@property (nonatomic, strong) NSString *imageDimension;
@property (nonatomic, strong) NSString *vendorCode;
@property (nonatomic, strong) NSString *isVendor;
@property (nonatomic, strong) NSString *vendorType;
@property (nonatomic, strong) NSString *isCliqImage;
@property (nonatomic, strong) UIImage *image;
@property (nonatomic, strong) ExSystem				*exSys;
@property (nonatomic, strong) NSMutableDictionary   *vendorImageDict;

-(UIImage *)getVendorImage:(NSString *)vCode VendorType:(NSString *)vType;
-(UIImage *)getVendorImageAsynch:(NSString *)vCode VendorType:(NSString *)vType RespondToCell:(UITableViewCell *)cell;
-(UIImage *)getCarImage:(NSString *)vCode;
-(NSData *)fetchFromDisk:(NSString *)imageFileName FolderType:(NSString *)folderType;
//-(NSData *)fetchFromURL:(NSString *)imageFileName FetchURL:(NSString *)xURL VendorType:(NSString *)vType RespondToCell:(UITableViewCell *)cell;
-(NSData *)fetchFromURL:(NSString *)imageFileName FetchURL:(NSString *)xURL VendorType:(NSString *)vType;
-(NSData *)fetchFromDiskPure:(NSString *)imageFileName;
-(UIImage *)getCarImageAsynch:(NSString *)vCode CountryCode:(NSString *)countryCode ClassOfCar:(NSString *)classOfCar BodyType:(NSString *)bodyType RespondToCell:(UITableViewCell *)cell FetchURI:(NSString *)fetchURI;

-(UIImage *)getCarImageSynch:(NSString *)vCode CountryCode:(NSString *)countryCode ClassOfCar:(NSString *)classOfCar BodyType:(NSString *)bodyType FetchURI:(NSString *)fetchURI;

-(UIImage *)getVendorImageAsynchForImageView:(NSString *)vCode VendorType:(NSString *)vType RespondToIV:(UIImageView *)iv;

-(UIImage *)getVendorImageAsynchForImage:(NSString *)vCode VendorType:(NSString *)vType RespondToImage:(UIImage *)img;

-(void)getImageAsynchForImage:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)iv;

-(void)getImageAsynchForImageMVC:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)iv MVC:(MobileViewController *)mvc;

-(void)getImageAsynchForImageMVC:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)iv MVC:(MobileViewController *)mvc ImageCacheName:(NSString*)imageCacheName OAuth2AccessToken:(NSString*)oauth2AccessToken;

-(BOOL)fetchCachedImage:(NSString *)cacheName RespondToImage:(UIImage *)img IV:(UIImageView *)iv;

-(void) startVendorImageCache;
-(void) stopVendorImageCache;
-(UIImage *)getVendorImageAsynchForImageView:(NSString *)vCode VendorType:(NSString *)vType RespondToIV:(UIImageView *)iv cellId:(NSString*)cellId;

// for fusion only
-(void)getImageAsynchWithUrl:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)imageView;
-(void)getImageAsynchWithUrl:(NSString *)url RespondToImage:(UIImage *)img IV:(UIImageView *)iv ImageCacheName:(NSString*)imageCacheName OAuth2AccessToken:(NSString*)oauth2AccessToken;

@end
