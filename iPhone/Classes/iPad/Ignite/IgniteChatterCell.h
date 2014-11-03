//
//  IgniteChatterCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IgniteChatterCellDelegate.h"


@interface IgniteChatterCell : UITableViewCell
{
    id<IgniteChatterCellDelegate> __weak _delegate;
    
    UILabel         *lblName;
    UILabel         *lblCompanyName;
    UILabel         *lblAge;
    UILabel         *lblLikes;
    UILabel         *lblText;
    UIImageView     *imgView;
    UIImageView     *imgThumb;
    UIImageView     *imgReplyCount;
    UILabel         *lblReplyCount;
    UIButton        *btnReplyCount;
    UIImageView     *imgLink;
    UILabel         *lblLink;
    UIImageView     *imgFile;
    UILabel         *lblFile;
    UILabel         *lblTrip;
    UILabel         *lblTripDate;
    UIButton        *btnTrip;
    UIImageView     *imgReply;
}

@property (nonatomic, weak) id<IgniteChatterCellDelegate> delegate;
@property (nonatomic, strong) IBOutlet UILabel      *lblName;
@property (nonatomic, strong) IBOutlet UILabel      *lblCompanyName;
@property (nonatomic, strong) IBOutlet UILabel      *lblAge;
@property (nonatomic, strong) IBOutlet UILabel      *lblLikes;
@property (nonatomic, strong) IBOutlet UILabel      *lblText;
@property (nonatomic, strong) IBOutlet UIImageView  *imgView;
@property (nonatomic, strong) IBOutlet UIImageView  *imgThumb;
@property (nonatomic, strong) IBOutlet UIImageView  *imgReplyCount;
@property (nonatomic, strong) IBOutlet UILabel      *lblReplyCount;
@property (nonatomic, strong) IBOutlet UIButton     *btnReplyCount;
@property (nonatomic, strong) IBOutlet UIImageView  *imgLink;
@property (nonatomic, strong) IBOutlet UILabel      *lblLink;
@property (nonatomic, strong) IBOutlet UIImageView  *imgFile;
@property (nonatomic, strong) IBOutlet UILabel      *lblFile;
@property (nonatomic, strong) IBOutlet UILabel      *lblTrip;
@property (nonatomic, strong) IBOutlet UILabel      *lblTripDate;
@property (nonatomic, strong) IBOutlet UIButton     *btnTrip;
@property (nonatomic, strong) IBOutlet UIImageView  *imgReply;

+ (IgniteChatterCell*)makeCell:(UITableView*)tableView owner:(id)owner withDelegate:(id<IgniteChatterCellDelegate>) del;

- (IBAction)buttonReplyPressed:(id)sender;
- (IBAction)buttonConversationPressed:(id)sender;
- (IBAction)buttonTripPressed:(id)sender;

@end
