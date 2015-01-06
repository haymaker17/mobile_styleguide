//
//  FFField.h
//  ConcurMobile
//
//  Created by Laurent Mery on 13/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CTEDataTypes.h"

@class CTEField;

extern NSString *const FFFieldLayoutTypeBoolean;
extern NSString *const FFFieldLayoutTypeConnectedList;
extern NSString *const FFFieldLayoutTypeDate;
extern NSString *const FFFieldLayoutTypeList;
extern NSString *const FFFieldLayoutTypeMoney;
extern NSString *const FFFieldLayoutTypeNumber;
extern NSString *const FFFieldLayoutTypeStatic;
extern NSString *const FFFieldLayoutTypeText;
extern NSString *const FFFieldLayoutTypeTextArea;
extern NSString *const FFFieldLayoutTypeTime;




@interface FFFieldLight : NSObject


#pragma mark - FFFieldLight - init

-(id)initWithDef:(CTEField*)cteField andDelegate:(id)delegate;

#pragma mark - FFFieldLight - properties

-(NSString*)label;
-(NSString*)name;
-(NSString*)defaultValue;
-(NSInteger)maxLength;
-(BOOL)hasLineSeparator;


#pragma mark - FFFieldLight - Access

-(void)setReadOnlyMax;
-(void)setAccess:(NSString*)access;
-(BOOL)isAccessRW;
-(BOOL)isAccessRO;
-(BOOL)isAccessHD;
-(BOOL)isVisible;

#pragma mark - FFFieldLight - validation

-(BOOL)isRequired;



@end











@interface FFField : FFFieldLight

@property (nonatomic, retain) CTEDataTypes *dataType;




#pragma mark - FFField - init

-(id)initWithDef:(CTEField*)cteField andDataTypes:(CTEDataTypes*)dataType andDelegate:(id)delegate;


#pragma mark - FFField - properties

-(NSString*)layoutType;




#pragma mark - FFField - layout

-(BOOL)isTextLayout;
-(BOOL)isTextAreaLayout;
-(BOOL)isMoneyLayout;
-(BOOL)isNumberLayout;
-(BOOL)isTimeLayout;
-(BOOL)isDateLayout;
-(BOOL)isBooleanLayout;
-(BOOL)isListLayout;
-(BOOL)isConnectedListLayout;


#pragma mark - FFField - validation

-(NSArray*)errorsOnValidate;


@end
