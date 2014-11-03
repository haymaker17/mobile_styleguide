//
//  RouteExpenseManager.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AnalyticsManager.h"
#import "RouteExpenseManager.h"

@implementation RouteExpenseManager

__strong static id _sharedInstance = nil;

+ (RouteExpenseManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.favoriteRouteExpenses = [self loadFavoriteRouteExpenses];
        self.routeExpenses = [self loadExpenses];
    }
    
    return self;
}

#pragma mark - Favorite route expenses

- (BOOL)deleteFavoriteRouteExpense:(Route *)route {
    BOOL routeExpenseDeleted = NO;
    
    for (int i = 0; i < [self.favoriteRouteExpenses count]; i++) {
        Route *r = [self.favoriteRouteExpenses objectAtIndex:i];
        
        if ([r.uuid isEqualToString:route.uuid]) {
            [self.favoriteRouteExpenses removeObjectAtIndex:i];
            routeExpenseDeleted = YES;
            break;
        }
    }
    
    if (routeExpenseDeleted) {
        [self saveFavoriteRouteExpenses:self.favoriteRouteExpenses];
    }
    
    return routeExpenseDeleted;
}

- (BOOL)deleteFavoriteRouteExpenseAtIndex:(NSInteger)index {
    BOOL routeDeleted = NO;
    
    if (index >= 0 && index < [self.favoriteRouteExpenses count]) {
        [self.favoriteRouteExpenses removeObjectAtIndex:index];
        
        [self saveFavoriteRouteExpenses:self.favoriteRouteExpenses];
        
        routeDeleted = YES;
    }
    
    return routeDeleted;
}

- (NSInteger)favoriteExpenseCount {
    return [self.favoriteRouteExpenses count];
}

- (NSArray *)fetchFavoriteRouteExpenses {
    return self.favoriteRouteExpenses;
}

- (BOOL)isRouteExpenseSavedAsFavorite:(RouteExpense *)route {
    BOOL isSaved = NO;
    
    for (Route *r in self.favoriteRouteExpenses) {
        if ([r.uuid isEqualToString:route.uuid]) {
            isSaved = YES;
            break;
        }
    }
    
    return isSaved;
}

- (NSMutableArray *)loadFavoriteRouteExpenses {
    NSMutableArray *favoriteRouteExpenses = [[NSMutableArray alloc] init];;
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSArray *serializedRoutes = [defaults arrayForKey:@"favorite_routes"];
    
    if ([serializedRoutes count] > 0) {
        for (NSData *data in serializedRoutes) {
            RouteExpense *routeExpense = [NSKeyedUnarchiver unarchiveObjectWithData:data];
            [favoriteRouteExpenses addObject:routeExpense];
        }
    }
    
    return favoriteRouteExpenses;
}

- (void)overwriteFavoriteRouteExpense:(RouteExpense *)routeExpense {
    for (int i = 0; i < [self.favoriteRouteExpenses count]; i++) {
        RouteExpense *re = [self.favoriteRouteExpenses objectAtIndex:i];
        
        if ([re.uuid isEqualToString:routeExpense.uuid]) {
            [self.favoriteRouteExpenses replaceObjectAtIndex:i withObject:routeExpense];
            break;
        }
    }
}

- (void)saveFavoriteRouteExpense:(RouteExpense *)routeExpense {
    [[AnalyticsManager sharedInstance] logCategory:@"JPT"
                                          withName:@"Add as Favorite"
                                     fromAncestors:@[@"Manual", @"Search"]];
    
    RouteExpense *routeExpenseCopy = [routeExpense copy];
    
    if ([self isRouteExpenseSavedAsFavorite:routeExpense]) {
        [self overwriteWithRouteExpense:routeExpenseCopy];
    } else {
        [self.favoriteRouteExpenses insertObject:routeExpenseCopy atIndex:0];
    }
    
    [self saveFavoriteRouteExpenses:self.favoriteRouteExpenses];
}

- (void)saveFavoriteRouteExpenses:(NSArray *)routeExpenses {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSMutableArray *mutableRoutes = [[NSMutableArray alloc] init];
    
    for (Route *route in routeExpenses) {
        NSData *data = [NSKeyedArchiver archivedDataWithRootObject:route];
        
        [mutableRoutes addObject:data];
    }
    
    [defaults setObject:mutableRoutes forKey:@"favorite_routes"];
    
    [defaults synchronize];
}

#pragma mark - Nominal route expenses

- (BOOL)deleteRouteExpense:(RouteExpense *)routeExpense {
    BOOL expenseDeleted = NO;
    
    for (int i = 0; i < [self.routeExpenses count]; i++) {
        RouteExpense *re = [self.routeExpenses objectAtIndex:i];
        
        if ([re.uuid isEqualToString:routeExpense.uuid]) {
            [self.routeExpenses removeObjectAtIndex:i];
            expenseDeleted = YES;
            break;
        }
    }
    
    if (expenseDeleted) {
        [self saveExpenses:self.routeExpenses];
    }
    
    return expenseDeleted;
}

- (NSArray *)fetchSavedExpenses {
    return self.routeExpenses;
}

- (void)insertFavoriteRouteExpense:(RouteExpense *)routeExpense atIndex:(NSInteger)index {
    [self.favoriteRouteExpenses insertObject:routeExpense atIndex:index];
    
    [self saveFavoriteRouteExpenses:self.favoriteRouteExpenses];
}

- (void)insertRouteExpense:(RouteExpense *)routeExpense atIndex:(NSInteger)index {
    [self.routeExpenses insertObject:routeExpense atIndex:index];
    
    [self saveExpenses:self.routeExpenses];
}

- (BOOL)isRouteExpenseSaved:(RouteExpense *)routeExpense {
    BOOL isSaved = NO;
    
    for (RouteExpense *re in self.routeExpenses) {
        if ([re.uuid isEqualToString:routeExpense.uuid]) {
            isSaved = YES;
            break;
        }
    }
    
    return isSaved;
}

- (NSInteger)routeExpenseCount {
    return [self.routeExpenses count];
}

- (NSMutableArray *)loadExpenses {
    NSMutableArray *routeExpenses = [[NSMutableArray alloc] init];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSArray *serializedRoutes = [defaults arrayForKey:@"saved_routes"];
    
    if ([serializedRoutes count] > 0) {
        for (NSData *data in serializedRoutes) {
            RouteExpense *re = [NSKeyedUnarchiver unarchiveObjectWithData:data];
            [routeExpenses addObject:re];
        }
    }
    
    return routeExpenses;
}

- (void)overwriteWithRouteExpense:(RouteExpense *)routeExpense {
    for (int i = 0; i < [self.routeExpenses count]; i++) {
        RouteExpense *re = [self.routeExpenses objectAtIndex:i];
        
        if ([re.uuid isEqualToString:routeExpense.uuid]) {
            [self.routeExpenses replaceObjectAtIndex:i withObject:routeExpense];
            break;
        }
    }
}

- (void)removeRouteExpenseAtIndex:(NSInteger)index {
    [self.routeExpenses removeObjectAtIndex:index];
    
    [self saveExpenses:self.routeExpenses];
}

- (void)saveExpense:(RouteExpense *)routeExpense {
    [[AnalyticsManager sharedInstance] logCategory:@"JPT"
                                          withName:@"Save for Later"
                                     fromAncestors:@[@"Manual", @"Search"]];
    
    if ([self isRouteExpenseSaved:routeExpense]) {
        [self overwriteWithRouteExpense:routeExpense];
    } else {
        [self.routeExpenses insertObject:routeExpense atIndex:0];
    }
    
    [self saveExpenses:self.routeExpenses];
}

- (void)saveExpenses:(NSArray *)routeExpenses {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSMutableArray *mutableSerializedRoutes = [[NSMutableArray alloc] init];
    
    for (RouteExpense *routeExpense in routeExpenses) {
        NSData *data = [NSKeyedArchiver archivedDataWithRootObject:routeExpense];
        
        [mutableSerializedRoutes addObject:data];
    }
    
    [defaults setObject:mutableSerializedRoutes forKey:@"saved_routes"];
    
    [defaults synchronize];
}

@end
