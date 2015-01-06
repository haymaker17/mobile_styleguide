//
//  FFBaseCell-private.h
//  ConcurMobile
//
//  Created by laurent mery on 25/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells.h"
#import "UIColor+ConcurColor.h"

#import "FFField.h"

#import "FFCellDelegateProtocol.h"

@interface FFBaseCell ()

@property (nonatomic, strong) UILabel *label;
@property (nonatomic, retain) UIImage *icoLabel;
@property (nonatomic, strong) UIView *lineSeparator;

@property (nonatomic, strong) NSMutableDictionary *constraintsElements;
@property (nonatomic, strong) NSMutableDictionary *constraintsMetrics;
@property (nonatomic, strong) NSMutableDictionary *cellConstraints;

@property (nonatomic, retain) id<FFCellDelegateProtocol> delegateFC;
@property (nonatomic, strong) NSArray *errors;
@property (nonatomic, assign) BOOL hasKeyboard;
@property (nonatomic, assign) BOOL vAlignCenterIfEmpty;
@property (nonatomic, assign) CGFloat lineSeparatorHeight;
@property (nonatomic, assign) CGFloat lineSeparatorHeightWithFormFieldHasLineSeparator;

-(void)render;
-(void)initLayout;
-(void)doLayout;

-(void)markRW:(BOOL)isRW;
-(void)markValid:(BOOL)isValid;
-(void)markRequired:(BOOL)isRequired;

-(void)updateDataType;
-(void)labelSetDefaultConstraints;
-(void)labelSetMiddleConstraints;

-(void)addCellVisualFormatConstraints:(NSDictionary*)newCellConstraints;
-(void)removeCellVisualFormatConstraints:(NSArray*)removeCellConstraints;

-(CGFloat)heightViewAddHeightOfLineSeparator;

-(BOOL)isValid;
-(NSArray*)errorsOnValidate;

@end





@interface FFStaticCell ()

@property (nonatomic, strong) UILabel *labelValue;

-(UILabel*)createLabelValue;

@end





@interface FFTextCell ()  <UITextFieldDelegate>

@property (nonatomic, strong) UITextField *inputValue;

-(UITextField*)createInputValue;

@end




@interface FFTextAreaCell ()
@end




@interface FFDateCell ()

@property (nonatomic, strong) UIDatePicker *datePicker;

@end




@interface FFNumberCell ()
@end




@interface FFMoneyCell ()

@property (nonatomic, strong) UIView *viewAmount;
@property (nonatomic, strong) UIView *viewCurrency;

@end



