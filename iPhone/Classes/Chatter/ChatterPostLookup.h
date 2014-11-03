//
//  ChatterPostLookup.h
//  ConcurMobile
//
//  Created by ernest cho on 6/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface ChatterPostLookup : NSObject
- (NSString *)getPostItemIdForTrip:(NSString *)recordLocator;
- (void)associateTrip:(NSString *)recordLocator withPost:(NSString *)itemId;
- (void)removeTrip:(NSString *)itemId;
@end
