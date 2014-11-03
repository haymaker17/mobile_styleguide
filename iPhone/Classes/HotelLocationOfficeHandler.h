//
//  HotelLocationOfficeHandler.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HotelLocationHandler.h"


@interface HotelLocationOfficeHandler : HotelLocationHandler
{
	NSArray			*officeLocations;
	NSMutableArray	*filteredOfficeLocations;
}

@property (nonatomic, strong) NSArray			*officeLocations;
@property (nonatomic, strong) NSMutableArray	*filteredOfficeLocations;

- (void)filterOfficeLocations:(NSString*)searchText;

@end
