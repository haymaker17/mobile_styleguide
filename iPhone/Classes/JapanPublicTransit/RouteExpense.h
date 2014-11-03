//
//  RouteExpense.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Route.h"

@interface RouteExpense : NSObject <NSCoding, NSCopying>

@property (copy, nonatomic) NSString *uuid;
@property (strong, nonatomic) Route *route;
@property (copy, nonatomic) NSString *purpose;
@property (copy, nonatomic) NSString *comment;
@property (assign) BOOL isFavorite;
@property (assign) BOOL isPersonalExpense;

@end
