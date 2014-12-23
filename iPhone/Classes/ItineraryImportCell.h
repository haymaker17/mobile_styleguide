//
//  ItineraryImportCell.h
//  ConcurMobile
//
//  Created by Wes Barton on 5/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ItineraryImportCell : UITableViewCell
@property (strong, nonatomic) IBOutlet UILabel *title;
@property (strong, nonatomic) IBOutlet UILabel *StartLabel;
@property (strong, nonatomic) IBOutlet UILabel *EndLabel;
@property (strong, nonatomic) IBOutlet UILabel *startDate;
@property (strong, nonatomic) IBOutlet UILabel *endDate;
@property (strong, nonatomic) IBOutlet UITextView *messageText;
@property (strong, nonatomic) IBOutlet UIImageView *selectedImage;
@property (strong, nonatomic) IBOutlet UIImageView *iconImageOne;
@property (strong, nonatomic) IBOutlet UIImageView *iconImageTwo;
@property (strong, nonatomic) IBOutlet UIImageView *iconImageThree;
@property (strong, nonatomic) IBOutlet UIImageView *iconImageFour;
@property (strong, nonatomic) IBOutlet UIImageView *iconImageFive;
@property (strong, nonatomic) IBOutlet NSLayoutConstraint *iconImageOneHeight;

@end
