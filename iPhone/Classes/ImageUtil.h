//
//  ImageUtil.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ImageUtil : NSObject

+(NSData *)imageDataForImageWithURLString:(NSString *)urlString;
+(void) killReceiptImagesFromDocumentsFolder:(NSString *)imageName;
+(NSData *)getImageData:(NSString *) fileName;
+ (UIImage*)imageWithImage:(UIImage*)image scaledToSize:(CGSize)newSize;
+(void) killImageFromDocumentsFolder:(NSString *)imageName;
//+(void)saveEntryImageLocal:(OOPEntry *)entry UserId:(NSString *)userId;
//+(UIImage *)loadEntryImageFromLocal:(OOPEntry *)entry UserId:(NSString *)userId;

+(NSString*) saveReceiptImageToDocumentsFolder:(UIImage *)theImage ImageName:(NSString *)imageName;
+(NSString*) saveReceiptDataToDocumentsFolder:(NSData *)data ImageName:(NSString *)imageName ;

+(UIImage*) getImageByName:(NSString*) imgName;
+ (UIImage *)fixOrientation :(UIImage*) img;

@end
