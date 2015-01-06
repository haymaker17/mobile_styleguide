//
//  FFField.m
//  ConcurMobile
//
//  Created by Laurent Mery on 13/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFField.h"
#import "CTEField.h"

#import "FFFieldProtocol.h"

@interface FFFieldLight ()

@property (nonatomic, retain) CTEField *def;
@property (nonatomic, retain) id<FFFieldProtocol> delegateFC;

@end


@implementation FFFieldLight

#pragma mark - FFFieldLight - init

-(id)initWithDef:(CTEField*)cteField andDelegate:(id)delegate{
    
    if (self = [super init]){
        
        self.delegateFC = delegate;
        
        self.def = cteField;
    }
    
    return self;
}

#pragma mark - FFFieldLight - properties

-(NSString *)label{
    
    return self.def.Label;
}


-(NSString*)name{
    
    return self.def.Name;
}

-(NSString*)defaultValue{
    
    return self.def.DefaultValue;
}

-(NSInteger)maxLength{
    
    return [self.def.MaxLength doubleValue];
}

-(BOOL)hasLineSeparator{
    
    return [@"true" isEqualToString:self.def.HasLineSeparator];
}

#pragma mark - FFFieldLight - Access

//public
-(void)setReadOnlyMax{
    
    if( [@"RW" isEqualToString:self.def.Access]){
        
        self.def.Access = @"RO";
    }
}

//public
-(void)setAccess:(NSString*)access{
    
    if (![access isEqualToString:self.def.Access]) {
        
        if ([@"RW" isEqualToString:access]){
            
            self.def.Access = access;
        }
        else if ([@"RO" isEqualToString:access]){
            
            self.def.Access = access;
        }

        self.def.Access = @"HD";
    }
}

//public
-(BOOL)isAccessRW{
    
    return [@"RW" isEqualToString:self.def.Access];
}

//public
-(BOOL)isAccessRO{
    
    return [@"RO" isEqualToString:self.def.Access];
}


//public
-(BOOL)isAccessHD{
    
    return [@"HD" isEqualToString:self.def.Access];
}

//public
-(BOOL)isVisible{
    
    return ![@"HD" isEqualToString:self.def.Access];
}


#pragma mark - FFFieldLight - Required

-(BOOL)isRequired{
    
    return [@"Y" isEqualToString:self.def.Required];
}

@end





@interface FFField ()

@property (nonatomic, strong) UIImage *iconLabel;

@property (copy, nonatomic) NSString *layoutType;
@property (copy, nonatomic) NSString *originalLayoutType;


@end


@implementation FFField

#pragma mark -  FFField - const

NSString * const FFFieldLayoutTypeBoolean =         @"Boolean";
NSString * const FFFieldLayoutTypeConnectedList =   @"ConnectedList";
NSString * const FFFieldLayoutTypeDate =            @"Date";
NSString * const FFFieldLayoutTypeList =            @"List";
NSString * const FFFieldLayoutTypeMoney =           @"Money";
NSString * const FFFieldLayoutTypeNumber =          @"Number";
NSString * const FFFieldLayoutTypeStatic =          @"Static";
NSString * const FFFieldLayoutTypeText =            @"Text";
NSString * const FFFieldLayoutTypeTextArea =        @"TextArea";
NSString * const FFFieldLayoutTypeTime =            @"Time";

#pragma mark -  FFField - init


-(id)initWithDef:(CTEField*)cteField andDataTypes:(CTEDataTypes*)dataType andDelegate:(id)delegate{
    
    if (self = [super initWithDef:cteField andDelegate:delegate]){
        
        self.dataType = dataType;
        [self initLayoutType];
    }
    
    return self;
}

-(void)initLayoutType{
    
    /*
     VARCHAR(static,      edit,textarea,picklist[extended requestid])
     INTEGER(static,      edit,picklist[MAin destination city])
     TIMESTAMP(static,    edit,date_edit[custom field])
     CHAR(                edit[approval status], time[time], picklist[main destination country])
     MONEY(static,        edit)
     BOOLEANCHAR(         checkbox,picklist)
     LIST(                picklist,list_edit)
     MLIST(               list_edit[connectedList])
     NUMERIC			  edit
     */
    
    if ([@"BOOLEANCHAR" isEqualToString:self.def.DataType]) {
        
        if ([@"edit" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeBoolean;
        }
        
        else if ([@"checkbox" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeBoolean;
        }
        
        else if ([@"picklist" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeList;
        }
    }
    
    else if ([@"VARCHAR" isEqualToString:self.def.DataType]) {
        
        if ([@"edit" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeText;
        }
        
        else if ([@"textarea" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeTextArea;
        }
        
        
        else if ([@"picklist" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeList;
        }
    }
    
    else if ([@"INTEGER" isEqualToString:self.def.DataType]) {
        
        if ([@"edit" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeNumber;
        }
        
        else if ([@"picklist" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeList;
        }
    }
    
    else if ([@"NUMERIC" isEqualToString:self.def.DataType]) {
        
        _layoutType = FFFieldLayoutTypeNumber;
    }
    
    else if ([@"TIMESTAMP" isEqualToString:self.def.DataType]) {
        
        _layoutType = FFFieldLayoutTypeDate;
    }
    
    else if ([@"CHAR" isEqualToString:self.def.DataType]) {
        
        if ([@"edit" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeStatic;
        }
        
        else if ([@"time" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeTime;
        }
        
        else if ([@"picklist" isEqualToString:self.def.CtrlType]){
            
            _layoutType = FFFieldLayoutTypeList;
        }
    }
    
    else if ([@"MONEY" isEqualToString:self.def.DataType]) {
        
        _layoutType = FFFieldLayoutTypeMoney;
    }
    
    else if ([@"LIST" isEqualToString:self.def.DataType]) {
        
        _layoutType = FFFieldLayoutTypeList;
    }
    
    else if ([@"MLIST" isEqualToString:self.def.DataType]) {
        
        _layoutType = FFFieldLayoutTypeConnectedList;
    }
    
    _originalLayoutType = _layoutType;
    
    
    if ([@"static" isEqualToString:self.def.CtrlType]) {
        
        [self setReadOnlyMax];
    }
    
    if (_layoutType == nil && ![self isAccessHD]) {
        
        NSLog(@"Undetermine type [ctrlType:%@, DataType:%@] in CTEFormsAndFields.transform", self.def.CtrlType, self.def.DataType);
        _layoutType = FFFieldLayoutTypeStatic; //to avoid fatal exception
    }
}

#pragma mark - FFField - property

-(NSString*)layoutType{
    
    return _layoutType;
}

#pragma mark - FFField - Access

//public
-(void)setReadOnlyMax{
    
    [super setReadOnlyMax];
    
    if ([@"RO" isEqualToString:self.def.Access]){
        
        _layoutType = FFFieldLayoutTypeStatic;
    }
}

//public
-(void)setAccess:(NSString*)access{
    
    if (![access isEqualToString:self.def.Access]) {
        
        if ([@"RW" isEqualToString:access]){
            
            if ([FFFieldLayoutTypeStatic isEqualToString:_layoutType]){
                
                _layoutType = _originalLayoutType;
            }
        }
        else if ([@"RO" isEqualToString:access]){
            
            _layoutType = FFFieldLayoutTypeStatic;
        }
        
        [super setAccess:access];
    }
}


#pragma mark - FFField - iconLabel

-(BOOL)hasIconLabel{
    
    BOOL hasIconLabel = NO;
    return hasIconLabel;
}

-(UIImage*)iconLabel{
    
    NSString *iconLabelStr = [self.delegateFC iconLabelForField:self];
    UIImage * iconLabelIcon;
    
    if (![@"" isEqualToString:iconLabelStr]){
        
        iconLabelIcon = [UIImage imageNamed:iconLabelStr];
    }
    
    return iconLabelIcon;
}

#pragma mark - FFField - layout


//public
-(BOOL)isTextLayout{
    
    return [FFFieldLayoutTypeText isEqualToString:_layoutType];
}

//public
-(BOOL)isTextAreaLayout{
    
    return [FFFieldLayoutTypeTextArea isEqualToString:_layoutType];
}

//public
-(BOOL)isMoneyLayout{
    
    return [FFFieldLayoutTypeMoney isEqualToString:_layoutType];
}

//public
-(BOOL)isNumberLayout{
    
    return [FFFieldLayoutTypeNumber isEqualToString:_layoutType];
}

//public
-(BOOL)isTimeLayout{
    
    return [FFFieldLayoutTypeTime isEqualToString:_layoutType];
}

//public
-(BOOL)isDateLayout{
    
    return [FFFieldLayoutTypeDate isEqualToString:_layoutType];
}

//public
-(BOOL)isBooleanLayout{
    
    return [FFFieldLayoutTypeBoolean isEqualToString:_layoutType];
}

//public
-(BOOL)isListLayout{
    
    return [FFFieldLayoutTypeList isEqualToString:_layoutType];
}

//public
-(BOOL)isConnectedListLayout{
    
    return [FFFieldLayoutTypeConnectedList isEqualToString:_layoutType];
}


#pragma mark - FFField - Validation

-(NSArray*)errorsOnValidate{
    
    NSArray *errors;
    
    if ([self isAccessRW]) {
        
        errors = [self.delegateFC errorsOnValidateField:self];
    }
    else {
        
        errors = [[NSArray alloc]init];
    }
    return errors;
}



#pragma mark - FFField - Memory managment

-(void)dealloc{
    
    self.def = nil;
    _dataType = nil;
}

@end
