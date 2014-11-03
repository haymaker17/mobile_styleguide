//
//  SavedRoutesSource.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "RouteExpenseManager.h"
#import "SavedRoutesSource.h"
#import "TableUtils.h"

@implementation SavedRoutesSource

#pragma mark - UITableViewDataSource

- (NSString *)emptyMessage {
    return [Localizer getLocalizedText:@"no_saved_routes"];
}

#pragma mark - UITableViewDataSource

// Need this for swipe-to-delete.
//
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        //[self.savedExpenses removeObjectAtIndex:[indexPath row]];
        
        [[RouteExpenseManager sharedInstance] removeRouteExpenseAtIndex:[indexPath row]];
        
        [tableView beginUpdates];
        
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        
        [tableView endUpdates];
    
        NSArray *savedExpenses = [[RouteExpenseManager sharedInstance] fetchSavedExpenses];
        
        if ([savedExpenses count] == 0) {
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
    NSArray *savedExpenses = [[RouteExpenseManager sharedInstance] fetchSavedExpenses];
    
    RouteExpense *re = [savedExpenses objectAtIndex:[indexPath row]];
    
    UITableViewCell *cell = [TableUtils disclosureCellWithLabel:re.route.synopsis
                                                      andDetail:re.route.metadata
                                                       forTable:tableView];

    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [[RouteExpenseManager sharedInstance] routeExpenseCount];
}

// Need this for row-moving.
//
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
    int fromRow = [fromIndexPath row];
    int toRow = [toIndexPath row];
    
    NSArray *savedExpenses = [[RouteExpenseManager sharedInstance] fetchSavedExpenses];
    
    RouteExpense *routeExpense = [savedExpenses objectAtIndex:fromRow];
    
    [[RouteExpenseManager sharedInstance] removeRouteExpenseAtIndex:fromRow];
    [[RouteExpenseManager sharedInstance] insertRouteExpense:routeExpense atIndex:toRow];
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    NSArray *savedExpenses = [[RouteExpenseManager sharedInstance] fetchSavedExpenses];
    
    RouteExpense *re = [savedExpenses objectAtIndex:[indexPath row]];
    
    if ([self.savedRouteSourceDelegate respondsToSelector:@selector(routeSource:didSelectSavedExpense:)]) {
        [self.savedRouteSourceDelegate routeSource:self didSelectSavedExpense:re];
    }
}

@end
