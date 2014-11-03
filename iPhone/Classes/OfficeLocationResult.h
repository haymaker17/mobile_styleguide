//
//  OfficeLocationResult.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/9/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "LocationResult.h"

@interface OfficeLocationResult : LocationResult
{
	NSString*	country;
    NSString*   streetAddress;
    
}

@property (nonatomic, strong) NSString* country;
@property (nonatomic, strong) NSString* streetAddress;

@end
