//
//  AttendeeData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/2/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormFieldData.h"
#import "LoadAttendeeForm.h"

@interface AttendeeData : NSObject <NSCopying>
{
	NSString			*amount;
	BOOL				isAmountEdited;
	int					instanceCount;

	NSString			*atnTypeKey;
	NSString			*attnKey;
	NSString			*versionNumber;
	NSString			*currentVersionNumber;
    
	NSMutableArray		*fieldKeys;
	NSMutableDictionary	*fields;
	FormFieldData		*field;
    
    // Whether the form fields are from attendee detail view form (used in list)
    BOOL                onListForm; // YES-the fields only contains id/value/liKey, to be used in attendee list widget or search results.
}

@property (strong, nonatomic) NSString			*amount;
@property (assign, nonatomic) BOOL				isAmountEdited;
@property (assign, nonatomic) int				instanceCount;

@property (strong, nonatomic) NSString			*atnTypeKey;
@property (strong, nonatomic) NSString			*attnKey;
@property (strong, nonatomic) NSString			*versionNumber;
@property (strong, nonatomic) NSString			*currentVersionNumber;

@property (strong, nonatomic) NSMutableArray		*fieldKeys;
@property (strong, nonatomic) NSMutableDictionary	*fields;
@property (strong, nonatomic) FormFieldData			*field;

@property (weak, readonly, nonatomic) NSString			*firstName;
@property (weak, readonly, nonatomic) NSString			*lastName;
@property BOOL  onListForm;

+ (void) divideAmountAmongAttendees:(NSArray*)allAttendees noShows:(int)noShowCount amount:(NSDecimalNumber*)totalAmount crnCode:(NSString*)crnCode;
+ (int) countAttendeeInstances:(NSArray*)attendees;
+ (NSDecimalNumber*) getNoShowsAmount:(NSArray*)allAttendees amount:(NSDecimalNumber*)totalAmount;

+ (AttendeeData*) newAttendeeFromAttendeeForm:(LoadAttendeeForm*)form initialValues:(NSDictionary*)valuesDict;

-(void)finishField;

-(id)init;
- (NSString*) getNullableValueForFieldId:(NSString*)iD;
- (NSString*) getNonNullableValueForFieldId:(NSString*)iD;
- (void) setFieldId:(NSString*)iD value:(NSString*)fieldValue;
-(NSString*) getFullName;
-(NSString*) getAttendeeTypeName;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

// Convert Attendee edit form to attendee list detail form
-(void)mergeFields:(AttendeeData*) atnSrc;
-(void) supportFields:(NSArray*) atnColumns;

@end
