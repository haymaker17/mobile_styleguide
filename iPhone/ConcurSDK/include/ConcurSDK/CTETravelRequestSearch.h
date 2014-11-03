//
//  CTETravelRequestSearch.h
//  ConcurSDK
//
//  Created by Kevin Boutin on 28/07/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//

#import <Foundation/Foundation.h>
@class CTETravelRequest;
@class CTEError;


@interface CTETravelRequestSearch : NSObject

/*
 * status used to filter the request
 */
- (id)initWithStatus:(NSString *)status;

/*
 * return the list of all Travels Request
 */
- (NSArray *)searchRequests;

/*
 * return object CTETravelRequest with full details
 */
- (CTETravelRequest *)searchRequestByID:(NSString *)requestID;

@end
