//
//  TextFieldEditCell.h
//  ConcurMobile
//
//  Created by yiwen on 11/5/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormFieldData.h"
#import "FieldEditDelegate.h"

@class TextFieldEditCell;

@protocol TextFieldEditCellDelegate

-(void) textFieldSelected:(TextFieldEditCell*) cell;
-(void) textFieldDeselected:(TextFieldEditCell*) cell;

-(void) moveToPrevious:(FormFieldData*) field;
-(void) moveToNext:(FormFieldData*) field;
-(void) cancelEdit:(FormFieldData*) field;
-(void) doneEdit:(FormFieldData*) field;

@end


@interface TextFieldEditCell : UITableViewCell <UITextFieldDelegate>
{
	UITextField		*txtValue;
	UILabel			*labelLabel;
	UILabel			*labelValue;
	UILabel			*labelErrMsg;
	FormFieldData	*field;
	id<TextFieldEditCellDelegate, FieldEditDelegate> __weak _delegate;
	BOOL			delayKeyboardDismissal, isTipHidden;
	
	UIView			*viewTip;
	UIButton		*btnTip;
	UILabel			*lblTip;
	UIImageView			*imgTip, *imgTipTop, *imgTipMiddle, *imgTipBottom;
}

@property (nonatomic, strong) IBOutlet UITextField	*txtValue;
@property (nonatomic, strong) IBOutlet UILabel	*labelLabel;
@property (nonatomic, strong) IBOutlet UILabel	*labelValue;
@property (nonatomic, strong) IBOutlet UILabel	*labelErrMsg;
@property (nonatomic, strong) FormFieldData		*field;
@property (nonatomic, weak) id<TextFieldEditCellDelegate, FieldEditDelegate> delegate;

@property (nonatomic, strong) IBOutlet UIView			*viewTip;
@property (nonatomic, strong) IBOutlet UIButton			*btnTip;
@property (nonatomic, strong) IBOutlet UILabel			*lblTip;
@property (nonatomic, strong) IBOutlet UIImageView			*imgTip;
@property (nonatomic, strong) IBOutlet UIImageView			*imgTipTop;
@property (nonatomic, strong) IBOutlet UIImageView			*imgTipMiddle;
@property (nonatomic, strong) IBOutlet UIImageView			*imgTipBottom;
@property BOOL delayKeyboardDismissal;
@property BOOL isTipHidden;
//+(TextFieldEditCell*)makeEmptyCell:(UITableView*)tableView owner:(id)owner;

//+(TextFieldEditCell*)makeCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) fld;
+(TextFieldEditCell*)makeDatetimeCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) fld;
+(TextFieldEditCell*)makeStaticCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) fld;
+(TextFieldEditCell*)makeEditCell:(UITableView*)tableView owner:(id)owner field:(FormFieldData*) fld;
+(TextFieldEditCell*)makeStaticCell:(UITableView*)tableView owner:(id)owner label:(NSString*)lbl value:(NSString*)val;

+(BOOL) canUseTextFieldEditor:(FormFieldData*)field;

-(void)textDoneEditing:(id)sender;

- (void)textFieldDidBeginEditing:(id)sender;
- (BOOL)textFieldShouldReturn:(UITextField *)theTextField;
-(void) finishEditing;

-(void)textChanged:(id)sender;

-(void)clickOutside:(id) sender;

-(IBAction) hideTip:(id)sender;
-(IBAction) showTip:(id)sender;
-(void) showAnimatedTip;
-(void) hideAnimatedTip;
-(void)hideAnimationDidStop;
@end
