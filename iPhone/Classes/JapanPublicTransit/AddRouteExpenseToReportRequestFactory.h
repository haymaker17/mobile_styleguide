//
//  AddRouteExpenseToReportRequestFactory.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/10/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "CXRequest.h"
#import "RouteExpense.h"

@interface AddRouteExpenseToReportRequestFactory : NSObject

+ (CXRequest *)addRouteExpense:(RouteExpense *)routeExpense toReport:(NSString *)reportKey;

@end
