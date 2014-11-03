//
//  IgniteChatterConversationCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "IgniteChatterConversationCellDelegate.h"


@interface IgniteChatterConversationCell : UITableViewCell
{
    id<IgniteChatterConversationCellDelegate> __weak _delegate;
    
    UILabel         *lblName;
    UILabel         *lblCompanyName;
    UILabel         *lblAge;
    UILabel         *lblLikes;
    UILabel         *lblText;
    UIImageView     *imgView;
    UIImageView     *imgThumb;
    UIImageView     *imgLink;
    UILabel         *lblLink;
    UIImageView     *imgFile;
    UILabel         *lblFile;
}

@property (nonatomic, weak) id<IgniteChatterConversationCellDelegate> delegate;
@property (nonatomic, strong) IBOutlet UILabel      *lblName;
@property (nonatomic, strong) IBOutlet UILabel      *lblCompanyName;
@property (nonatomic, strong) IBOutlet UILabel      *lblAge;
@property (nonatomic, strong) IBOutlet UILabel      *lblLikes;
@property (nonatomic, strong) IBOutlet UILabel      *lblText;
@property (nonatomic, strong) IBOutlet UIImageView  *imgView;
@property (nonatomic, strong) IBOutlet UIImageView  *imgThumb;
@property (nonatomic, strong) IBOutlet UIImageView  *imgLink;
@property (nonatomic, strong) IBOutlet UILabel      *lblLink;
@property (nonatomic, strong) IBOutlet UIImageView  *imgFile;
@property (nonatomic, strong) IBOutlet UILabel      *lblFile;

+ (IgniteChatterConversationCell*)makeCell:(UITableView*)tableView owner:(id)owner withDelegate:(id<IgniteChatterConversationCellDelegate>) del;

- (IBAction)buttonConversationPressed:(id)sender;

@end
