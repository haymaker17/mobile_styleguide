//
//  CTEFreeTextQueryAPI.h
//  ConcurSDK
//
//  Created by Pavan Adavi on 4/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

@import Foundation;
#import "CTEError.h"

@interface CTEFreeTextQueryAPI : NSObject


/**
 Initialize the object with current context
 @param cuurentContext
        sets the context for the query f or h see @http://www.evature.com/docs/request.html#context for details
 @param currentScope
        sets the scope parameter for current query see @http://www.evature.com/docs/request.html#context for details
*/

- (instancetype)initWithCurrentContext:(NSString *)currentContext currentScope:(NSString *)currentScope;

/**
 Send the text query to Evature and get the resulting travel text in JSON response
 @param textQuery 
        Input text based search query for evature
 @param success
        Success callback block.  This block is called on successful login. Includes the Evature response JSON.
 @param failure
        Failure callback block.  This block is called on login failure with a @c CTEError @c.

 */
- (void)requestJSONForQuery:(NSString *)textQuery
                              success:(void (^)(NSDictionary *jsonResponse))success
                              failure:(void (^)(CTEError *error))failure;

/**
 * Resets the session with Evature so the query is considered a new query or considered a continuation of previous search by default
 */
-(void)restartSession ;

/**
 Sets the location latitude and longitude that will be used to determine user location.
 @param latitude
        string value of latitude
 @param longitude 
        string value of longitude
 */
-(void)setLocationLatitude:(NSString *)latitude longitude:(NSString *)longitude;

/**
Set the ipaddress that will be used to determine user location in abscense of latitude and longitude
 */
-(void)setDeviceIpAddress:(NSString *)ipAddress;

@end
