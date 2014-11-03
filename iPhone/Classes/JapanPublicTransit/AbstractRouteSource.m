//
//  AbstractRouteSource.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractRouteSource.h"

@implementation AbstractRouteSource

- (NSString *)emptyMessage {
    // Override this.
    
    return nil;
}

#pragma mark - UITableViewDataSource

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    // Override
    
    return nil;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Override
    
    return 0;
}

@end
