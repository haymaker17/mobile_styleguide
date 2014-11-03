//
//  RouteExpenseManager.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "RouteExpense.h"

@interface RouteExpenseManager : NSObject

@property (strong, nonatomic) NSMutableArray *favoriteRouteExpenses;
@property (strong, nonatomic) NSMutableArray *routeExpenses;

+ (RouteExpenseManager *)sharedInstance;

- (BOOL)deleteFavoriteRouteExpense:(Route *)route;
- (BOOL)deleteFavoriteRouteExpenseAtIndex:(NSInteger)index;
- (BOOL)deleteRouteExpense:(RouteExpense *)routeExpense;

- (NSInteger)favoriteExpenseCount;

- (NSArray *)fetchFavoriteRouteExpenses;
- (NSArray *)fetchSavedExpenses;

- (void)insertFavoriteRouteExpense:(RouteExpense *)routeExpense atIndex:(NSInteger)index;
- (void)insertRouteExpense:(RouteExpense *)routeExpense atIndex:(NSInteger)index;

- (BOOL)isRouteExpenseSavedAsFavorite:(RouteExpense *)routeExpense;
- (BOOL)isRouteExpenseSaved:(RouteExpense *)routeExpense;

- (NSInteger)routeExpenseCount;

- (void)removeRouteExpenseAtIndex:(NSInteger)index;

- (void)saveFavoriteRouteExpense:(RouteExpense *)routeExpense;
- (void)saveExpense:(RouteExpense *)routeExpense;

@end
