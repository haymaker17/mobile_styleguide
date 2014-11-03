//
//  RecentRoutesSource.m
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "RecentRoutesSource.h"
#import "RouteManager.h"
#import "TableUtils.h"

@implementation RecentRoutesSource

- (NSString *)emptyMessage {
    return [Localizer getLocalizedText:@"no_recent_routes"];
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    NSArray *recentSearches = [[RouteManager sharedInstance] fetchRecentSearchRoutes];
    
    Route *route = [recentSearches objectAtIndex:[indexPath row]];

    UITableViewCell *cell = [TableUtils disclosureCellWithLabel:route.synopsis
                                                      andDetail:route.metadata
                                                       forTable:tableView];
    
    return cell;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    NSArray *recentSearches = [[RouteManager sharedInstance] fetchRecentSearchRoutes];
    
    return [recentSearches count];
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    NSArray *recentSearches = [[RouteManager sharedInstance] fetchRecentSearchRoutes];
    
    Route *route = [recentSearches objectAtIndex:[indexPath row]];
    
    route.date = [NSDate date];
    
    if ([self.delegate respondsToSelector:@selector(routeSource:didSelectRoute:)]) {
        [self.delegate routeSource:self didSelectRoute:route];
    }
}

@end
