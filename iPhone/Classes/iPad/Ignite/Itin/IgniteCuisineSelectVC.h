//
//  IgniteCuisineSelectVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/28/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface IgniteCuisineSelectVC : MobileViewController
{
    UINavigationBar                                 *navBar;
    UITableView                                     *tableList;
    id<UITableViewDataSource, UITableViewDelegate>  _delegate;
}

@property (nonatomic, strong) IBOutlet UITableView      *tableList;
@property (nonatomic, strong) IBOutlet UINavigationBar  *navBar;

- (void)setSeedData:(id<UITableViewDataSource, UITableViewDelegate>) del;
@end
