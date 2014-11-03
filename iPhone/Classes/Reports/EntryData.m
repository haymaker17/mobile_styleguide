//
//  EntryData.m
//  ConcurMobile
//
//  Created by Paul Kramer on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "EntryData.h"
#import "ExceptionData.h"
#import "ExpenseTypesManager.h"

@implementation EntryData

@synthesize			approvedAmount, expName, hasAllocation, hasAttendees, hasAttendeesField, hasComments, hasExceptions, isCreditCardCharge, isItemized, isPersonal
,locationName, rpeKey, transactionAmount, transactionCrnCode, transactionDate, vendorDescription, rptKey, expKey, comment, field, attendee, attendeeKeys, attendees;
@synthesize isPersonalCardCharge;
@synthesize cctType;

@synthesize	fieldKeys, commentKeys;
@synthesize	fields, comments;

@synthesize	itemKeys,items,item,parentRpeKey, exceptions, exception;
@synthesize severityLevel, hasMobileReceipt, receiptRequired, imageRequired, ereceiptId, meKey,receiptImageId;
@synthesize receiptImage;
@synthesize formKey;
@synthesize noShowCount;

@synthesize atnColumns;
@synthesize carConfig;

@synthesize hasTravelAllowance, taDayKey;

-(id)init
{
    self = [super init];
	if (self)
    {
        self.comments = [[NSMutableDictionary alloc] init];
        self.comment = [[CommentData alloc] init];
        self.commentKeys = [[NSMutableArray alloc] init];
        
        self.fields = [[NSMutableDictionary alloc] init];
        self.field = [[FormFieldData alloc] init];
        self.fieldKeys = [[NSMutableArray alloc] init];
        
        self.items = [[NSMutableDictionary alloc] init];
        self.item = [[EntryData alloc] initItemize];
        self.itemKeys = [[NSMutableArray alloc] init];
        
        self.attendees = [[NSMutableDictionary alloc] init];
        self.attendee = [[AttendeeData alloc] init];
        self.attendeeKeys = [[NSMutableArray alloc] init];
        
        self.exception = [[ExceptionData alloc] init];
        self.exceptions = [[NSMutableArray alloc] init];   
        
        self.atnColumns = [[NSMutableArray alloc] init];
    }
	
	return self;
}

-(id)initItemize
{
    self = [super init];
	if (self)
    {
        self.comments = [[NSMutableDictionary alloc] init];
        self.comment = [[CommentData alloc] init];
        self.commentKeys = [[NSMutableArray alloc] init];
        
        self.fields = [[NSMutableDictionary alloc] init];
        self.field = [[FormFieldData alloc] init];
        self.fieldKeys = [[NSMutableArray alloc] init];
        
        self.attendees = [[NSMutableDictionary alloc] init];
        self.attendee = [[AttendeeData alloc] init];
        self.attendeeKeys = [[NSMutableArray alloc] init];
        
        self.exception = [[ExceptionData alloc] init];
        self.exceptions = [[NSMutableArray alloc] init];

        self.atnColumns = [[NSMutableArray alloc] init];
    }
	return self;
}


-(void)finishException
{
	if (exception != nil) 
	{
		[exceptions addObject:exception];

		self.exception = [[ExceptionData alloc] init]; 
	}
}


-(void)finishComment
{
	if (comment != nil && comment.commentKey) 
	{
		comments[comment.commentKey] = comment;
		[commentKeys addObject:comment.commentKey];

		self.comment = [[CommentData alloc] init];
	}
}


-(void)finishField
{
	if (field != nil && field.iD != nil) 
	{
		fields[field.iD] = field;
		[fieldKeys addObject:field.iD];

		self.field = [[FormFieldData alloc] init];
	}
}

-(void)finishAttendeeColumnDef
{
	if (field != nil && field.iD != nil) 
	{
		[atnColumns addObject:field];
        
		self.field = [[FormFieldData alloc] init];
	}
}

-(void)finishItemization
{
	if (item != nil && item.rpeKey) 
	{
		items[item.rpeKey] = item;
		[itemKeys addObject:item.rpeKey];

		self.item = [[EntryData alloc] initItemize];
	}
}

-(void)finishAttendee
{
	if (attendee != nil && attendee.attnKey != nil) 
	{
        self.attendee.onListForm = YES; // Attendee list on entry form
		attendees[attendee.attnKey] = attendee;
		[attendeeKeys addObject:attendee.attnKey];

		self.attendee = [[AttendeeData alloc] init];
	}
}

-(void)createDefaultAttendeeUsingExpenseTypeVersion:(NSString*)version policyKey:(NSString*)polKey forChild:(BOOL)isForChild
{
	ExpenseTypesManager *expenseTypesManager = [ExpenseTypesManager sharedInstance];
	ExpenseTypeData *et = [expenseTypesManager expenseTypeForVersion:version policyKey:polKey expenseKey:expKey forChild:isForChild];
	
	if ([et.userAsAtnDefault isEqualToString:@"Y"])
	{
		AttendeeData *attendeeRepresentingThisEmployee = [[ExpenseTypesManager sharedInstance] attendeeRepresentingThisEmployee];
		if (attendeeRepresentingThisEmployee != nil && ![self.attendeeKeys containsObject:attendeeRepresentingThisEmployee.attnKey])
		{
			self.attendee = [attendeeRepresentingThisEmployee copy];
            [self.attendee supportFields:self.atnColumns]; // MOB-9773
			[self finishAttendee];
		}
	}
}


-(BOOL) isDetail
{
    return [self.fields count] >0;
}

-(BOOL) isChild
{
    return [self.parentRpeKey length];
}


#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:approvedAmount	forKey:@"approvedAmount"];
	[coder encodeObject:expName	forKey:@"expName"];
	[coder encodeObject:hasAllocation	forKey:@"hasAllocation"];
	[coder encodeObject:hasAttendees	forKey:@"hasAttendees"];
	[coder encodeObject:hasComments	forKey:@"hasComments"];
	[coder encodeObject:hasExceptions	forKey:@"hasExceptions"];
	[coder encodeObject:isCreditCardCharge	forKey:@"isCreditCardCharge"];
	[coder encodeObject:isItemized	forKey:@"isItemized"];
	[coder encodeObject:isPersonal	forKey:@"isPersonal"];
	[coder encodeObject:locationName	forKey:@"locationName"];
	[coder encodeObject:rpeKey	forKey:@"rpeKey"];
	[coder encodeObject:transactionAmount	forKey:@"transactionAmount"];
	[coder encodeObject:transactionCrnCode	forKey:@"transactionCrnCode"];
	[coder encodeObject:transactionDate	forKey:@"transactionDate"];
	[coder encodeObject:vendorDescription	forKey:@"vendorDescription"];
	[coder encodeObject:rptKey	forKey:@"rptKey"];
	[coder encodeObject:expKey	forKey:@"expKey"];
	[coder encodeObject:attendeeKeys	forKey:@"attendeeKeys"];
	[coder encodeObject:attendees	forKey:@"attendees"];
	[coder encodeObject:isPersonalCardCharge	forKey:@"isPersonalCardCharge"];
	[coder encodeObject:fieldKeys	forKey:@"fieldKeys"];
	[coder encodeObject:fields	forKey:@"fields"];
	[coder encodeObject:commentKeys	forKey:@"commentKeys"];
	[coder encodeObject:comments	forKey:@"comments"];
	[coder encodeObject:itemKeys	forKey:@"itemKeys"];
	[coder encodeObject:items	forKey:@"items"];
	[coder encodeObject:parentRpeKey	forKey:@"parentRpeKey"];
	[coder encodeObject:exceptions	forKey:@"exceptions"];
	[coder encodeObject:severityLevel	forKey:@"severityLevel"];
	[coder encodeObject:hasMobileReceipt	forKey:@"hasMobileReceipt"];
	[coder encodeObject:hasTravelAllowance	forKey:@"hasTravelAllowance"];
	[coder encodeObject:taDayKey	forKey:@"taDayKey"];
	[coder encodeObject:receiptRequired	forKey:@"receiptRequired"];
	[coder encodeObject:imageRequired	forKey:@"imageRequired"];
	[coder encodeObject:ereceiptId	forKey:@"ereceiptId"];
    [coder encodeObject:ereceiptId	forKey:@"eReceiptImageId"];
	[coder encodeObject:meKey	forKey:@"meKey"];
	[coder encodeObject:formKey forKey:@"formKey"];
    [coder encodeObject:receiptImageId forKey:@"receiptImageId"];
    [coder encodeObject:noShowCount forKey:@"noShowCount"];
    [coder encodeObject:atnColumns forKey:@"atnColumns"];
    [coder encodeObject:cctType forKey:@"cctType"];
    [coder encodeObject:self.taxforms forKey:@"taxForms"];
//    [coder encodeObject:carConfig forKey:@"carConfig"];
}

- (id)initWithCoder:(NSCoder *)coder {
    self.approvedAmount = [coder decodeObjectForKey:@"approvedAmount"];
	self.expName = [coder decodeObjectForKey:@"expName"];
	self.hasAllocation = [coder decodeObjectForKey:@"hasAllocation"];
	self.hasAttendees = [coder decodeObjectForKey:@"hasAttendees"];
	self.hasComments = [coder decodeObjectForKey:@"hasComments"];
	self.hasExceptions = [coder decodeObjectForKey:@"hasExceptions"];
	self.isCreditCardCharge = [coder decodeObjectForKey:@"isCreditCardCharge"];
	self.isItemized = [coder decodeObjectForKey:@"isItemized"];
	self.isPersonal = [coder decodeObjectForKey:@"isPersonal"];
	self.locationName = [coder decodeObjectForKey:@"locationName"];
	self.rpeKey = [coder decodeObjectForKey:@"rpeKey"];
	self.transactionAmount = [coder decodeObjectForKey:@"transactionAmount"];
	self.transactionCrnCode = [coder decodeObjectForKey:@"transactionCrnCode"];
	self.transactionDate = [coder decodeObjectForKey:@"transactionDate"];
	self.vendorDescription = [coder decodeObjectForKey:@"vendorDescription"];
	self.rptKey = [coder decodeObjectForKey:@"rptKey"];
	self.expKey = [coder decodeObjectForKey:@"expKey"];
	self.attendeeKeys = [coder decodeObjectForKey:@"attendeeKeys"];
	self.attendees = [coder decodeObjectForKey:@"attendees"];
	self.isPersonalCardCharge = [coder decodeObjectForKey:@"isPersonalCardCharge"];
	self.fieldKeys = [coder decodeObjectForKey:@"fieldKeys"];
	self.fields = [coder decodeObjectForKey:@"fields"];
	self.commentKeys = [coder decodeObjectForKey:@"commentKeys"];
	self.comments = [coder decodeObjectForKey:@"comments"];
	self.itemKeys = [coder decodeObjectForKey:@"itemKeys"];
	self.items = [coder decodeObjectForKey:@"items"];
	self.parentRpeKey = [coder decodeObjectForKey:@"parentRpeKey"];
	self.exceptions = [coder decodeObjectForKey:@"exceptions"];
	self.severityLevel = [coder decodeObjectForKey:@"severityLevel"];
	self.hasMobileReceipt = [coder decodeObjectForKey:@"hasMobileReceipt"];
	self.hasTravelAllowance = [coder decodeObjectForKey:@"hasTravelAllowance"];
	self.taDayKey = [coder decodeObjectForKey:@"taDayKey"];
	self.receiptRequired = [coder decodeObjectForKey:@"receiptRequired"];
	self.imageRequired = [coder decodeObjectForKey:@"imageRequired"];
	self.ereceiptId = [coder decodeObjectForKey:@"ereceiptId"];
    self.ereceiptId = [coder decodeObjectForKey:@"eReceiptImageId"];
	self.meKey = [coder decodeObjectForKey:@"meKey"];
	self.formKey = [coder decodeObjectForKey:@"formKey"];
    self.receiptImageId = [coder decodeObjectForKey:@"receiptImageId"];
    self.noShowCount = [coder decodeObjectForKey:@"noShowCount"];
    self.atnColumns = [coder decodeObjectForKey:@"atnColumns"];
    self.cctType = [coder decodeObjectForKey:@"cctType"];
    self.taxforms  = [coder decodeObjectForKey:@"taxForms"];
//    self.carConfig = [coder decodeObjectForKey:@"carConfig"];
    return self;
}


@end
