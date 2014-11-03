//
//  AppsMenuVC.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "iPadHomeVC.h"

typedef enum appsType {
    TRAVEL_APPS = 0,
    BOOKING_APPS = 1
}AppsType;

@interface AppsMenuVC  : MobileViewController <UITableViewDelegate, UITableViewDataSource> {
}

@property (nonatomic,strong) 	IBOutlet UITableView		*appsTable;
@property (nonatomic,strong) 	NSMutableArray	*appsList;
@property (nonatomic,strong)    NSMutableArray  *bookingAppsList;
@property (nonatomic,weak)    iPadHomeVC		*iPadHome;

@property int displayAppsOfType;
@end
