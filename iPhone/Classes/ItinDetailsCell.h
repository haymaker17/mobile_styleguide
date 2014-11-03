//
//  ItinDetailsCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItinDetailsViewController;

@interface ItinDetailsCell : UITableViewCell {
	UILabel *labelSection1;
	UILabel *labelSection2;
	UILabel *labelSection3;
	UILabel *labelSection4;
	UILabel *label1Section1;
	UILabel *label2Section1;
	UILabel *label1Section2;
	UILabel *label2Section2;
	UILabel *label1Section3;
	UILabel *label2Section3;
	UILabel *label3Section3;
	UILabel *label1Section4;
	UILabel *labelVendor;
	UIImageView *imgVendor;
	UIImageView *imgDetail;
	UIButton *btnAction;
	ItinDetailsViewController *rootVC;
}

@property (nonatomic, retain) IBOutlet UILabel *labelSection1;
@property (nonatomic, retain) IBOutlet UILabel *labelSection2;
@property (nonatomic, retain) IBOutlet UILabel *labelSection3;
@property (nonatomic, retain) IBOutlet UILabel *labelSection4;

@property (nonatomic, retain) IBOutlet UILabel *label1Section1;
@property (nonatomic, retain) IBOutlet UILabel *label2Section1;
@property (nonatomic, retain) IBOutlet UILabel *label1Section2;
@property (nonatomic, retain) IBOutlet UILabel *label2Section2;
@property (nonatomic, retain) IBOutlet UILabel *label1Section3;
@property (nonatomic, retain) IBOutlet UILabel *label2Section3;
@property (nonatomic, retain) IBOutlet UILabel *label3Section3;
@property (nonatomic, retain) IBOutlet UILabel *label1Section4;

@property (nonatomic, retain) IBOutlet UILabel *labelVendor;
@property (nonatomic, retain) IBOutlet UIButton *btnAction;

@property (nonatomic, retain) IBOutlet UIImageView *imgVendor;
@property (nonatomic, retain) IBOutlet UIImageView *imgDetail;

@property (nonatomic, retain) ItinDetailsViewController *rootVC;

- (IBAction)buttonDrillPressed:(id)sender;

@end
