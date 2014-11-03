//
//  ReportDetailCell_iPad.h
//  ConcurMobile
//
//  Created by charlottef on 3/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ReportDetailCell_iPad : UITableViewCell

@property (strong, nonatomic) IBOutlet UILabel  *nameLabel;
@property (strong, nonatomic) IBOutlet UILabel  *dateLabel;
@property (strong, nonatomic) IBOutlet UILabel  *descriptionLabel;

@property (strong, nonatomic) IBOutlet UILabel  *amountLabel;
@property (strong, nonatomic) IBOutlet UILabel  *requestedLabel;

@property (strong, nonatomic) IBOutlet UIButton *receiptButton;
@property (strong, nonatomic) IBOutlet UILabel  *viewReceiptLabel;

@property (strong, nonatomic) IBOutlet UIImageView  *iv0;
@property (strong, nonatomic) IBOutlet UIImageView  *iv1;
@property (strong, nonatomic) IBOutlet UIImageView  *iv2;

// Extended
@property (strong, nonatomic) IBOutlet UILabel  *separatorLabel;
@property (strong, nonatomic) IBOutlet UILabel  *extraLabel0;
@property (strong, nonatomic) IBOutlet UILabel  *extraLabel1;

@end
