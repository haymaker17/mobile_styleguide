//
//  ApproveReportsCell.h
//  ConcurMobile
//
//  Created by yiwen on 1/14/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ApproveReportsViewController;

@interface ApproveReportsCell : UITableViewCell {
	UILabel *labelEmployee;
	UILabel *labelTotal;
	
	UILabel *labelName;
	UILabel *labelDate;
    
    UIImageView *icon1;
    UIImageView *icon2;
    UIImageView *icon3;
    
	ApproveReportsViewController *rootVC;
	NSUInteger currentRow;
	
}

@property (nonatomic, retain) IBOutlet UILabel *labelName;
@property (nonatomic, retain) IBOutlet UILabel *labelTotal;
@property (nonatomic, retain) IBOutlet UILabel *labelEmployee;
@property (nonatomic, retain) IBOutlet UILabel *labelDate;
@property (nonatomic, retain) IBOutlet UIImageView *icon1;
@property (nonatomic, retain) IBOutlet UIImageView *icon2;
@property (nonatomic, retain) IBOutlet UIImageView *icon3;
@property (nonatomic, retain) ApproveReportsViewController *rootVC;
@property (nonatomic) NSUInteger currentRow;

- (NSString *) reuseIdentifier;
- (IBAction)buttonDrillPressed:(id)sender;

@end
