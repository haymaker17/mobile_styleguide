//
//  NameCell.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface NameCell : UITableViewCell <UITextFieldDelegate>
@property (strong, nonatomic) IBOutlet UIImageView *customImageView;
@property (strong, nonatomic) IBOutlet UITextField *firstNameTextField;
@property (strong, nonatomic) IBOutlet UITextField *lastNameTextField;

@end
