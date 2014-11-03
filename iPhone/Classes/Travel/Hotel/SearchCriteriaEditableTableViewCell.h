//
//  SearchCriteriaEditableTableViewCell.h
//  ConcurMobile
//
//  Created by Sally Yan on 7/24/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SearchCriteriaCellData.h"

@interface SearchCriteriaEditableTableViewCell : UITableViewCell <UITextFieldDelegate>

@property (weak, nonatomic) IBOutlet UILabel *lblTitle;
@property (weak, nonatomic) IBOutlet UITextField *textFieldSubtitle;
@property (weak, nonatomic) IBOutlet UIImageView *ivIcon;

-(void)setCellData:(SearchCriteriaCellData *)cellData;
@property (copy,nonatomic) void(^onTextChanged)(NSString *textString);

@end
