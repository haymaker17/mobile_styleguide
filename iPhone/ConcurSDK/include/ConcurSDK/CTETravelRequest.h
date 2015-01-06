//
//  CTETravelRequest.h
//  ConcurSDK
//
//  Created by Kevin Boutin on 25/07/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//

#import <Foundation/Foundation.h>
#import "CTEDataTypes.h"

@class CTEError;
@class CTEComment;
@class CTEUserAction;

@interface CTETravelRequest : NSObject

@property (copy, nonatomic) CTEDataTypes *UserLoginID;
@property (copy, nonatomic) CTEDataTypes *ApproverLoginID;
@property (copy, nonatomic) CTEDataTypes *RequestID;
@property (copy, nonatomic) CTEDataTypes *ArPolID;
@property (copy, nonatomic) CTEDataTypes *ArPolName;
@property (copy, nonatomic) CTEDataTypes *Name;
@property (copy, nonatomic) CTEDataTypes *Purpose;
@property (copy, nonatomic) CTEDataTypes *CurrencyCode;
@property (copy, nonatomic) CTEDataTypes *CurrencyName;
@property (copy, nonatomic) CTEDataTypes *CreationDate;
@property (copy, nonatomic) CTEDataTypes *HasException;
@property (copy, nonatomic) CTEDataTypes *EverSentBack;
@property (copy, nonatomic) CTEDataTypes *EmployeeName; //EmpName
@property (copy, nonatomic) CTEDataTypes *HeaderFormID;
@property (copy, nonatomic) CTEDataTypes *AllocationFormID;
@property (copy, nonatomic) CTEDataTypes *ApprovalStatusName;
@property (copy, nonatomic) CTEDataTypes *ApprovalStatusCode;
@property (copy, nonatomic) CTEDataTypes *AuthorizedDate;
@property (copy, nonatomic) CTEDataTypes *SubmitDate;
@property (copy, nonatomic) CTEDataTypes *TotalPostedAmount;
@property (copy, nonatomic) CTEDataTypes *TotalApprovedAmount;
@property (copy, nonatomic) CTEDataTypes *TotalRemainingAmount;
@property (copy, nonatomic) CTEDataTypes *ApprovalLimitDate;
@property (copy, nonatomic) CTEDataTypes *AgencyOfficeName;
@property (copy, nonatomic) CTEDataTypes *StartDate;
@property (copy, nonatomic) CTEDataTypes *EndDate;
@property (copy, nonatomic) CTEDataTypes *StartTime;
@property (copy, nonatomic) CTEDataTypes *EndTime;
@property (copy, nonatomic) CTEDataTypes *TransactionDate;
@property (copy, nonatomic) CTEDataTypes *ExtensionOf;
@property (copy, nonatomic) CTEDataTypes *LastModifiedDate;
@property (copy, nonatomic) NSArray *Comments;
@property (copy, nonatomic) NSArray *Exceptions;
@property (copy, nonatomic) NSArray *Entries;
@property (copy, nonatomic) NSArray *CashAdvances;
@property (copy, nonatomic) NSArray *UserActions;
@property (copy, nonatomic) CTEDataTypes *Custom1;
@property (copy, nonatomic) CTEDataTypes *Custom2;
@property (copy, nonatomic) CTEDataTypes *Custom3;
@property (copy, nonatomic) CTEDataTypes *Custom4;
@property (copy, nonatomic) CTEDataTypes *Custom5;
@property (copy, nonatomic) CTEDataTypes *Custom6;
@property (copy, nonatomic) CTEDataTypes *Custom7;
@property (copy, nonatomic) CTEDataTypes *Custom8;
@property (copy, nonatomic) CTEDataTypes *Custom9;
@property (copy, nonatomic) CTEDataTypes *Custom10;
@property (copy, nonatomic) CTEDataTypes *Custom11;
@property (copy, nonatomic) CTEDataTypes *Custom12;
@property (copy, nonatomic) CTEDataTypes *Custom13;
@property (copy, nonatomic) CTEDataTypes *Custom14;
@property (copy, nonatomic) CTEDataTypes *Custom15;
@property (copy, nonatomic) CTEDataTypes *Custom16;
@property (copy, nonatomic) CTEDataTypes *Custom17;
@property (copy, nonatomic) CTEDataTypes *Custom18;
@property (copy, nonatomic) CTEDataTypes *Custom19;
@property (copy, nonatomic) CTEDataTypes *Custom20;
@property (copy, nonatomic) CTEDataTypes *LastComment;

//virtual field to merge all segments type name on one string (localized)
@property (copy, nonatomic) CTEDataTypes *SegmentTypes;

@property (strong, nonatomic) CTEError *CteError;

-(CTEComment *)getLastComment;

-(BOOL)hasPermittedAction:(NSString*)action;
-(CTEUserAction*)getAction:(NSString*)action;

- (id)valueForUndefinedKey:(NSString*)key;

@end
