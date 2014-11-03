//
//  MockServer.m
//  ConcurMobile
//
//  Created by ernest cho on 9/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MockServer.h"
#import "URLMock.h"

// Had to create a mock server cause the hotel booking is broken. :/
@implementation MockServer

static id sharedInstance = nil;

+ (id)sharedInstance
{
    if (!sharedInstance) {
        sharedInstance = [[self alloc] init];
    }
    return sharedInstance;
}

- (id)init
{
    self = [super init];
    if (self)
    {
        [self enableMockServer];
    }
    return self;
}

- (void)enableMockServer
{
    [UMKMockURLProtocol enable];
}

- (void)disableMockServer
{
    [UMKMockURLProtocol disable];
}

- (void)addMockForHotelSearch
{
    NSURL *URL = [NSURL URLWithString:@"https://rqa3-cb.concursolutions.com/mobile/travel/v1.0/Hotels?latitude=47.610377&longitude=-122.200679&radius=5&distanceUnit=m&checkin=2014-9-2&checkout=2014-9-3&limit=30&offset=0"];
    NSDictionary *response = [self loadJSONFile:@"HotelSearchResponse"];

    [UMKMockURLProtocol expectMockHTTPGetRequestWithURL:URL responseStatusCode:200 responseJSON:response];
}

- (void)addMockForHotelRates
{
    NSURL *URL = [NSURL URLWithString:@"https://rqa3-cb.concursolutions.com/mobile/travel/v1.0/Hotels/773647a0-2bd1-479e-9b79-f5031ee17cd1/39409/Rates"];
    NSDictionary *response = [self loadJSONFile:@"HotelRatesResponse"];

    [UMKMockURLProtocol expectMockHTTPGetRequestWithURL:URL responseStatusCode:200 responseJSON:response];
}

/**
 *  Load file
 *
 *  @param filename file name
 *  @param filetype file type
 *
 *  @return NSData
 */
- (NSData *)loadFileAsData:(NSString *)filename fileType:(NSString *)filetype
{
    NSBundle *bundle = [NSBundle bundleForClass:[self class]];
    NSString *path = [bundle pathForResource:filename ofType:filetype];
    return [NSData dataWithContentsOfFile:path];
}

/**
 *  Loads file
 *
 *  @param filename file name
 *  @param filetype file type
 *
 *  @return NSString
 */
- (NSString *)loadFileAsString:(NSString *)filename fileType:(NSString *)filetype
{
    NSData *data = [self loadFileAsData:filename fileType:filetype];
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

/**
 *  Loads XML file from file
 *
 *  @param filename file name
 *
 *  @return NSString
 */
- (NSString *)loadXMLFile:(NSString *)filename
{
    return [self loadFileAsString:filename fileType:@"xml"];
}

/**
 *  Loads JSON file from file
 *
 *  @param filename file name
 *
 *  @return NSDictionary
 */
- (NSDictionary *)loadJSONFile:(NSString *)filename
{
    NSDictionary *result = @{};
    NSData *data = [self loadFileAsData:filename fileType:@"json"];

    if (NSClassFromString(@"NSJSONSerialization"))
    {
        NSError *error = nil;
        id object = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];

        if (error) {
            ALog(@"Failed to serialize JSON to a dictionary! %@", error);
        }

        if([object isKindOfClass:[NSDictionary class]]) {
            result = object;
        } else {
            ALog(@"JSON serialized, but it's not a dictionary! %@", object);
        }
    }
    return result;
}

/**
 *  Loads image from file
 *
 *  @param filename file name
 *  @param type     file type
 *
 *  @return UIImage
 */
- (UIImage *)loadImageFile:(NSString *)filename fileType:(NSString *)type
{
    NSBundle *bundle = [NSBundle bundleForClass:[self class]];
    NSString *path = [bundle pathForResource:filename ofType:type];

    // by default, imageWithData loads images unscaled while AFNetworking gets images scaled
    UIImage *unscaledImage = [UIImage imageWithData:[NSData dataWithContentsOfFile:path]];
    struct CGImage *imageData = [unscaledImage CGImage];

    // return the scaled version of the image
    return [[UIImage alloc] initWithCGImage:imageData scale:2.0 orientation:UIImageOrientationUp];
}


@end
