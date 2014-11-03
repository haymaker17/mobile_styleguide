//
//  MyCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/10/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class ExpenseListViewController;

@interface MyCell : UITableViewCell {
	UILabel *label;
	UILabel *labelTotal;
	UILabel *labelStatus;
	UIButton *btnDetail;
	UIButton *btnDrill;
	ExpenseListViewController *rootVC;
	NSUInteger *currentRow;

}

@property (nonatomic, retain) IBOutlet UILabel *label;
@property (nonatomic, retain) IBOutlet UILabel *labelTotal;
@property (nonatomic, retain) IBOutlet UILabel *labelStatus;
@property (nonatomic, retain) IBOutlet UIButton *btnDetail;
@property (nonatomic, retain) IBOutlet UIButton *btnDrill;
@property (nonatomic, retain) ExpenseListViewController *rootVC;
@property (nonatomic) NSUInteger *currentRow;

- (IBAction)buttonDetailPressed:(id)sender;
- (IBAction)buttonDrillPressed:(id)sender;

@end
