//
//  LoginHelpContentsCell.h
//  ConcurMobile
//
//  Created by charlottef on 12/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface LoginHelpContentsCell : UITableViewCell
{
    UILabel *primaryTextLabel;
}

@property (strong, nonatomic) IBOutlet UILabel  *primaryTextLabel;

+ (id) makeCell;

@end
