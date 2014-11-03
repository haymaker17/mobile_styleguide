//
//  HotelLocationAddressHandler.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HotelLocationHandler.h"

@class LocationResult;

@interface HotelLocationAddressHandler : HotelLocationHandler
{
	NSMutableDictionary		*cache;
	NSMutableArray			*cacheHistory;
	NSMutableArray			*locationResults;
}

@property (strong, nonatomic) NSMutableDictionary	*cache;
@property (strong, nonatomic) NSMutableArray		*cacheHistory;
@property (strong, nonatomic) NSMutableArray		*locationResults;

-(void)updateCacheWithResults:(NSMutableArray*)results forKey:(NSString*)key;
-(NSMutableArray*)cachedResultsForKey:(NSString*)key;

@end
