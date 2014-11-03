//
//  ExpensesListNaviViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/18/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "TripsData.h"
#import "DetailViewController.h"

@interface ExpensesListNaviViewController : MobileViewController <UITableViewDelegate, UITableViewDataSource>
{
	TripsData		*tripsData;
	NSMutableArray	*listKeys;
	UITableView		*tripTable;
	DetailViewController	*dvc;
}

@property (nonatomic, strong) TripsData			*tripsData;
@property (nonatomic, strong) NSMutableArray	*listKeys;
@property (nonatomic, strong) IBOutlet UITableView		*tripTable;
@property (nonatomic, strong) DetailViewController	*dvc;
@end
