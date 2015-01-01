//
//  ProfileTelCell.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ProfileTelCell : UITableViewCell <UITextFieldDelegate>

@property (strong, nonatomic) IBOutlet UITextField *textField;
@property (strong, nonatomic) IBOutlet UIImageView *custonImageView;
@property (strong, nonatomic) IBOutlet UILabel *underLine;

@end
