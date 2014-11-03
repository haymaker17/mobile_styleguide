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
@class CTEError;
@class CTETravelRequestComment;

@interface CTETravelRequest : NSObject

@property (copy, nonatomic) NSString *UserLoginId;
@property (copy, nonatomic) NSString *ApproverLoginId;
@property (copy, nonatomic) NSString *RequestId;
@property (copy, nonatomic) NSString *Name;
@property (copy, nonatomic) NSString *Purpose;
@property (copy, nonatomic) NSString *CurrencyCode;
@property (copy, nonatomic) NSString *CreationDate;
@property (copy, nonatomic) NSString *HasException;
@property (copy, nonatomic) NSString *EverSentBack;
@property (copy, nonatomic) NSString *EmployeeName; //EmpName
@property (copy, nonatomic) NSString *HeaderFormKey;
@property (copy, nonatomic) NSString *ApprovalStatusName;
@property (copy, nonatomic) NSString *ApprovalStatusCode;
@property (copy, nonatomic) NSString *AuthorizedDate;
@property (copy, nonatomic) NSString *SubmitDate;
@property (copy, nonatomic) NSString *TotalPostedAmount;
@property (copy, nonatomic) NSString *TotalApprovedAmount;
@property (copy, nonatomic) NSString *TotalRemainingAmount;
@property (copy, nonatomic) NSString *ApprovalLimitDate;
@property (copy, nonatomic) NSString *AgencyOfficeName;
@property (copy, nonatomic) NSString *StartDate;
@property (copy, nonatomic) NSString *EndDate;
@property (copy, nonatomic) NSString *StartTime;
@property (copy, nonatomic) NSString *EndTime;
@property (copy, nonatomic) NSString *ExtensionOf;
@property (copy, nonatomic) NSString *LastModifiedDate;
@property (copy, nonatomic) NSString *CommentCount;
@property (copy, nonatomic) NSArray *CommentsList;
@property (copy, nonatomic) NSString *ExceptionCount;
@property (copy, nonatomic) NSArray *ExceptionsList;
@property (copy, nonatomic) NSString *EntryCount;
@property (copy, nonatomic) NSArray *EntriesList;
@property (copy, nonatomic) NSString *CashAdvanceCount;
@property (copy, nonatomic) NSArray *CashAdvancesList;
@property (copy, nonatomic) NSString *Custom1;
@property (copy, nonatomic) NSString *Custom2;
@property (copy, nonatomic) NSString *Custom3;
@property (copy, nonatomic) NSString *Custom4;
@property (copy, nonatomic) NSString *Custom5;
@property (copy, nonatomic) NSString *Custom6;
@property (copy, nonatomic) NSString *Custom7;
@property (copy, nonatomic) NSString *Custom8;
@property (copy, nonatomic) NSString *Custom9;
@property (copy, nonatomic) NSString *Custom10;
@property (copy, nonatomic) NSString *Custom11;
@property (copy, nonatomic) NSString *Custom12;
@property (copy, nonatomic) NSString *Custom13;
@property (copy, nonatomic) NSString *Custom14;
@property (copy, nonatomic) NSString *Custom15;
@property (copy, nonatomic) NSString *Custom16;
@property (copy, nonatomic) NSString *Custom17;
@property (copy, nonatomic) NSString *Custom18;
@property (copy, nonatomic) NSString *Custom19;
@property (copy, nonatomic) NSString *Custom20;
@property (copy, nonatomic) NSString *LastComment;

//virtual field to merge all segments type name on one string (localized)
@property (copy, nonatomic) NSString *SegmentTypes;

@property (strong, nonatomic) CTEError *CteError;

-(CTETravelRequestComment *)getLastComment;
- (id)valueForUndefinedKey:(NSString *)key;

@end
