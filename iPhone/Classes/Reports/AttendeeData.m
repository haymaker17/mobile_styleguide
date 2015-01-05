//
//  AttendeeData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 4/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AttendeeData.h"
#import "FormFieldData.h"
#import "MCLogging.h"
#import "FormatUtils.h"
#import "math.h"

@implementation AttendeeData

@synthesize		amount;
@synthesize		isAmountEdited;
@synthesize		instanceCount;

@synthesize		atnTypeKey;
@synthesize		attnKey;
@synthesize		versionNumber;
@synthesize		currentVersionNumber;

@synthesize		fieldKeys;
@synthesize		fields;
@synthesize		field;

@dynamic		firstName;
@dynamic		lastName;

@synthesize     onListForm;

+ (NSDecimalNumber*) getNoShowsAmount:(NSArray*)allAttendees amount:(NSDecimalNumber*)totalAmount
{
    NSDecimalNumber *amountToDivide = [NSDecimalNumber decimalNumberWithDecimal:[totalAmount decimalValue]];
	
	for (AttendeeData *eachAttendee in allAttendees)
	{
        NSDecimalNumber *attendeeAmount = [FormatUtils decimalNumberFromServerString:eachAttendee.amount];
        amountToDivide = [amountToDivide decimalNumberBySubtracting:attendeeAmount];
	}
    return amountToDivide;
}


+ (void) divideAmountAmongAttendees:(NSArray*)allAttendees noShows:(int)noShows amount:(NSDecimalNumber*)totalAmount crnCode:(NSString*)crnCode
{
    
    
    // Note that the variable names say pennies, but in non-dollar currencies they're the
    // smallest denomination subunit, which may not be 100 per unit.
    // CFNumberFormatterGetDecimalInfoForCurrencyCode) will give us the number of decimal places.
    //
    int numPenniesPerDollar = 1;
    short numDecimalPlaces = 0;
    
    int32_t defaultFractionDigits = 0;
    double discardRoundingIncrement = 0.0;
    Boolean gotDecimalInfo = false;

    // MOB-14611 - some defensive code here to check if currency code is nil. if currency code is nil for any reason the app will crash due to CFNumberFormatter
    // As of now currency code is not expected to be nil
    if([crnCode lengthIgnoreWhitespace])
    {
        CFStringRef cfCrnCode = (__bridge CFStringRef)crnCode;
        gotDecimalInfo = CFNumberFormatterGetDecimalInfoForCurrencyCode(cfCrnCode, &defaultFractionDigits, &discardRoundingIncrement);
    }
    
    if (gotDecimalInfo)
    {
        numDecimalPlaces = defaultFractionDigits;
        numPenniesPerDollar = (int)pow(10, numDecimalPlaces);
    }
    
    NSDecimalNumber *dTotalPennies = [totalAmount decimalNumberByMultiplyingByPowerOf10:numDecimalPlaces];
    int totalNumPennies = (int)[dTotalPennies doubleValue];
    
    // We need to do all our calculations with positive amounts, otherwise there will be rounding
    // errors.  So remember if it's negative, then use positive numbers.
       // Here's the starting amount to work with. fabsf is absolute value function
    bool isNegativeAmount = (totalNumPennies < 0);
    totalNumPennies = abs(totalNumPennies);
    
 
	
	

	// Walk through all the attendees; if their amount has been edited,
	// subtract that from the total and ignore them. Otherwise, track
    // them for allocation below.
    NSMutableArray *attendees = [[NSMutableArray alloc] init];
	for (AttendeeData *eachAttendee in allAttendees)
	{
		if (eachAttendee.isAmountEdited && eachAttendee.amount != nil)
		{
			totalNumPennies -= ([eachAttendee.amount doubleValue] * numPenniesPerDollar);
		}
		else
		{
			[attendees addObject:eachAttendee];
		}
	}
	
    // Check to see if any work has to be done.
	if ([attendees count] > 0)
	{
        // Get total number of attendees (an Attendee object can represent multiple actual people).
        int totalAttendeeInstances = [AttendeeData countAttendeeInstances: attendees];
        
        
        // Apparently there can be 'absentees', that is, people who should be allocated money
        // but are not in the list. This will likely cause the report to have an exception
        // because the total amount won't be equal to that allocated to attendees.
        // I presume this is either expected or handled elsewhere.
        totalAttendeeInstances += noShows;
        
        // Calculate the base 'penny' distribution for each peson, plus the remainder to be 
        // distributed across the first n people.
        int baseDistribution = totalNumPennies / totalAttendeeInstances;
        int remainderDistribution = totalNumPennies % totalAttendeeInstances;
    
        
        // build the printf specification for the fractional part, used below
        NSString *fracFormat = [NSString stringWithFormat:@"%%0%dd", numDecimalPlaces];
       
    
              
        // Finally: allocate base amount plus 1 remainder to each instance.
		for (int i = 0; i < [attendees count]; i++)
		{
			AttendeeData *atn = attendees[i];
            int attendeeInstances = atn.instanceCount;
            
            // The base is easy.
            int numPenniesForAttendee = baseDistribution * attendeeInstances;
            
            // Remainder is one per instance, but we could run out.
            int remainderAmountForAttendee = MIN(remainderDistribution, attendeeInstances);
            remainderDistribution -= remainderAmountForAttendee;
            
            // Total up allocation for attendee.
            numPenniesForAttendee += remainderAmountForAttendee;
            
            // Save in the amount property. It's wrong to have it a string, but
            // we'll address that later. I don't want to convert the amount to 
            // double then format the double, because I don't know what kind of
            // rounding will happen
            
            int wholePart = numPenniesForAttendee / numPenniesPerDollar;
            int fracPart = numPenniesForAttendee % numPenniesPerDollar;
            
            NSMutableString *strAmt = [NSMutableString stringWithCapacity:50];
            
            if (isNegativeAmount)
                [strAmt appendString:@"-"];
            
            [strAmt appendFormat:@"%d", wholePart];
            if (numDecimalPlaces > 0)
            {
                
                [strAmt appendString:@"."];
                [strAmt appendFormat:fracFormat, fracPart];
            }
            
            atn.amount = [strAmt copy];

		}
	}
	
}




+(int) countAttendeeInstances:(NSArray*)attendees
{
	int numInstances = 0;
	
	for (AttendeeData *attendee in attendees)
	{
		numInstances += attendee.instanceCount;
	}
	
	return numInstances;
}

+ (AttendeeData*) newAttendeeFromAttendeeForm:(LoadAttendeeForm*)form initialValues:(NSDictionary*)valuesDict
{
	if (form == nil || form.atnTypeKey == nil || form.fields == nil)
	{
		int fieldCount = ((form == nil || form.fields == nil) ? -1 : (int)[form.fields count]);
		[[MCLogging getInstance] log:[NSString stringWithFormat:@"AttendeeData.newAttendeeFromAttendeeForm: failed to create attende form, atnTypeKey = %@, field count: %i ", form.atnTypeKey, fieldCount] Level:MC_LOG_DEBU];
		return nil;
	}
	
	AttendeeData *newAttendee = [[AttendeeData alloc] init];
		 
	newAttendee.atnTypeKey = form.atnTypeKey;
	
	for (FormFieldData *field in form.fields)
	{
		if ((newAttendee.fields)[field.iD] == nil)
		{
			FormFieldData *copyOfFormField = [field copy];
			
			if (valuesDict != nil)
			{
				NSString *value = valuesDict[copyOfFormField.iD];
				if (value != nil)
				{
					copyOfFormField.fieldValue = value;
				}
			}
			
			[newAttendee.fieldKeys addObject:copyOfFormField.iD];
			(newAttendee.fields)[copyOfFormField.iD] = copyOfFormField;
			
		}
	}
	
	return newAttendee;
}

-(id)init
{
    self = [super init];
	if (self)
    {
	
        // It is important to create the following field-related objects *before* setting any fields.
        self.fields = [[NSMutableDictionary alloc] init];
        self.field = [[FormFieldData alloc] init];
        self.fieldKeys = [[NSMutableArray alloc] init];

        amount = 0;
        isAmountEdited = NO;
        instanceCount = 1;
    }
	return self;
}

-(void)finishField
{
	if (field != nil) 
	{
		fields[field.iD] = field;
		[fieldKeys addObject:field.iD];

		self.field = [[FormFieldData alloc] init];
	}
}

- (NSString*) firstName	// Dynamic readonly property
{
	return [self getNonNullableValueForFieldId:@"FirstName"];
}

- (NSString*) lastName	// Dynamic readonly property
{
	return [self getNonNullableValueForFieldId:@"LastName"];
}

- (NSString*) getNullableValueForFieldId:(NSString*)iD
{
	FormFieldData *fld = fields[iD];
	
	NSString *fieldValue = nil;
	if (fld != nil)
	{
		fieldValue = fld.fieldValue;
	}
	return fieldValue;
}

- (NSString*) getNonNullableValueForFieldId:(NSString*)iD
{
	NSString *fieldValue = [self getNullableValueForFieldId:iD];
	return (fieldValue == nil ? @"" : fieldValue);
}

- (void) setFieldId:(NSString*)iD value:(NSString*)fieldValue
{
	FormFieldData *fld = fields[iD];
	if (fld == nil)
	{
		fld = [[FormFieldData alloc] init];
		[fieldKeys addObject:iD];
		fields[iD] = fld;
	}
	fld.iD = iD;
	fld.fieldValue = fieldValue;
}


-(NSString*) getFullName:(BOOL)lastNameFirst
{

	NSString *fullName = nil;
	
	FormFieldData *firstNameField = fields[@"FirstName"];	// Do not localize!  This is the name of a key.
	FormFieldData *lastNameField = fields[@"LastName"];	// Do not localize!  This is the name of a key.
	
	NSString *first = (firstNameField == nil ? nil : [firstNameField fieldValue]);
	NSString *last = (lastNameField == nil ? nil : [lastNameField fieldValue]);
	
	if (first != nil && [first length] > 0)
	{
		if (last != nil && [last length] > 0)
		{
            if (lastNameFirst)
                fullName = [NSString stringWithFormat:@"%@, %@", last, first];
            else
                fullName = [NSString stringWithFormat:@"%@ %@", first, last];
		}
		else
		{
			fullName = first;
		}
	}
	else
	{
		if (last != nil)
		{
			fullName = last;
		}
		else
		{
			fullName = @"";
		}
	}
						  
	return fullName;
}

-(NSString*) getFullName
{
    return [self getFullName:NO];
}


-(NSString*) getAttendeeTypeName
{
	FormFieldData *attendeeTypeField = fields[@"AtnTypeKey"];	// Do not localize!  This is the name of a key.
	NSString *fieldValue = (attendeeTypeField == nil ? nil : [attendeeTypeField fieldValue]);
	return (fieldValue == nil ? @"" : fieldValue);
}

- (id)copyWithZone:(NSZone *)zone
{
	AttendeeData* copy = [AttendeeData allocWithZone:zone];
	
	copy.amount = self.amount;
	copy.isAmountEdited = self.isAmountEdited;
	copy.instanceCount = self.instanceCount;
	copy.attnKey = self.attnKey;
	copy.atnTypeKey = self.atnTypeKey;
	copy.versionNumber = self.versionNumber;
	copy.currentVersionNumber = self.currentVersionNumber;
	copy.onListForm = self.onListForm;
    
	if (self.fieldKeys != nil)
		copy.fieldKeys = [[NSMutableArray allocWithZone:zone] initWithArray:self.fieldKeys];
	
	if (self.fields != nil)
	{
		copy.fields = [[NSMutableDictionary allocWithZone:zone] init];
		NSArray* keys = self.fields.allKeys;	// Theoretically this is the same as self.fieldKeys, but just in case...
		for (NSString* fieldKey in keys)
		{
			FormFieldData *origField = (self.fields)[fieldKey];
			FormFieldData *copyOfField = [origField copyWithZone:zone];
			(copy.fields)[fieldKey] = copyOfField;
		}
	}
	
	if (self.field != nil)
		copy.field = [self.field copyWithZone:zone];
	
	return copy;
}

// Copy over new values from sourceFields, to those values of existing fields.
-(void)mergeFields:(AttendeeData*) atnSrc
{
//	copy.amount = self.amount;
//	copy.isAmountEdited = self.isAmountEdited;
//	copy.instanceCount = self.instanceCount;
//	copy.attnKey = self.attnKey;
//	copy.atnTypeKey = self.atnTypeKey;
	self.versionNumber = atnSrc.versionNumber;
	self.currentVersionNumber = atnSrc.currentVersionNumber;
    
	if (self.fields != nil)
	{
		NSArray* keys = self.fields.allKeys;
		for (NSString* fieldKey in keys)
		{
			FormFieldData *origField = (self.fields)[fieldKey];
			FormFieldData *updatedField = (atnSrc.fields)[fieldKey];
            
            if (updatedField != nil)
            {
                // Copy over liKey, liCode, value
                origField.liKey = updatedField.liKey;
                origField.fieldValue = updatedField.fieldValue;
            }
		}

        FormFieldData   *fullNameFld = (self.fields)[@"AttendeeName"];
        if (fullNameFld != nil)
        {
            NSString        *lastName = [atnSrc lastName];
            NSString        *firstName = [atnSrc firstName];
            
            if (![self.lastName isEqualToString:lastName] || ![firstName isEqualToString:self.firstName])
                fullNameFld.fieldValue = [atnSrc getFullName:YES];
        }
	}
}


-(void) supportFields:(NSArray*) atnColumns
{   // MOB-8699 - TODO call MWS to get new fields
    for (FormFieldData* fld in atnColumns)
    {
        FormFieldData   *curFld = (self.fields)[fld.iD];
        if (curFld == nil)
        {
            curFld = [fld copyWithZone:nil];
            [self.fieldKeys addObject:curFld.iD];
            (self.fields)[curFld.iD] = curFld;
            if ([fld.iD isEqualToString:@"AttendeeName"])
            {
                curFld.fieldValue = [self getFullName:YES];
            }
            else if ([fld.iD isEqualToString:@"AtnTypeKey"])
            {
                curFld.fieldValue = [self getNonNullableValueForFieldId:@"AtnTypeName"];
                curFld.liKey = self.atnTypeKey;
            }
        }
    }
}



#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
	
	[coder encodeObject:amount forKey:@"amount"];
	[coder encodeBool:isAmountEdited forKey:@"isAmountEdited"];
	[coder encodeInt:instanceCount forKey:@"instanceCount"];
	[coder encodeObject:atnTypeKey forKey:@"atnTypeKey"];
	[coder encodeObject:attnKey forKey:@"attnKey"];
	[coder encodeObject:versionNumber forKey:@"versionNumber"];
	[coder encodeObject:currentVersionNumber forKey:@"currentVersionNumber"];
	[coder encodeObject:fieldKeys forKey:@"fieldKeys"];
	[coder encodeObject:fields forKey:@"fields"];
}

- (id)initWithCoder:(NSCoder *)coder {
	
	self.amount = [coder decodeObjectForKey:@"amount"];
	self.isAmountEdited = [coder decodeBoolForKey:@"isAmountEdited"];
	self.instanceCount = [coder decodeIntForKey:@"instanceCount"];
	self.atnTypeKey = [coder decodeObjectForKey:@"atnTypeKey"];
	self.attnKey = [coder decodeObjectForKey:@"attnKey"];
	self.versionNumber = [coder	decodeObjectForKey:@"versionNumber"];
	self.currentVersionNumber = [coder decodeObjectForKey:@"currentVersionNumber"];
	self.fieldKeys = [coder decodeObjectForKey:@"fieldKeys"];
	self.fields = [coder decodeObjectForKey:@"fields"];
    return self;
}


@end
