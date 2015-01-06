//
//  FFTextCell.m
//  ConcurMobile
//
//  Created by laurent mery on 24/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells-private.h"




@implementation FFTextCell


NSString *const FFCellReuseIdentifierText = @"TextCell";


#pragma mark - init

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
    
    self.hasKeyboard = YES;
    
    if (self = [super initWithStyle:style
                    reuseIdentifier:reuseIdentifier]){
        
    }
    return self;
}

#pragma mark - layout

-(void)render{
    
    [super render];
    
    _inputValue = [self createInputValue];
    [_inputValue setDelegate:self];
    [self.contentView addSubview:_inputValue];
}

-(void)initLayout{
    
    [super initLayout];
    
    //init constraint - elements
    [self.constraintsElements setObject:_inputValue forKey:@"inputValue"];
    
    //label
    [self.constraintsMetrics setObject:@20 forKey:@"inputValueHeight"];
    
    //set constraints
    [self addCellVisualFormatConstraints:@{
                                           @"HValue": @"H:|-margeLeft-[inputValue]-margeRight-|",
                                           @"VValue": @"V:[inputValue(inputValueHeight)]-margeBottom-[lineSeparator(lineSeparatorHeight)]|"
                                           }];
}

-(void)doLayout{
    
    [super doLayout];
    
    [_inputValue needsUpdateConstraints];
}


#pragma mark - component


-(UITextField*)createInputValue{
    
    UITextField *input = [[UITextField alloc] initWithFrame:CGRectNull];
    
    input.translatesAutoresizingMaskIntoConstraints = NO;
    
    [input setTextColor:[UIColor textFormInput]];
    [input setFont:[UIFont fontWithName:@"HelveticaNeue" size:18.0]];
    
    [input setClearButtonMode:UITextFieldViewModeWhileEditing];
    [input setKeyboardType:UIKeyboardTypeDefault];
    [input setKeyboardAppearance:UIKeyboardAppearanceDefault];
    
    return input;
}


//public
-(void)updateDataType{
    
    [super updateDataType];
    
    [_inputValue setText:[self.field.dataType stringValue]];
}



#pragma mark - validation

-(NSArray *)errorsOnValidateField:(FFField *)field{
    
    NSMutableArray *errors = [NSMutableArray arrayWithArray:[super errorsOnValidate]];
    
    //required
    if ([self.field isRequired] && [self.field.dataType isEmpty]){
        
        [errors addObject:[@"Required Fields Missing" localize]];
    }
    
    //maxLength
    NSNumber *maxLength = [NSNumber numberWithDouble:[self.field maxLength]];
    if ([[self.field.dataType stringValue] length] > [maxLength doubleValue]){
        
        [errors addObject:[NSString stringWithFormat:[@"FIELD_MAX_LENGTH_ERR_MSG" localize], [self.field maxLength]]];
    }
    
    return [errors copy];
}


#pragma mark - manage input

- (BOOL)textFieldShouldReturn:(UITextField *)textField{
    
    [textField resignFirstResponder];
    [self onBlur];
    return NO;
}

- (BOOL)myInputValueIsFirstResponder{
    
    return [self.inputValue isFirstResponder];
}


- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField{
    
    //if value is empty, label is align verticaly.
    //so, reajust position label when we swich on edit mode
    if ([self.field.dataType isEmpty] && self.vAlignCenterIfEmpty) {
        
        [self labelSetDefaultConstraints];
    }
    
    return YES;
}


#pragma mark - manage memory

-(void)onBlur{
    
    //update DataType
    [self.field.dataType setStringValue:_inputValue.text];
    [self updateDataType];
}



@end
