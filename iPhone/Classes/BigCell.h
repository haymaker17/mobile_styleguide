//
//  BigCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/10/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class ExpenseListViewController;

@interface BigCell : UITableViewCell {
	UILabel *label;
	UILabel *labelTotal;
	UILabel *labelStatus;
	UILabel *labelReportDate;
	UILabel *labelPayStatus;
	UILabel *labelClaimed;
	UILabel *labelApproved;
	UITextView *purpose;
	UIButton *btnDetail;
	ExpenseListViewController *rootVC;
	NSUInteger *currentRow;
	
}

@property (nonatomic, retain) IBOutlet UILabel *label;
@property (nonatomic, retain) IBOutlet UILabel *labelTotal;
@property (nonatomic, retain) IBOutlet UILabel *labelStatus;
@property (nonatomic, retain) IBOutlet UILabel *labelReportDate;
@property (nonatomic, retain) IBOutlet UILabel *labelPayStatus;
@property (nonatomic, retain) IBOutlet UILabel *labelClaimed;
@property (nonatomic, retain) IBOutlet UILabel *labelApproved;
@property (nonatomic, retain) IBOutlet UITextView *purpose;
@property (nonatomic, retain) IBOutlet UIButton *btnDetail;
@property (nonatomic, retain) ExpenseListViewController *rootVC;
@property (nonatomic) NSUInteger *currentRow;

- (IBAction)buttonDetailPressed:(id)sender;

@end