//
//  AttendeeCell.h
//  ConcurMobile
//
//  Created by yiwen on 9/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface AttendeeCell : UITableViewCell 
{
	UILabel			*lblName, *lblAmount, *lblType;
}

@property (strong, nonatomic) IBOutlet UILabel *lblName;
@property (strong, nonatomic) IBOutlet UILabel *lblAmount;
@property (strong, nonatomic) IBOutlet UILabel *lblType;

@end
