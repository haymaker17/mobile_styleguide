//
//  CTEDataTypes.h
//  ConcurSDK
//
//  Created by Laurent Mery on 05/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

/*
 
 The Idea is
 
 The view (concurMobile) use only [model.property stringValue] to print value without take care on type
 the format is managed into the Model across the DataType object
 
 So, Model initialize datatype with correct type (text, date, list...)
 by the way we remove all doublon type like list and customField.list for example
 
 Value on datatype is saved on intermediate format
 for example, webService could provide text date with various format : M/dd/YYYY or YYYY-MM-ddTHH:mm:ss or h:mm a
 and we saved the value into datatype with NSDate
 
 The singleton CTEDataTypesManager will be usefull for the view to display date and money type within the correct format
 ConcurMobile will set dateTemplate or currencyCode, after each call to stringValue for date or money type WILL USED THE correct internationnal format
 (this is the general use, but you could set individualy the template date or currency Code)
 
 */

#import <Foundation/Foundation.h>



extern NSString *const dateModeDATE;
extern NSString *const dateModeTIME;
extern NSString *const dateModeDATETIME;









#pragma mark - singleton CTEDateTypesManager

//this singleton authorized to set global input/ouput format
@class CTERXMLElement;

@interface CTEDataTypesManager : NSObject

@property (nonatomic, copy) NSString *dateInputFormat;
@property (nonatomic, copy) NSString *dateOutputTemplate;
@property (nonatomic, copy) NSString *timeOutputTemplate;
@property (nonatomic, copy) NSString *datetimeOutputTemplate;
@property (nonatomic, copy) NSString *currencyCode;
//TODO: manage list entry

+ (id)sharedManager;

@end












@interface CTEDataTypes : NSObject



// DataType Type : ConnectedList || Text || Boolean || Date || List || Number || Amount  (TODO: ADD Comment Type)
@property (nonatomic, copy, readonly) NSString *Type;


#pragma mark - values property

@property (nonatomic, copy, readonly)   NSString    *Text;
@property (nonatomic, copy)             NSDate      *Date;
@property (nonatomic, copy)             NSNumber    *Number;
@property (nonatomic, assign)           BOOL        Boolean;
@property (nonatomic, copy)             NSString    *ListCode;

//format to convert string date (to be set before the call to setValue)

//properties relative to List Type and ConnectedList Type
@property (nonatomic, copy, readonly)   NSString    *ListItemID;
@property (nonatomic, copy)             NSString    *ListText;

//properties relative to Date Type
@property (nonatomic, copy)             NSString    *dateInputFormat;
@property (nonatomic, copy)             NSString    *dateOutputTemplate;
@property (nonatomic, copy, readonly)   NSString    *dateMode;

//property relative to Money Type
@property (nonatomic, copy)             NSString    *CurrencyCode;

//set by model
@property (nonatomic, assign, readonly) BOOL isCustom;


#pragma mark - inits

//text
-(id)initTextWithValue:(NSString*)text;

//date
-(id)initDateWithValue:(NSString*)stringDate andDateInputFormat:(NSString*)format andDateMode:(NSString*)dateMode;
-(id)initDateWithNSDate:(NSDate*)date andDateInputFormat:(NSString*)format andDateMode:(NSString*)dateMode;

//number
-(id)initNumberWithValue:(NSString*)stringNumber;

//amount
//TODO : manage exchange rate
-(id)initAmountWithValue:(NSString*)value andCurrencyCode:(NSString*)currencyCode;

//boolean
-(id)initBooleanWithValue:(NSString*)stringBoolean;

//list
-(id)initListWithValue:(NSString*)text code:(NSString*)code listItemId:(NSString*)listItemId;


#pragma mark - set values


//text
-(void)setStringValue:(NSString*)value;

//date (get/set Date)

//List (get/set ListCode ; ListText)



#pragma mark - get values

//all types (voncersion text value)
-(NSString*)stringValue;

//money
-(NSDictionary*)currencyList;

#pragma mark - status methods

-(BOOL)isTextType;
-(BOOL)isBooleanType;
-(BOOL)isDateType;
-(BOOL)isListType;
-(BOOL)isConnectedListType;
-(BOOL)isNumberType;
-(BOOL)isAmountType;
-(BOOL)isEmpty;

-(BOOL)isDateModeIsDate;
-(BOOL)isDateModeIsDateTime;
-(BOOL)isDateModeIsTime;

#pragma mark - compare methodq

-(BOOL)isEqualToDataTypeByValue:(CTEDataTypes*)dataType;

#pragma mark - form methods

-(BOOL)isDirty;
-(void)reset;

@end