//
//  FormFieldCell.h
//  ConcurMobile
//
//  Created by yiwen on 4/18/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FormFieldData.h"

@interface FormFieldCell : UITableViewCell 
{
    UILabel			*lblLabel;
	UILabel			*lblValue;
	UILabel			*lblErrMsg;
    UILabel         *lblConnListLevel;
	FormFieldData	*field;

}

@property (nonatomic, strong) IBOutlet UILabel	*lblLabel;
@property (nonatomic, strong) IBOutlet UILabel	*lblValue;
@property (nonatomic, strong) IBOutlet UILabel	*lblErrMsg;
@property (nonatomic, strong) IBOutlet UILabel	*lblConnListLevel;

@property (nonatomic, strong) FormFieldData		*field;

-(void) resetCellContent:(FormFieldData*) fld;
+(UIColor*) getLabelColor;

@end
