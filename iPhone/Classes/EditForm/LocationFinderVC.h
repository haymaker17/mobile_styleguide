//
//  LocationFinderVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/8/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface LocationFinderVC : UITableViewController <UISearchBarDelegate>

@property (strong, nonatomic) IBOutlet UISearchBar *searchBar;

@end
