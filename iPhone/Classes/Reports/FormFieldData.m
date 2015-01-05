//
//  FormFieldData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "FormFieldData.h"
#import "FormatUtils.h"

@implementation FormFieldData
@synthesize ctrlType, dataType, iD, label, fieldValue;
@synthesize listKey, liCode, liKey;
@synthesize required, maxLength, access, searchable;
@synthesize ftCode, parFieldId, parFtCode, parLiKey;
@synthesize hierKey, hierLevel, parHierLevel;
@synthesize isCopyDownSourceForOtherForms, itemCopyDownAction;
@synthesize validationErrMsg,validationExpression, failureMsg , tip;

@synthesize extraDisplayInfo;
@synthesize listChoices;
@synthesize searchableListChoices;
@synthesize defaultValue, cpDownSource, cpDownFormType;
@synthesize isDynamicField, formFieldKey, originalCtrlType;

static NSMutableDictionary* formFieldXmlToPropertyMap = nil;

+ (NSMutableDictionary*) getXmlToPropertyMap
{
	return formFieldXmlToPropertyMap;
}

+ (void)initialize
{
	if (self == [FormFieldData class]) 
	{
        // Perform initialization here.
		formFieldXmlToPropertyMap = [[NSMutableDictionary alloc] init];
        formFieldXmlToPropertyMap[@"Value"] = @"FieldValue";
		formFieldXmlToPropertyMap[@"Label"] = @"Label";
		formFieldXmlToPropertyMap[@"Id"] = @"ID";
		formFieldXmlToPropertyMap[@"DataType"] = @"DataType";
		formFieldXmlToPropertyMap[@"CtrlType"] = @"CtrlType";
		formFieldXmlToPropertyMap[@"ListKey"] = @"ListKey";
		formFieldXmlToPropertyMap[@"LiKey"] = @"LiKey";
		formFieldXmlToPropertyMap[@"LiCode"] = @"LiCode";
		formFieldXmlToPropertyMap[@"Required"] = @"Required";
		formFieldXmlToPropertyMap[@"MaxLength"] = @"MaxLength";
		formFieldXmlToPropertyMap[@"Access"] = @"Access";
        formFieldXmlToPropertyMap[@"Searchable"] = @"Searchable";
		formFieldXmlToPropertyMap[@"ParFtCode"] = @"ParFtCode";
		formFieldXmlToPropertyMap[@"FtCode"] = @"FtCode";
		formFieldXmlToPropertyMap[@"ParLiKey"] = @"ParLiKey";
		formFieldXmlToPropertyMap[@"ParFieldId"] = @"ParFieldId";
		formFieldXmlToPropertyMap[@"IsCopyDownSourceForOtherForms"] = @"IsCopyDownSourceForOtherForms";
		formFieldXmlToPropertyMap[@"ItemCopyDownAction"] = @"ItemCopyDownAction";
		formFieldXmlToPropertyMap[@"CopyDownSource"] = @"CpDownSource";
		formFieldXmlToPropertyMap[@"CopyDownFormType"] = @"CpDownFormType";
		formFieldXmlToPropertyMap[@"DefaultValue"] = @"DefaultValue";
        formFieldXmlToPropertyMap[@"ValidationExpression"] = @"ValidationExpression";
        formFieldXmlToPropertyMap[@"FailureMsg"] = @"FailureMsg";
        formFieldXmlToPropertyMap[@"IsDynamicField"] = @"IsDynamicField";
        formFieldXmlToPropertyMap[@"FfKey"] = @"FormFieldKey";
        formFieldXmlToPropertyMap[@"OriginalCtrlType"] = @"OriginalCtrlType";
	}
}

-(id) init 
{
    self = [super init];
    if (self)
    {
        self.label = @"";   // MOB-5144 Make sure label is never nil
    }
    return self;
}

+(void)copyValuesFromFields:(NSArray*)srcFields toFields:(NSArray*)destFields exceptIds:(NSDictionary*)exceptDict
{
	if (srcFields == nil || destFields == nil)
		return; // nil;
	
	NSMutableDictionary* destDict = [FormFieldData makeFieldDict:destFields];
	
	for (FormFieldData* srcField in srcFields)
	{
		NSString *fieldId = srcField.iD;
		if (fieldId != nil)
		{
			FormFieldData* destField = destDict[fieldId];
			if (destField != nil && [destField.access isEqualToString:@"RW"] && (exceptDict == nil || exceptDict[fieldId] == nil))
			{
				destField.fieldValue = srcField.fieldValue;
			}
			
			// MOB-4481 A hack for integer attendee type field returned from server
			if ([fieldId isEqualToString:@"AtnTypeName"] && destField == nil)
			{
				destField = destDict[@"AtnTypeKey"];
				if (destField.liKey!= nil && [destField.liKey isEqualToString:destField.fieldValue])
				{
					destField.fieldValue = srcField.fieldValue;
				}
			}
		}
	}
	
	//return destDict;
}

+(NSMutableDictionary*) makeFieldDict:(NSArray*)fields
{
	NSUInteger numFields = [fields count];
	__autoreleasing NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithCapacity:(numFields > 0 ? numFields : 1)];
	for (FormFieldData* field in fields)
	{
		NSString *fieldId = field.iD;
		if (fieldId != nil)
		{
			dict[fieldId] = field;
		}
	}
	return dict;
}


- (id)copyWithZone:(NSZone *)zone
{
	FormFieldData* copy = [[FormFieldData allocWithZone:zone] 
						   initField:self.iD label:self.label value:self.fieldValue ctrlType:self.ctrlType dataType:self.dataType];
	
	copy.listKey = self.listKey;
	copy.liCode = self.liCode;
	copy.liKey = self.liKey;
	copy.required = self.required;
	copy.maxLength = self.maxLength;
	copy.access = self.access;
    copy.searchable = self.searchable;
	copy.isCopyDownSourceForOtherForms = self.isCopyDownSourceForOtherForms;
    copy.itemCopyDownAction = self.itemCopyDownAction;
	copy.ftCode = self.ftCode;
	copy.parFieldId = self.parFieldId;
	copy.parFtCode = self.parFtCode;
	copy.parLiKey = self.parLiKey;
	copy.hierKey = hierKey;
	copy.hierLevel = hierLevel;
	copy.parHierLevel = parHierLevel;
	copy.defaultValue = defaultValue;
    copy.cpDownSource = cpDownSource;
    copy.cpDownFormType = cpDownFormType;
    copy.validationExpression = validationExpression;
    copy.failureMsg = failureMsg;
    copy.isDynamicField = isDynamicField;
    copy.formFieldKey = formFieldKey;
    copy.originalCtrlType = originalCtrlType;
	return copy;
}

-(id)initField:(NSString*)id label:(NSString*)lbl value:(NSString*)val ctrlType:(NSString*)cType dataType:(NSString*)dType
{
	self.iD = id;
	self.label = lbl;
	self.fieldValue = val;
	self.ctrlType = cType;
	self.dataType = dType;
	return self;
}

-(NSString*) getServerValue
{
	if (([@"MONEY" isEqualToString:self.dataType] ||
		 [@"NUMERIC" isEqualToString:self.dataType])
		&& [@"edit" isEqualToString:self.ctrlType])
	{
		NSString* serverNumber = [FormatUtils convertDoubleToStringUS:self.fieldValue];
		// server throws an error on .00
        if ([serverNumber isEqualToString:@".00"]) {
            return @"0.00";
        }
        
		return serverNumber;
	}
	else if ([@"INTEGER" isEqualToString:self.dataType] && [@"edit" isEqualToString:self.ctrlType] && ![@"CurrencyName" isEqualToString:self.iD])
	{
		NSString* serverNumber = [FormatUtils convertIntegerToStringUS:self.fieldValue];
		return serverNumber;
	}
	return self.fieldValue;
}

-(NSString*) getFullLabel
{
    if ([self isRequired])
    {
        return [NSString stringWithFormat:@"%@ *", self.label];
    }
    return self.label;
}


#pragma mark Helper Methods for Form Field Display 
-(BOOL) isRequired
{
    return self.required != nil && [@"Y" isEqualToString:self.required];
}
-(BOOL) isEditable
{
    return self.access == nil || [@"RW" isEqualToString:self.access];
}
-(BOOL) isMissingValue
{
    BOOL missingValue = FALSE;
    if ([self isRequired] && [self isEditable])
	{
		if ((![self.fieldValue lengthIgnoreWhitespace] && ![self.iD isEqualToString:@"Attendees"])
            || ([self.iD isEqualToString:@"ExpKey"] && [self.liKey isEqualToString:@"UNDEF"]))
		{
			missingValue = TRUE;
		}
        // MOB-12779: Take out the check of "MONEY" to allows to save the report expese or quick expense with zero amount.
		else if ([self.dataType isEqualToString:@"NUMERIC"])
		{
            NSString* usNum = [self getServerValue];
			double amt = [usNum doubleValue];  // Recognize en-US format
			if (amt == 0.0)
				missingValue = TRUE;
		}
    }
    return missingValue;
}
-(BOOL) isSearchable
{
    return self.searchable != nil && [@"true" isEqualToString:self.searchable];
}
-(BOOL) requiresNumericInput
{
    return ([@"MONEY" isEqualToString:self.dataType] ||[@"NUMERIC" isEqualToString:self.dataType] || [@"INTEGER" isEqualToString:self.dataType]);
}

-(BOOL) needsSecureEntry
{
    return [@"PASSWORD" isEqualToString:self.dataType];
}

// APIs to access extra display info
-(NSString*) getAndValidateExtraInfo:(NSString*)fldId withDataType:(NSString*)dType
{
    if ((fldId != nil && [self.iD isEqualToString:fldId]) || 
        (dType != nil && [self.dataType isEqualToString:dType]))
        return (NSString*) self.extraDisplayInfo;
    return nil;
}
-(NSString*) getPolKeyForExpFldType
{
    return [self getAndValidateExtraInfo:nil withDataType:@"EXPTYPE"]; 
}

-(NSString*) getCrnCodeForMoneyFldType
{
    return [self getAndValidateExtraInfo:nil withDataType:@"MONEY"]; 
}

-(NSString*) getRptKeyForReceiptTypeFld
{
    return [self getAndValidateExtraInfo:@"ReceiptType" withDataType:nil]; 
}

#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:ctrlType	forKey:@"ctrlType"];
	[coder encodeObject:dataType	forKey:@"dataType"];
	[coder encodeObject:iD			forKey:@"iD"];
	[coder encodeObject:label		forKey:@"label"];
	[coder encodeObject:fieldValue	forKey:@"fieldValue"];
	[coder encodeObject:listKey		forKey:@"listKey"];
	[coder encodeObject:liKey		forKey:@"liKey"];
	[coder encodeObject:liCode		forKey:@"liCode"];
	[coder encodeObject:required	forKey:@"required"];
	[coder encodeObject:maxLength		forKey:@"maxLength"];
	[coder encodeObject:access		forKey:@"access"];
    [coder encodeObject:searchable forKey:@"searchable"];
	[coder encodeObject:isCopyDownSourceForOtherForms		forKey:@"isCopyDownSourceForOtherForms"];
	[coder encodeObject:ftCode		forKey:@"ftCode"];
	[coder encodeObject:parFieldId	forKey:@"parFieldId"];
	[coder encodeObject:parFtCode	forKey:@"parFtCode"];
	[coder encodeObject:parLiKey	forKey:@"parLiKey"];
	[coder encodeInt:hierKey		forKey:@"hierKey"];
	[coder encodeInt:hierLevel		forKey:@"hierLevel"];
	[coder encodeInt:parHierLevel	forKey:@"parHierLevel"];
    [coder encodeObject:itemCopyDownAction forKey:@"itemCopyDownAction"];
    [coder encodeObject:validationExpression forKey:@"validationExpression"];
    [coder encodeObject:failureMsg forKey:@"failureMsg"];
    [coder encodeObject:isDynamicField forKey:@"isDynamicField"];
    [coder encodeObject:formFieldKey forKey:@"formFieldKey"];
    [coder encodeObject:originalCtrlType forKey:@"originalCtrlType"];
}

- (id)initWithCoder:(NSCoder *)coder {
    self.ctrlType = [coder decodeObjectForKey:@"ctrlType"];
	self.dataType = [coder decodeObjectForKey:@"dataType"];
	self.iD = [coder decodeObjectForKey:@"iD"];
	self.label = [coder decodeObjectForKey:@"label"];
	self.fieldValue = [coder decodeObjectForKey:@"fieldValue"];
	self.listKey = [coder decodeObjectForKey:@"listKey"];
	self.liKey = [coder decodeObjectForKey:@"liKey"];
	self.liCode = [coder decodeObjectForKey:@"liCode"];
	self.required = [coder decodeObjectForKey:@"required"];
	self.maxLength = [coder decodeObjectForKey:@"maxLength"];
	self.access = [coder decodeObjectForKey:@"access"];
    self.searchable = [coder decodeObjectForKey:@"searchable"];
	self.isCopyDownSourceForOtherForms = [coder decodeObjectForKey:@"isCopyDownSourceForOtherForms"];

	self.ftCode = [coder decodeObjectForKey:@"ftCode"];
	self.parFieldId = [coder decodeObjectForKey:@"parFieldId"];
	self.parFtCode = [coder decodeObjectForKey:@"parFtCode"];
	self.parLiKey = [coder decodeObjectForKey:@"parLiKey"];
	self.hierKey = [coder decodeIntForKey:@"hierKey"];
	self.hierLevel = [coder decodeIntForKey:@"hierLevel"];
	self.parHierLevel = [coder decodeIntForKey:@"parHierLevel"];
    
    self.itemCopyDownAction = [coder decodeObjectForKey:@"itemCopyDownAction"];
    self.validationExpression = [coder decodeObjectForKey:@"validationExpression"];
    self.failureMsg = [coder decodeObjectForKey:@"failureMsg"];
    self.isDynamicField = [coder decodeObjectForKey:@"isDynamicField"];
    self.formFieldKey = [coder decodeObjectForKey:@"formFieldKey"];
    self.originalCtrlType = [coder decodeObjectForKey:@"originalCtrlType"];

    return self;
}

+(BOOL) isLocationFieldId:(NSString*)fieldId
{
    return [fieldId isEqualToString:@"LocName"] || [fieldId isEqualToString:@"LnKey"];
}

+(BOOL) isCurrencyFieldId:(NSString*)fieldId
{
    return [fieldId isEqualToString:@"TransactionCurrencyName"];
}
@end
