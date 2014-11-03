//
//  EntryListViewController.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/12/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
@class RootViewController;

@interface EntryListViewController : MobileViewController  <UITableViewDelegate, UITableViewDataSource>{
	NSMutableArray *listData;
	NSMutableArray *listOGData;
	NSArray *listDict;
	UITableView *tableView;
	UINavigationBar *navBar;
	//RootViewController *rootViewController;
}

@property (nonatomic, retain) NSMutableArray *listData;
@property (nonatomic, retain) NSMutableArray *listOGData;
@property (nonatomic, retain) NSArray *listDict;
@property (nonatomic, retain) IBOutlet UITableView *tableView;
@property (nonatomic, retain) IBOutlet UINavigationBar *navBar;
//@property (retain, nonatomic) RootViewController *rootViewController;

-(IBAction)expandCell:(id)sender detailType:(NSString *)detail rowNumber:(NSUInteger *)rowNum;
-(IBAction)switchViews:(id)sender;

@end
