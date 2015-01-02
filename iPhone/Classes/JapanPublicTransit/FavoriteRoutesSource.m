//
//  FavoriteRoutesSource.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "FavoriteRoutesSource.h"
#import "RouteExpenseManager.h"
#import "TableUtils.h"

@implementation FavoriteRoutesSource

- (NSString *)emptyMessage {
    return [Localizer getLocalizedText:@"no_favorite_routes"];
}

#pragma mark - UITableViewDataSource

// Need this for swipe-to-delete.
//
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        [[RouteExpenseManager sharedInstance] deleteFavoriteRouteExpenseAtIndex:[indexPath row]];
        
        [tableView beginUpdates];
        
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        
        [tableView endUpdates];
        
        NSArray *favoriteRouteExpenses = [[RouteExpenseManager sharedInstance] fetchFavoriteRouteExpenses];
        
        if ([favoriteRouteExpenses count] == 0) {
            if ([self.delegate respondsToSelector:@selector(didDeleteLastItemForRouteSource:)]) {
                [self.delegate didDeleteLastItemForRouteSource:self];
            }
        }
    }
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSArray *favoriteRouteExpenses = [[RouteExpenseManager sharedInstance] fetchFavoriteRouteExpenses];
    
    RouteExpense *routeExpense = [favoriteRouteExpenses objectAtIndex:[indexPath row]];
    
    UITableViewCell *cell = [TableUtils disclosureCellWithLabel:routeExpense.route.synopsis
                                                      andDetail:routeExpense.route.metadata
                                                       forTable:tableView];
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [[RouteExpenseManager sharedInstance] favoriteExpenseCount];
}

// Need this for row-moving.
//
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
    NSInteger fromRow = [fromIndexPath row];
    NSInteger toRow = [toIndexPath row];

    NSArray *favoriteRoutes = [[RouteExpenseManager sharedInstance] fetchFavoriteRouteExpenses];
    
    RouteExpense *routeExpense = [favoriteRoutes objectAtIndex:fromRow];

    [[RouteExpenseManager sharedInstance] deleteFavoriteRouteExpenseAtIndex:fromRow];
    [[RouteExpenseManager sharedInstance] insertFavoriteRouteExpense:routeExpense atIndex:toRow];
    
//    [[RouteManager sharedInstance] deleteFavoriteRouteAtIndex:fromRow];
//    [[RouteManager sharedInstance] insertRoute:route atIndex:toRow];
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    NSArray *favoriteRouteExpenses = [[RouteExpenseManager sharedInstance] fetchFavoriteRouteExpenses];
    
    RouteExpense *routeExpense = [favoriteRouteExpenses objectAtIndex:[indexPath row]];

    routeExpense.route.date = [NSDate date];
    
    if ([self.favoriteRouteSourceDelegate respondsToSelector:@selector(routeSource:didSelectSavedExpense:)]) {
        [self.favoriteRouteSourceDelegate routeSource:self didSelectSavedExpense:routeExpense];
    }
}

@end
