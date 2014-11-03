//
//  DetailItemizationVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface DetailItemizationVC : UIViewController <UITableViewDelegate, UITableViewDataSource> {
	UILabel				*lblExpenseType, *lblAmount, *lblLine1, *lblLine2;
	UITableView			*tableList;
}

@property (nonatomic, retain) IBOutlet UILabel				*lblExpenseType;
@property (nonatomic, retain) IBOutlet UILabel				*lblAmount;
@property (nonatomic, retain) IBOutlet UILabel				*lblLine1;
@property (nonatomic, retain) IBOutlet UILabel				*lblLine2;
@property (nonatomic, retain) IBOutlet UITableView			*tableList;

@end
