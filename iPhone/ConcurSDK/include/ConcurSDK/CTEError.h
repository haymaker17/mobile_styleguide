//
//  CTEError.h
//  ConcurSDK
//
//  Created by ernest cho on 2/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEErrorMessage.h"

/**
 Concur error extends NSError with some convenience methods. 

 For information about what all the foundation error codes mean see this documentation on NSError.
 http://nshipster.com/nserror/
 */
@interface CTEError : NSError

/**
 Some endpoints send back non-standard errors, store them just in case we want to pass them all the way back.
 
 This can be nil.
 */
@property (nonatomic, readonly, strong) NSString *concurErrorResponse;

/**
 Whenever possible, the SDK should process the error XML into CTEErrorMessages
 */
@property (nonatomic, readonly, strong) NSArray *concurErrorMessages;

/**
 Creates a Concur NSError with error information from the Concur server.

 @param concurErrorXML
        error XML from server
 */
- (id)initWithXML:(NSString *)concurErrorXML;

/**
 Creates a Concur NSError with error information from the Concur server.

 @param concurErrorJSON 
        error JSON from server
 */
- (id)initWithJSON:(NSDictionary *)concurErrorJSON;

/**
 Creates a Concur NSError with error information from the Concur server.

 @param concurErrorResponse
        non-standard error response
 @param errorMessage
        non-standard errors need to be parsed into a CTEErrorMessage
 */
- (id)initWithNonStandardErrorResponse:(NSString *)concurErrorResponse errorMessage:(CTEErrorMessage *)errorMessage;

/**
 YES if this is a network error.
 */
- (BOOL)isNetworkError;

/**
 HTTP status code.

 -1, if this is NOT a network error.
 */
- (NSInteger)httpStatusCode;

/**
 Rate-limit indicator
 
 YES, if Akamai have returned the rate-limiting error message
 */
- (BOOL)isRateLimitingOn;

/**
 Convenience method.
 
 This method tries to return the first CTEErrorMessage.userMessage
 If that fails, it returns a generic error message
 */
- (NSString *)simpleErrorMessage;

@end
