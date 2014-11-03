//
//  HotelSearchTableViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 6/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AbstractDataSourceDelegate.h"
#import "ImageDownloaderOperation.h"
#import "HotelRoomsListTableViewController.h"
#import "OptionsSelectDelegate.h"

@interface HotelSearchTableViewController : UITableViewController <AbstractDataSourceDelegate, ImageDownloaderOperationDelegate, UIScrollViewDelegate, OptionsSelectDelegate>

@property (weak, nonatomic) IBOutlet UIView *ivSearchView;

@property BOOL isSearchHotelsNearMeEnabled;
+(void) showHotelsNearMe:(UINavigationController *)navi;

@end
