//
//  ImageUtil.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ImageUtil.h"

#include <math.h>
static inline double radians (double degrees) {return degrees * M_PI/180;}

@implementation ImageUtil
#define kTHUMB_SIZE 75
#define kTHUMB_GAPPED (kTHUMB_SIZE + 4)

+ (NSData *)imageDataForImageWithURLString:(NSString *)urlString
{
	return [NSData dataWithContentsOfURL:[NSURL URLWithString:urlString]];
}

+(NSString*) saveReceiptImageToDocumentsFolder:(UIImage *)theImage ImageName:(NSString *)imageName 
{
	//kill existing images, if they exist
	[self killReceiptImagesFromDocumentsFolder:imageName];
	
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:imageName];
	//NSLog(@"Image Path: %@", initFilePath);
	NSData *imageData = UIImageJPEGRepresentation(theImage, 0.9f);
	[imageData writeToFile:initFilePath atomically:YES]; //dump the data that we just snagged out to the disk
	imageData = nil;
	
	return initFilePath;
}

+(NSString*) saveReceiptDataToDocumentsFolder:(NSData *)data ImageName:(NSString *)imageName 
{
	//kill existing images, if they exist
	[self killReceiptImagesFromDocumentsFolder:imageName];
	
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:imageName];
	//NSLog(@"Image Path: %@", initFilePath);
	
	[data writeToFile:initFilePath atomically:YES]; //dump the data that we just snagged out to the disk
	data = nil;
	
	return initFilePath;
}

+(void) killReceiptImagesFromDocumentsFolder:(NSString *)imageName
{
	if (imageName != nil)
	{
		
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:imageName];
		//NSLog(@"Deleting Image File: %@", initFilePath);
		
		NSFileManager *fileManager = [NSFileManager defaultManager];
		[fileManager removeItemAtPath:initFilePath error:NULL];
	}
}


//returns the data for an image file name
+(NSData *)getImageData:(NSString *) fileName
{
	if (fileName != nil)
	{
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:fileName];
		//NSLog(@"Load Image Path: %@", initFilePath);
		return[NSData dataWithContentsOfFile:initFilePath];
	}
    
    return nil;
}


+ (UIImage*)imageWithImage:(UIImage*)image scaledToSize:(CGSize)newSize;
{
	// Create a graphics image context
	UIGraphicsBeginImageContext(newSize);
	
	// Tell the old image to draw in this new context, with the desired
	// new size
	[image drawInRect:CGRectMake(0,0, newSize.width, newSize.height)];
	
	CGContextRef bitmap;
	bitmap = UIGraphicsGetCurrentContext();
	if (image.imageOrientation == UIImageOrientationLeft) {
		//NSLog(@"image orientation left");
		CGContextRotateCTM (bitmap, radians(90));
		CGContextTranslateCTM (bitmap, 0, -newSize.height);
		
	} else if (image.imageOrientation == UIImageOrientationRight) {
		//NSLog(@"image orientation right");
		CGContextRotateCTM (bitmap, radians(-90));
		CGContextTranslateCTM (bitmap, -newSize.width, 0);
		
	} else if (image.imageOrientation == UIImageOrientationUp) {
		//NSLog(@"image orientation up");	
		
	} else if (image.imageOrientation == UIImageOrientationDown) {
		//NSLog(@"image orientation down");	
		CGContextTranslateCTM (bitmap, newSize.width,newSize.height);
		CGContextRotateCTM (bitmap, radians(-180.));
		
	}
	
	// Get the new image from the context
	UIImage* newImage = UIGraphicsGetImageFromCurrentImageContext();
	
	// End the context
	UIGraphicsEndImageContext();
	
	// Return the new image.
	return newImage;
}


+(void) killImageFromDocumentsFolder:(NSString *)imageName 
{
	if (imageName != nil)
	{
		NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString *documentsDirectory = paths[0];
		NSString *initFilePath = [documentsDirectory stringByAppendingPathComponent:imageName];
		NSFileManager *fileManager = [NSFileManager defaultManager];
		[fileManager removeItemAtPath:initFilePath error:NULL];
	}	
}


+(void)saveEntryImageLocal:(OOPEntry *)entry UserId:(NSString *)userId
{
	NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
	NSString *documentsDirectory = paths[0];
	
	NSString *archivePath = [documentsDirectory stringByAppendingPathComponent:[NSString stringWithFormat:@"RECEIPT_%@_%@.png", entry.meKey, userId]];
	
	NSData *imageData = UIImageJPEGRepresentation(entry.receiptImage, 0.9f);
	[imageData writeToFile:archivePath atomically:YES];
}


+(UIImage *)loadEntryImageFromLocal:(OOPEntry *)entry UserId:(NSString *)userId
{
	__autoreleasing UIImage *img = [[UIImage alloc] initWithData:[ImageUtil getImageData:[NSString stringWithFormat:@"RECEIPT_%@_%@.png", entry.meKey, userId]]];
	return img;
}

+(UIImage*) getImageByName:(NSString*) imgName
{
    if (![imgName length])
        return nil;
    
    NSString* imgFullName = imgName;
    return [UIImage imageNamed:imgFullName];
}


// If image's orientation is not up, make a new image with the same data but force the orientation to be up
+ (UIImage *)fixOrientation :(UIImage*) img
{
    // No-op if the orientation is already correct
    if (img.imageOrientation == UIImageOrientationUp)
        return img;
    
    // We need to calculate the proper transformation to make the image upright.
    // We do it in 2 steps: Rotate if Left/Right/Down, and then flip if Mirrored.
    CGAffineTransform transform = CGAffineTransformIdentity;
    
    switch (img.imageOrientation) {
        case UIImageOrientationDown:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, img.size.width, img.size.height);
            transform = CGAffineTransformRotate(transform, M_PI);
            break;
            
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
            transform = CGAffineTransformTranslate(transform, img.size.width, 0);
            transform = CGAffineTransformRotate(transform, M_PI_2);
            break;
            
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, 0, img.size.height);
            transform = CGAffineTransformRotate(transform, -M_PI_2);
            break;
        case UIImageOrientationUp:
        case UIImageOrientationUpMirrored:
            break;
    }
    
    switch (img.imageOrientation) {
        case UIImageOrientationUpMirrored:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, img.size.width, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
            
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, img.size.height, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
        case UIImageOrientationUp:
        case UIImageOrientationDown:
        case UIImageOrientationLeft:
        case UIImageOrientationRight:
            break;
    }
    
    // Now we draw the underlying CGImage into a new context, applying the transform
    // calculated above.
    CGContextRef ctx = CGBitmapContextCreate(NULL, img.size.width, img.size.height,
                                             CGImageGetBitsPerComponent(img.CGImage), 0,
                                             CGImageGetColorSpace(img.CGImage),
                                             CGImageGetBitmapInfo(img.CGImage));
    CGContextConcatCTM(ctx, transform);
    switch (img.imageOrientation) {
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            // Grr...
            CGContextDrawImage(ctx, CGRectMake(0,0,img.size.height,img.size.width), img.CGImage);
            break;
            
        default:
            CGContextDrawImage(ctx, CGRectMake(0,0,img.size.width,img.size.height), img.CGImage);
            break;
    }
    
    // And now we just create a new UIImage from the drawing context
    CGImageRef cgimg = CGBitmapContextCreateImage(ctx);
    UIImage *newImg = [UIImage imageWithCGImage:cgimg];
    CGContextRelease(ctx);
    CGImageRelease(cgimg);
    return newImg;
}

@end
