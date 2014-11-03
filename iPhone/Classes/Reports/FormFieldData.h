//
//  FormFieldData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface FormFieldData : NSObject <NSCopying>
{
	NSString *ctrlType, *dataType, *iD, *label, *fieldValue;
	NSString *listKey, *liCode, *liKey;
	NSString *required, *maxLength, *access, *tip, *searchable;
	NSString *isCopyDownSourceForOtherForms;
	NSString *itemCopyDownAction;
    // Copy down, form merge
    NSString *defaultValue, *cpDownSource, *cpDownFormType;
    
	// For connected list
	NSString *ftCode, *parFieldId, *parFtCode;
	int hierKey, hierLevel, parHierLevel;
	NSString *parLiKey;
	
    // MOB-11780: Check validationexpression for business purpose field
    NSString *validationExpression, *failureMsg;
	// For error validation
	NSString *validationErrMsg;
    
    // Extra displayInfo, e.g. polKey for expenseType,rptKey for receiptType
    // crnCode for amount fields
    NSObject	*extraDisplayInfo;
    
    NSMutableArray *listChoices;        //keep full list of content
    NSMutableArray *searchableListChoices;
    NSString       *isDynamicField;
    NSString       *formFieldKey;
    NSString       *originalCtrlType;
}

@property (strong, nonatomic) NSString *ctrlType;
@property (strong, nonatomic) NSString *dataType;
@property (strong, nonatomic) NSString *iD;
@property (strong, nonatomic) NSString *label;
@property (strong, nonatomic) NSString *fieldValue;		// Display value - UTC date time - liName
@property (strong, nonatomic) NSString *listKey;
@property (strong, nonatomic) NSString *liCode;
@property (strong, nonatomic) NSString *liKey;
@property (strong, nonatomic) NSString *required;
@property (strong, nonatomic) NSString *maxLength;
@property (strong, nonatomic) NSString *access;
@property (strong, nonatomic) NSString *isCopyDownSourceForOtherForms;
@property (strong, nonatomic) NSString *tip;
@property (strong, nonatomic) NSString *searchable;
@property (strong, nonatomic) NSString *itemCopyDownAction;

@property (strong, nonatomic) NSString *ftCode;
@property (strong, nonatomic) NSString *parFieldId;
@property (strong, nonatomic) NSString *parFtCode;
@property (strong, nonatomic) NSString *parLiKey;

@property (strong, nonatomic) NSString *defaultValue;
@property (strong, nonatomic) NSString *cpDownFormType;
@property (strong, nonatomic) NSString *cpDownSource;

@property (strong, nonatomic) NSString *validationErrMsg;
@property (strong, nonatomic) NSString *validationExpression, *failureMsg;
@property (strong, nonatomic) NSObject *extraDisplayInfo;

@property (strong, nonatomic) NSMutableArray *listChoices;
@property (strong, nonatomic) NSMutableArray *searchableListChoices;

@property int hierLevel;
@property int hierKey;
@property int parHierLevel;

@property (strong, nonatomic) NSString *isDynamicField;
@property (strong, nonatomic) NSString *formFieldKey;
@property (strong, nonatomic) NSString *originalCtrlType;

+(void) copyValuesFromFields:(NSArray*)srcFields toFields:(NSArray*)destFields exceptIds:(NSDictionary*)exceptDict;
+(NSMutableDictionary*) makeFieldDict:(NSArray*)fields;
+ (NSMutableDictionary*) getXmlToPropertyMap;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;
-(id)initField:(NSString*)id label:(NSString*)lbl value:(NSString*)val ctrlType:(NSString*)cType dataType:(NSString*)dType;
-(NSString*) getServerValue;

-(NSString*) getFullLabel;

-(BOOL) isRequired;
-(BOOL) isEditable;
-(BOOL) isMissingValue;
-(BOOL) isSearchable;
-(BOOL) requiresNumericInput;
-(BOOL) needsSecureEntry; // Password/Pin
+(BOOL) isLocationFieldId:(NSString*)fieldId;
+(BOOL) isCurrencyFieldId:(NSString*)fieldId;

// APIs to access extra display info
-(NSString*) getPolKeyForExpFldType;
-(NSString*) getCrnCodeForMoneyFldType;
-(NSString*) getRptKeyForReceiptTypeFld;

@end
