//
//  CTEField.h
//  ConcurSDK
//
//  Created by laurent mery on 10/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CTEField : NSObject

@property (copy, nonatomic, readonly) NSString *ID;							//form field key (midtier name is FfKey)
@property (copy, nonatomic, readonly) NSString *Sequence;						//display order
@property (copy, nonatomic) NSString		   *CtrlType;						//like "Edit" or "picklist" or "textarea" or "static"
@property (copy, nonatomic, readonly) NSString *OriginalCtrlType;				//virtual Field (set by implementation)
@property (copy, nonatomic, readonly) NSString *DataType;						//like "VARCHAR" or "MONEY" or "INTEGER" or "TIMESTAMP" or "NUMERIC"
@property (copy, nonatomic, readonly) NSString *Required;						// "Y" or "N"
@property (copy, nonatomic) NSString		   *Access;						// "HD" or "RO" or "RW"
@property (copy, nonatomic, readonly) NSString *OriginalAccess;				//virtual Field (set by implementation)
@property (copy, nonatomic, readonly) NSString *FormTypeCode;
@property (copy, nonatomic, readonly) NSString *Custom;						// "Y" or "N"
@property (copy, nonatomic, readonly) NSString *IsDynamicField;				// "Y" or "N"
@property (copy, nonatomic, readonly) NSString *Label;						//localized label
@property (copy, nonatomic, readonly) NSString *Name;							//name of the field (midtier name is Id)
@property (copy, nonatomic, readonly) NSString *DefaultValue;					//Contains the default value if configured copydown is "constant"
@property (copy, nonatomic, readonly) NSString *ValidationExpression;
@property (copy, nonatomic, readonly) NSString *FailureMsg;					//localized failure message to display if validationExpression return false
@property (copy, nonatomic, readonly) NSString *MaxLength;


//for crtlType == "textarea"
@property (copy, nonatomic, readonly) NSString *Width;
@property (copy, nonatomic, readonly) NSString *Rows;
@property (copy, nonatomic, readonly) NSString *Cols;


//for copy down
@property (copy, nonatomic, readonly) NSString *CopyDownSource;				// like "EMPINFO"
@property (copy, nonatomic, readonly) NSString *CopyDowFormType;				// like "AgencyOfficeKey"
@property (copy, nonatomic, readonly) NSString *ItemCopyDownAction;


//for list
@property (copy, nonatomic, readonly) NSString *DefaultValueLiCode;			//Contains the default value for the code if configured copydown is "constant" and the field's type is list


//for connected list
@property (copy, nonatomic, readonly) NSString *HierLevel;
@property (copy, nonatomic, readonly) NSString *HierKey;						//key used to identify a connected list hierarchy
@property (copy, nonatomic, readonly) NSString *PartHierLevel;				//the hier level of the parent field for connected lists

//@property (copy, nonatomic, readonly) NSString *OriginalCtrlType;	//original value of ctrlType property
//@property (copy, nonatomic, readonly) NSString *OriginalAccess;	// original Value of Access property
//@property (copy, nonatomic) NSString *FieldTypeKey;
//@property (copy, nonatomic) NSString *HasLineSeparator;			// "Y" or "N"
//@property (copy, nonatomic) NSString *HiddenIfEmpty;				// "Y" or "N"
//@property (copy, nonatomic) NSString *SpanRow;					// "true" or "false"
//@property (copy, nonatomic) NSString *IsListManaged;				// "true" or "false"
//@property (copy, nonatomic) NSString *IsCCToEntryMapping;			// "true" or "false"
//@property (copy, nonatomic) NSString *Tooltip;

//for list
//@property (copy, nonatomic) NSString *ListKey;					//Encrypted key for the list resource if the field's type is list
//@property (copy, nonatomic) NSString *DefaultValueLiKey;			//Contains the default value for the key if configured copydown is "constant" and the field's type is list
//@property (assign, nonatomic) NSString *ParLiKey;					//For connected Lists the parent field's value


-(void)setReadOnlyMax;
- (id)valueForUndefinedKey:(NSString *)key;

@end
