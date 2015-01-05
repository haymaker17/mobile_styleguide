//
//  EditFormHelper.m
//  ConcurMobile
//
//  Created by yiwen on 2/10/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "EditFormHelper.h"
#import "FormFieldData.h"
#import "FormatUtils.h"

@interface EditFormHelper ()
{
    BOOL isHideVdrDescription;
}

@end

@implementation EditFormHelper
@synthesize editForm;

-(id) initWithEditForm:(id<EditFormDataSource>)eform
{
	self.editForm = eform;
	return self;
}

-(BOOL) validateDouble :(NSString*) trimmedVal doubleValue:(double*) dblVal 
{
	NSScanner* scanner = [NSScanner scannerWithString:trimmedVal];
	[scanner setLocale:[NSLocale currentLocale]];
	if ([scanner isAtEnd] == NO)
	{
		if (![scanner scanDouble:dblVal])
		{
			return FALSE;
		}
        else
        {
            if (![scanner isAtEnd] || *dblVal == HUGE_VAL)
			{				
                return FALSE;
            }
        }
    }
    return TRUE;
}

-(BOOL) validateInteger:(NSString*) trimmedVal integerValue:(int*)intVal 
{
	NSScanner* scanner = [NSScanner scannerWithString:trimmedVal];
	[scanner setLocale:[NSLocale currentLocale]];
	
	if ([scanner isAtEnd] == NO)
	{
		if (![scanner scanInteger:intVal])
		{
			return FALSE;
        }
        else
        {
            if (![scanner isAtEnd] || *intVal == INT_MAX)
			{
                return FALSE;
            }
        }
    }
    return TRUE;
}

-(BOOL) validateDouble :(NSString*) trimmedVal required:(BOOL) fReq missingReq:(BOOL*)missingReqFields 
			doubleValue:(double*) dblVal  field:(FormFieldData*) fld
{
	NSScanner* scanner = [NSScanner scannerWithString:trimmedVal];
	[scanner setLocale:[NSLocale currentLocale]];
	if ([scanner isAtEnd] == NO)
	{
		if (![scanner scanDouble:dblVal])
		{
			fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
			return FALSE;
		}
		else 
		{
			// Make sure no garbage character at the end or 0.0 value
			if (0.0 == *dblVal && fReq)
				*missingReqFields = TRUE;
			
			if (![scanner isAtEnd] || *dblVal == HUGE_VAL)
			{				
				if (*dblVal == HUGE_VAL)
					fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_TOO_BIG_ERR_MSG"];
				else
					fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
				
				return FALSE;
			}
		}
	}
	else {
		if (fReq)
			fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
		
		return !fReq;
	}
	
	return TRUE;
}

-(BOOL) validateMoney:(NSString*) trimmedVal required:(BOOL) fReq missingReq:(BOOL*) missingReqFields doubleValue:(double*)dblVal field:(FormFieldData*) fld
{
	NSNumberFormatter *currencyStyle = [[NSNumberFormatter alloc] init];
	[currencyStyle setFormatterBehavior:NSNumberFormatterBehavior10_4];
	[currencyStyle setNumberStyle:NSNumberFormatterDecimalStyle];

    NSNumber *retValue = [currencyStyle numberFromString:trimmedVal];
    if (retValue == nil)
    {
        *dblVal = 0;
        fld.validationErrMsg = [Localizer getLocalizedText:@"NUMERIC_VALIDATION_ERR_MSG"];
    }
    else
    {
        *dblVal = [retValue doubleValue];

        if (*dblVal >= 999999999999999 || *dblVal <= -999999999999999 || *dblVal == HUGE_VAL)
        {
            fld.validationErrMsg = [Localizer getLocalizedText:@"AMOUNT_TOO_BIG_ERR_MSG"];
        
            return FALSE;
        }
        
        if (0.0 == *dblVal && fReq)
            *missingReqFields = TRUE;
    }
    return retValue != nil;
}

-(BOOL) validateInteger:(NSString*) trimmedVal required:(BOOL) fReq missingReq:(BOOL*)missingReqFields 
		   integerValue:(int*)intVal field:(FormFieldData*) fld
{
	fld.validationErrMsg = nil;
	
	NSScanner* scanner = [NSScanner scannerWithString:trimmedVal];
	[scanner setLocale:[NSLocale currentLocale]];
	
	if ([scanner isAtEnd] == NO)
	{
		if (![scanner scanInteger:intVal])
		{
			fld.validationErrMsg = [Localizer getLocalizedText:@"INTEGER_VALIDATION_ERR_MSG"];
			return FALSE;
		}
		else 
		{
			// Make sure no garbage character at the end or 0.0 value
			if (0 == *intVal && fReq)
            {
                //MOB-14437 : Allow user to enter 0 in car mileage for the below fields
                if ([fld.iD isEqualToString:@"OdometerStart"] || [fld.iD isEqualToString:@"OdometerEnd"] || [fld.iD isEqualToString:@"BusinessDistance"] || [fld.iD isEqualToString:@"PersonalDistance"]) {
                    *missingReqFields = FALSE;
                }
                else
                    *missingReqFields = TRUE;
			}
			if (![scanner isAtEnd] || *intVal == INT_MAX)
			{
				if (*intVal == INT_MAX)
					fld.validationErrMsg = [Localizer getLocalizedText:@"INTEGER_TOO_BIG_ERR_MSG"];
				else
					fld.validationErrMsg = [Localizer getLocalizedText:@"INTEGER_VALIDATION_ERR_MSG"];
				
				return FALSE;
			}
			//MOB-14437 - Do not allow negative value values on odo or any other value
            if (0 > *intVal && ([fld.iD isEqualToString:@"OdometerStart"] || [fld.iD isEqualToString:@"OdometerEnd"] || [fld.iD isEqualToString:@"TotalDistance"] || [fld.iD isEqualToString:@"PersonalDistance"] || [fld.iD isEqualToString:@"BusinessDistance"]))
            {
                fld.validationErrMsg = [Localizer getLocalizedText:@"INTEGER_NEGATIVE_ERR_MSG"];
				
				return FALSE;
            }
		}
	}
	else {
		if (fReq)
			fld.validationErrMsg = [Localizer getLocalizedText:@"INTEGER_VALIDATION_ERR_MSG"];
		
		return !fReq;
	}
	
	return TRUE;
}

-(BOOL) validateField:(FormFieldData*) fld missing:(BOOL*) missingReqFields
{
    BOOL result = TRUE;
    NSCharacterSet *wsCharSet = [NSCharacterSet whitespaceCharacterSet];
    
    BOOL fReq = [@"Y" isEqualToString:fld.required];
    if (fld.access == nil || [fld.access isEqualToString:@"RW"])
    {
        fld.validationErrMsg = nil;
        NSString* trimmedVal = [fld.fieldValue stringByTrimmingCharactersInSet:wsCharSet];
        
        if (fReq)
        {
            if ([@"Attendees" isEqualToString:fld.iD]) // Do not localize!  This is an id, it is NOT text shown to the user!
            {
                if (![self validateAttendees])
                {
                    result = FALSE;
                    return result;
                }
            }
            else 
            {
                if (![trimmedVal length]
                    && ![fld.liKey lengthIgnoreWhitespace])
                {
                    if ([fld.iD isEqualToString:@"Name"] ||
                        [fld.iD isEqualToString:@"Purpose"]||  // MOB-5828
                        [fld.iD isEqualToString:@"TransactionDate"]||
                        [fld.iD isEqualToString:@"ExpKey"]||
                        [fld.iD isEqualToString:@"PostedAmount"]||
                        [fld.iD isEqualToString:@"ExchangeRate"]||
                        [fld.iD isEqualToString:@"TransactionAmount"]||
                        [fld.iD isEqualToString:@"TransactionCurrencyName"]||
                        [fld.iD isEqualToString:@"CrnCode"]||
                        [fld.iD isEqualToString:@"PatKey"]||
                        [fld.iD isEqualToString:@"OdometerEnd"]||
                        [fld.iD isEqualToString:@"BusinessDistance"]||
                        [fld.iD isEqualToString:@"CarKey"]||
                        [fld.iD isEqualToString:@"ReceiptType"])
                            result = FALSE;
                    if(![fld.iD isEqualToString:@"VendorDescription"] && isHideVdrDescription )
                        *missingReqFields = YES;
                    return result;
                }
                else if ([fld.iD isEqualToString:@"ExpKey"] && [fld.liKey isEqualToString:@"UNDEF"])
                {
                    result = FALSE;
                    *missingReqFields = YES;
                    return result;
                }
            }
        }
        
        if (([@"NUMERIC" isEqualToString:fld.dataType] && [@"edit" isEqualToString:fld.ctrlType]))
        {
            double dblVal = 0.0;
            if (![self validateDouble:trimmedVal required:fReq missingReq:missingReqFields doubleValue:&dblVal field:(FormFieldData*)fld])
            {
                result = FALSE;
            }
            
        }
        else if ([@"INTEGER" isEqualToString:fld.dataType] && [@"edit" isEqualToString:fld.ctrlType])
        {
        	// MOB-14437 - this ensures that the value is verified for zero and negative values
            int intVal = [trimmedVal intValue];
            if (![self validateInteger:trimmedVal required:fReq missingReq:missingReqFields integerValue:&intVal field:(FormFieldData*)fld])
            {
                result = FALSE;
            }
        }
        
        // MOB-11780: Check if there is a validation expression and validate if there is one
        else if ( ([fld.dataType isEqualToString:@"VARCHAR"] || [fld.dataType isEqualToString:@"CHAR"] || [fld.ctrlType isEqualToString:@"textarea"]) && ([fld.ctrlType isEqualToString:@"edit"]))
        {
            int maxLength = ![fld.maxLength length]?INT_MAX : [fld.maxLength intValue];
            if (trimmedVal != nil && maxLength != INT_MAX)
            {
                if ([trimmedVal length]> maxLength)
                {
                    fld.validationErrMsg = [NSString stringWithFormat:[Localizer getLocalizedText:@"FIELD_MAX_LENGTH_ERR_MSG"], fld.maxLength];
                    result = FALSE;
                }
            }
            
            // if feild is not required and value is empty dont apply rules
            if(!fReq && ![trimmedVal lengthIgnoreWhitespace])
            {
                return TRUE;
            }
            
            NSString *validationRegex = fld.validationExpression;
            // if there is no rule then return true
            if(validationRegex == nil || ![validationRegex lengthIgnoreWhitespace])
            {
                return TRUE;
            }
            
            if( nil != trimmedVal )
            {
                NSRegularExpression *regex = [NSRegularExpression regularExpressionWithPattern:validationRegex options:0 error:NULL];
           
                NSTextCheckingResult *match1 = [regex firstMatchInString:trimmedVal options:0 range:NSMakeRange(0, [trimmedVal length])];
                // If there is no match then regex match failed.
                if(match1 == nil)
                {
                    fld.validationErrMsg = fld.failureMsg;  //failrue message is set in server so no localization
                    return FALSE;
                }
            }
        }
        
    }
    return result;
}

-(BOOL) validateFields:(BOOL*) missingReqFields
{
	BOOL result = TRUE;
    
	if (editForm.allFields != nil)
	{            
		for (FormFieldData* fld in editForm.allFields)
		{
            if ([fld.iD isEqualToString:@"VenLiKey"] && [fld.ctrlType isEqualToString:@"combo"])
                isHideVdrDescription = TRUE;
            
            result = result && [self validateField:fld missing:missingReqFields];
        }
	}
	
	return result;
}

-(BOOL) validateAttendees
{
	return [editForm validateAttendees];
}


-(void) initFields
{
	FormFieldData* ctryCodeFld = nil;
	FormFieldData* ctrySubCodeFld = nil;
	
    BOOL isFormEditable = [editForm canEdit];
    
	// Convert number fields from en-US format to local format for editing
	for (FormFieldData* fld in editForm.allFields)
	{
        // If not editable (e.g. approval), set fields to RO for now.
        // ##APPROVAL EDITING## Need to remove this when we support approval editing
        if (!isFormEditable && (fld.access == nil || [fld.access isEqualToString:@"RW"]))
            fld.access = @"RO";
            
		if ([@"NUMERIC" isEqualToString:fld.dataType] && [@"edit" isEqualToString:fld.ctrlType])
		{
			fld.fieldValue = [FormatUtils formatDouble:fld.fieldValue];
		}
		else if ([@"INTEGER" isEqualToString:fld.dataType] && [@"edit" isEqualToString:fld.ctrlType] && ![fld.iD isEqualToString:@"CurrencyName"])
		{
			fld.fieldValue = [FormatUtils formatInteger:fld.fieldValue];
		}
		else if ([@"TIMESTAMP" isEqualToString:fld.dataType] && [fld.fieldValue isEqualToString:@"0001-01-01T00:00:00"]) 
		{
			fld.fieldValue = nil;
		}
		
		if ([fld.iD isEqualToString:@"CtryCode"])
			ctryCodeFld = fld;
		else if ([fld.iD isEqualToString:@"CtrySubCode"])
			ctrySubCodeFld = fld;
	}
	
	if (ctryCodeFld != nil && ctrySubCodeFld != nil)
	{
		ctrySubCodeFld.parLiKey = ctryCodeFld.liKey;
	}
	
}

@end
