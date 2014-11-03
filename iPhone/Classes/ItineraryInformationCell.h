//
//  ItineraryInformationCell.h
//  ConcurMobile
//
//  Created by Wes Barton on 5/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ItineraryInformationCell : UITableViewCell
@property (strong, nonatomic) IBOutlet UITextView *messageText;
@property (strong, nonatomic) IBOutlet UILabel *messageLabel;

@property (strong, nonatomic) IBOutlet UILabel *singleDayLabel;
@property (strong, nonatomic) IBOutlet UITextView *singleDayMessage;

@end
