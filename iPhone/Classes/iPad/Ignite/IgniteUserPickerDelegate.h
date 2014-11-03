//
//  IgniteUserPickerDelegate.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "IgniteUserSearchResult.h"

@protocol IgniteUserPickerDelegate <NSObject>
-(void) userPickedSearchResult:(IgniteUserSearchResult*)searchResult;
@end
