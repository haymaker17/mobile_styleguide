//
//  ItinDetailsHotelCell.h
//  ConcurMobile
//
//  Created by yiwen on 12/15/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItinDetailsViewController;

@interface ItinDetailsHotelCell : UITableViewCell 
{
	UILabel *txtBooking;
	UILabel *txtCheckIn;
	UILabel *txtCheckOut;
	UILabel *txtStatus;
	UILabel *txtPhone;
	UILabel *txtCancellation;
	UIButton *btnAction;
	ItinDetailsViewController *rootVC;
}

@property (nonatomic, retain) IBOutlet UILabel *txtBooking;
@property (nonatomic, retain) IBOutlet UILabel *txtCheckIn;
@property (nonatomic, retain) IBOutlet UILabel *txtCheckOut;
@property (nonatomic, retain) IBOutlet UILabel *txtStatus;
@property (nonatomic, retain) IBOutlet UILabel *txtPhone;
@property (nonatomic, retain) IBOutlet UILabel *txtCancellation;
@property (nonatomic, retain) IBOutlet UIButton *btnAction;

@property (nonatomic, retain) ItinDetailsViewController *rootVC;

- (IBAction)buttonDrillPressed:(id)sender;

@end
