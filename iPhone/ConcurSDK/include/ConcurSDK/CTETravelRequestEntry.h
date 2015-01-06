//
//  CTETravelRequestEntry.h
//  ConcurSDK
//
//  Created by Kevin Boutin on 18/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
//  ** build phases : to be include in <copy file> **
//

#import <Foundation/Foundation.h>
#import "CTEDataTypes.h"
@class CTETravelRequestDigestCellInfos;
@class CTEComment;

@interface CTETravelRequestEntry : NSObject

@property (copy, nonatomic) CTEDataTypes *ExpenseTypeName;
@property (copy, nonatomic) CTEDataTypes *ExchangeRate;
@property (copy, nonatomic) CTEDataTypes *ForeignAmount; //transactionAmount
@property (copy, nonatomic) CTEDataTypes *ForeignCurrencyName;
@property (copy, nonatomic) CTEDataTypes *ForeignCurrencyCode;
@property (copy, nonatomic) CTEDataTypes *PostedAmount;
@property (copy, nonatomic) CTEDataTypes *ApprovedAmount;
@property (copy, nonatomic) CTEDataTypes *RemainingAmount;
@property (copy, nonatomic) CTEDataTypes *TransactionDate;
@property (copy, nonatomic) CTEDataTypes *LastModifiedDate;
@property (copy, nonatomic) NSArray *Comments;
@property (copy, nonatomic) NSArray *Exceptions;
@property (copy, nonatomic) NSArray *Allocations;
@property (copy, nonatomic) NSArray *Segments;
@property (copy, nonatomic) CTEDataTypes *OrgUnit1;
@property (copy, nonatomic) CTEDataTypes *OrgUnit2;
@property (copy, nonatomic) CTEDataTypes *OrgUnit3;
@property (copy, nonatomic) CTEDataTypes *OrgUnit4;
@property (copy, nonatomic) CTEDataTypes *OrgUnit5;
@property (copy, nonatomic) CTEDataTypes *OrgUnit6;
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
@property (copy, nonatomic) CTEDataTypes *Custom21;
@property (copy, nonatomic) CTEDataTypes *Custom22;
@property (copy, nonatomic) CTEDataTypes *Custom23;
@property (copy, nonatomic) CTEDataTypes *Custom24;
@property (copy, nonatomic) CTEDataTypes *Custom25;
@property (copy, nonatomic) CTEDataTypes *Custom26;
@property (copy, nonatomic) CTEDataTypes *Custom27;
@property (copy, nonatomic) CTEDataTypes *Custom28;
@property (copy, nonatomic) CTEDataTypes *Custom29;
@property (copy, nonatomic) CTEDataTypes *Custom30;
@property (copy, nonatomic) CTEDataTypes *Custom31;
@property (copy, nonatomic) CTEDataTypes *Custom32;
@property (copy, nonatomic) CTEDataTypes *Custom33;
@property (copy, nonatomic) CTEDataTypes *Custom34;
@property (copy, nonatomic) CTEDataTypes *Custom35;
@property (copy, nonatomic) CTEDataTypes *Custom36;
@property (copy, nonatomic) CTEDataTypes *Custom37;
@property (copy, nonatomic) CTEDataTypes *Custom38;
@property (copy, nonatomic) CTEDataTypes *Custom39;
@property (copy, nonatomic) CTEDataTypes *Custom40;

-(CTETravelRequestDigestCellInfos*)getDigestCellInfos;
-(CTEComment *)getLastComment;
- (NSString*)segmentFormID;

- (id)valueForUndefinedKey:(NSString*)key;

@end
