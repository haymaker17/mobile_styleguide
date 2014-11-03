//
//  IgniteUserPickerVC.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "IgniteUserPickerDelegate.h"

@interface IgniteUserPickerVC : MobileViewController <UITableViewDataSource, UITableViewDelegate>
{
    UITableView     *tableList;
    NSString        *searchString;
    NSArray         *searchResults;
    NSArray         *emptyResultSet;

    id<IgniteUserPickerDelegate>  __weak _delegate;
}

@property (nonatomic, strong) IBOutlet UITableView  *tableList;
@property (nonatomic, copy)   NSString              *searchString;
@property (nonatomic, strong) NSArray               *searchResults;
@property (nonatomic, strong) NSArray               *emptyResultSet;

@property (nonatomic, weak) id<IgniteUserPickerDelegate> delegate;

- (void) searchForString:(NSString*)strSearch;

@end
