//
//  CTENetworking.h
//  ConcurSDK
//
//  Created by ernest cho on 2/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "CTEError.h"

/**
 *   Simplifies calls to Concur endpoints.
 *
 *   Uses AFNetworking 2.0 with the following modifications:
 *   Added XML serializers
 *   Added Concur session header
 *   Added Network logging
 *
 *   You may need to create more serializers based on the Content-type of the request/response.
 */
@interface CTENetworking : NSObject

/**
 *  Default init.
 *
 *  Sets the base url to the server url in the CTENetworkSettings.
 *  The base url can be overriden by providing a fully qualified url!  Use this feature if you need to call 3rd party websites.
 *
 *  Examples:
 *
 *  server url from CTENetworkSettings = @"https://www.concursolutions.com"
 *  service url = @"/mobile/getReceipt"
 *  url used in request = @"https://www.concursolutions.com/mobile/getReceipt"
 *
 *  server url from CTENetworkSettings = @"https://www.concursolutions.com"
 *  service url = @"https://www.google.com/#q=hello"
 *  url used in request = @"https://www.google.com/#q=hello"
 *
 *  @return CTENetworking
 */
- (id)init;

/**
 *  Get an XML from a Concur web service
 *
 *  @param serviceURL service url.  The base server url is retrieved from the network settings.
 *  @param success    success callback block.
 *  @param failure    failure callback block.
 */
- (void)getXMLFromURL:(NSString *)serviceURL success:(void (^)(NSString *responseObject))success failure:(void (^)(CTEError *error))failure;

/**
 *  Post an XML to a Concur web service
 *
 *  @param serviceURL service url.  The base url is retrieved from the network settings.
 *  @param requestXML request xml.
 *  @param success    success callback block.  Returns an response xml as an NSString
 *  @param failure    failure callback block.  Returns a CTEError
 */
- (void)postXMLToURL:(NSString *)serviceURL requestXML:(NSString *)requestXML success:(void (^)(NSString *responseObject))success failure:(void (^)(CTEError *error))failure;

/**
 *  Get a JSON from a Concur web service
 *
 *  @param serviceURL service url.  The base url is retrieved from the network settings.
 *  @param success    success callback block.  Returns an NSDictionary
 *  @param failure    failure callback block.  Returns a CTEError
 */
- (void)getJSONFromURL:(NSString *)serviceURL success:(void (^)(NSDictionary *responseObject))success failure:(void (^)(CTEError *error))failure;

/**
 *  Post a JSON to a Concur web service
 *
 *  @param serviceURL  service url.  The base url is retrieved from the network settings.
 *  @param requestJSON request json
 *  @param success     success callback block.  Returns an NSDictionary
 *  @param failure     failure callback block.  Returns a CTEError
 */
- (void)postJSONToURL:(NSString *)serviceURL requestJSON:(NSDictionary *)requestJSON success:(void (^)(NSDictionary *responseObject))success failure:(void (^)(CTEError *error))failure;

/**
 *  Get an Image from a Concur web service
 *
 *  @param serviceURL service url.  The base url is retrieved from the network settings.
 *  @param success    success callback block.  Returns an UIImage
 *  @param failure    failure callback block.  Returns a CTEError
 */
- (void)getImageFromURL:(NSString *)serviceURL success:(void (^)(UIImage *image))success failure:(void (^)(CTEError *error))failure;

/**
 *  Utility method that encodes a string for the Concur server
 *
 *  This is a non-standard encode, but it's what we use on Concur servers.  Kindof weird.
 *
 *  @param string to encode
 *  @return encoded string
 */
+ (NSString *)encodeStringForServer:(NSString *)string;

@end
