//
//  GovDocumentDetail.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocumentDetail : NSObject

@property (nonatomic, strong) NSDate                *tripEndDate;
@property (nonatomic, strong) NSString              *travelerId;
@property (nonatomic, strong) NSString              *docType;
@property (nonatomic, strong) NSString              *docName;
@property (nonatomic, strong) NSString              *purposeCode;
@property (nonatomic, strong) NSString              *approveLabel;
@property (nonatomic, strong) NSString              *docTypeLabel;
@property (nonatomic, strong) NSDecimalNumber       *totalExpCost;
@property (nonatomic, strong) NSDate                *tripBeginDate;
@property (nonatomic, strong) NSString              *gtmDocType;
@property (nonatomic, strong) NSString              *travelerName;
@property (nonatomic, strong) NSNumber              *needsStamping;

@property (nonatomic, strong) NSNumber              *authForVch;

@property (nonatomic, strong) NSString              *currentStatus;
@property (nonatomic, strong) NSString              *tANumber;

@property (nonatomic, strong) NSDecimalNumber       *emissionsLbs;
@property (nonatomic, strong) NSDecimalNumber       *totalEstCost;
@property (nonatomic, strong) NSDecimalNumber       *nonReimbursableAmount;
@property (nonatomic, strong) NSDecimalNumber       *advAmtRequested;
@property (nonatomic, strong) NSDecimalNumber       *advApplied;
@property (nonatomic, strong) NSDecimalNumber       *payToChargeCard;
@property (nonatomic, strong) NSDecimalNumber       *payToTraveler;
@property (nonatomic, strong) NSString              *comments;

@property (nonatomic, strong) NSMutableArray        *accountCodes;
@property (nonatomic, strong) NSMutableArray        *reasonCodes;
@property (nonatomic, strong) NSMutableArray        *expenses;
@property (nonatomic, strong) NSMutableArray        *exceptions;
@property (nonatomic, strong) NSMutableArray        *perdiemTDY;
@property (nonatomic, strong) NSMutableArray        *tripTypeCodes;

@property (nonatomic, strong) NSNumber              *auditPassed;
@property (nonatomic, strong) NSNumber              *auditFailed;

@property (nonatomic, strong) NSString              *receiptId;

@property BOOL requireTypeCode;
@end
