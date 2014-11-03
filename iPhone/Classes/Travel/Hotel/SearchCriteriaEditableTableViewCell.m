//
//  SearchCriteriaEditableTableViewCell.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/24/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SearchCriteriaEditableTableViewCell.h"
#import "UIResponder+NextUIResponder.h"

@implementation SearchCriteriaEditableTableViewCell{
    BOOL isNewSearch;
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
        self.textFieldSubtitle.clearButtonMode = UITextFieldViewModeAlways;
    }
    return self;
}

- (void)awakeFromNib
{
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

-(void)setCellData:(SearchCriteriaCellData *)cellData
{
    self.lblTitle.text = cellData.title;
    self.textFieldSubtitle.text = cellData.subTitle;
    self.ivIcon.image = [UIImage imageNamed:cellData.imageName];
    self.textFieldSubtitle.delegate = self;
}

#pragma mark -
#pragma mark Text Field Methods
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
//    return [textField.nextUIResponder becomeFirstUIResponder];
    return [textField resignFirstResponder];
}
- (void)textFieldDidBeginEditing:(UITextField *)textField{
    self.textFieldSubtitle.clearButtonMode = UITextFieldViewModeAlways;
    if(!isNewSearch){
        textField.text = @"";
        isNewSearch = YES;
    }
}

- (void)textFieldDidEndEditing:(UITextField *)textField
{
    self.textFieldSubtitle.clearButtonMode = UITextFieldViewModeAlways;
    if (self.onTextChanged) {
        self.onTextChanged(textField.text);
    }
}

@end
