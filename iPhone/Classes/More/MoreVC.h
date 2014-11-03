//
//  MoreVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EditField.h"
#import "HomePageCell.h"

@class MobileAlertView;

@interface MoreVC : MobileViewController<UITableViewDataSource, UITableViewDelegate>
{
    UITableView     *tableList;
    NSMutableArray  *aRows;
    MobileAlertView *unlinkFromTripItAlertView;
}


@property(nonatomic, strong) IBOutlet UITableView   *tableList;
@property (strong, nonatomic)  NSMutableArray       *aRows;
@property (strong, nonatomic)  MobileAlertView      *unlinkFromTripItAlertView;


-(void) makeRows;

#pragma mark - Yodlee
-(void)showAddCards:(id)sender;

#pragma mark - TripIt
- (void)didPressTripIt;

#pragma mark - Settings
-(void)showSettings:(id)sender;

#pragma mark Button Methods
-(IBAction)cancelSettings:(id)sender;


#pragma mark - Abukai
-(void) showAbukai: (id)sender;
@end
