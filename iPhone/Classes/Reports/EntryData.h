//
//  EntryData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormFieldData.h"
#import "CommentData.h"
#import "AttendeeData.h"
#import "ExceptionData.h"
#import "CarConfigData.h"
#import "Taxforms.h"

@interface EntryData : NSObject 
{
	NSString			*approvedAmount, *expName, *expKey,  *hasAllocation, *hasAttendees, *hasAttendeesField, *hasComments, *hasExceptions
						, *isCreditCardCharge, *isPersonalCardCharge, *isItemized, *isPersonal
						,*locationName, *rpeKey, *transactionAmount, *transactionCrnCode, *transactionDate, *vendorDescription, *rptKey, *parentRpeKey;
	
	// Fields used for submit logic.
	NSString			*severityLevel, *hasMobileReceipt, *receiptRequired, *imageRequired, *ereceiptId, *meKey;
	
	// Field used for editing
	NSString			*formKey;
	
	NSMutableArray		*fieldKeys, *commentKeys, *itemKeys, *attendeeKeys;
	NSMutableDictionary	*fields, *comments, *items, *attendees;
	FormFieldData		*field;
	CommentData			*comment;
	EntryData			*item;
	AttendeeData		*attendee;
	ExceptionData		*exception;
	NSMutableArray		*exceptions;
	UIImage				*receiptImage;
	NSString			*receiptImageId;
    NSString            *noShowCount;
    
    NSMutableArray      *atnColumns;  // FormField objects
    
    CarConfigData		*carConfig; // MOB-11022 if rpt currency does not match emp currency, then car config is returned for each entry
    
    NSString            *cctType; // MOB-13616 indicating pre-auth, auth and auth cancel type
}

@property (strong, nonatomic) NSString *approvedAmount;
@property (strong, nonatomic) NSString *expKey;
@property (strong, nonatomic) NSString *expName;
@property (strong, nonatomic) NSString *hasAllocation;
@property (strong, nonatomic) NSString *hasAttendees;
@property (strong, nonatomic) NSString *hasAttendeesField;
@property (strong, nonatomic) NSString *hasComments;
@property (strong, nonatomic) NSString *hasExceptions;
@property (strong, nonatomic) NSString *isCreditCardCharge;
@property (strong, nonatomic) NSString *isPersonalCardCharge;
@property (strong, nonatomic) NSString *isItemized;
@property (strong, nonatomic) NSString *isPersonal;
@property (strong, nonatomic) NSString *locationName;
@property (strong, nonatomic) NSString *rpeKey;
@property (strong, nonatomic) NSString *transactionAmount;
@property (strong, nonatomic) NSString *transactionCrnCode;
@property (strong, nonatomic) NSString *transactionDate;
@property (strong, nonatomic) NSString *vendorDescription;
@property (strong, nonatomic) NSString *rptKey;
@property (strong, nonatomic) NSString *severityLevel;
@property (strong, nonatomic) NSString *hasMobileReceipt;
@property (strong, nonatomic) NSString *hasTravelAllowance;
@property (strong, nonatomic) NSString *taDayKey;
@property (strong, nonatomic) NSString *receiptRequired;
@property (strong, nonatomic) NSString *imageRequired;
@property (strong, nonatomic) NSString *ereceiptId;
@property (strong, nonatomic) NSString *meKey;
@property (strong, nonatomic) NSString *formKey;
@property (strong, nonatomic) NSString *noShowCount;
@property (strong, nonatomic) NSString *cctType;
// MOB-21147
@property (strong, nonatomic) NSString *eReceiptImageId;


@property (strong, nonatomic) NSMutableArray		*fieldKeys;
@property (strong, nonatomic) NSMutableArray		*commentKeys;
@property (strong, nonatomic) NSMutableArray		*attendeeKeys;
@property (strong, nonatomic) NSMutableDictionary	*fields;
@property (strong, nonatomic) NSMutableDictionary	*comments;
@property (strong, nonatomic) NSMutableDictionary	*attendees;
@property (strong, nonatomic) NSMutableArray        *atnColumns;

@property (strong, nonatomic) NSMutableArray		*itemKeys;
@property (strong, nonatomic) NSMutableDictionary	*items;
@property (strong, nonatomic) EntryData				*item;
@property (strong, nonatomic) NSString				*parentRpeKey;

@property (strong, nonatomic) FormFieldData			*field;
@property (strong, nonatomic) CommentData			*comment;
@property (strong, nonatomic) AttendeeData			*attendee;

@property (strong, nonatomic) NSMutableArray		*exceptions;
@property (strong, nonatomic) ExceptionData			*exception;
@property (strong, nonatomic) UIImage				*receiptImage;
@property (strong, nonatomic) NSString				*receiptImageId;
@property (strong, nonatomic) CarConfigData         *carConfig;
@property (strong, nonatomic) TaxForms              *taxforms ;

-(id)initItemize;
-(void)finishField;
-(void)finishComment;
-(void)finishItemization;
-(void)finishAttendee;
-(void)finishException;

-(void)finishAttendeeColumnDef;

-(void)createDefaultAttendeeUsingExpenseTypeVersion:(NSString*)version policyKey:(NSString*)polKey forChild:(BOOL)isForChild;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

-(BOOL) isDetail;
-(BOOL) isChild;
@end
