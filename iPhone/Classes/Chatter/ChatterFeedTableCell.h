//
//  ChatterFeedTableCell.h
//  ConcurMobile
//
//  Created by ernest cho on 6/13/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChatterFeedTableCell : UITableViewCell
@property (strong, nonatomic) IBOutlet UILabel *author;
@property (strong, nonatomic) IBOutlet UILabel *company;
@property (strong, nonatomic) IBOutlet UILabel *dateString;
@property (strong, nonatomic) IBOutlet UITextView *chatterText;
@property (strong, nonatomic) IBOutlet UIImageView *portrait;
@end
