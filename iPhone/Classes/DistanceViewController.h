//
//  DistanceViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/19/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"

@interface DistanceViewController : BookingBaseTableViewController <UITableViewDelegate, UITableViewDataSource>
{
	NSArray					*values;
	NSNumber				*distanceValue;
	NSNumber				*isDistanceMetric;
	UIBarButtonItem			*unitsButton;
}

@property (nonatomic, strong) NSArray				*values;
@property (nonatomic, strong) NSNumber				*distanceValue;
@property (nonatomic, strong) NSNumber				*isDistanceMetric;
@property (nonatomic, strong) UIBarButtonItem		*unitsButton;

-(IBAction)btnUnits:(id)sender;
-(NSString*)getUnitsButtonTitle;
-(void)closeView;

@end
