//
//  GovDocumentDetailData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ArchivedResponder.h"
#import "GovDocumentDetail.h"
#import "GovDocAccountCode.h"
#import "GovDocPerdiemTDY.h"
#import "GovDocReasonCode.h"
#import "GovDocExpense.h"
#import "GovDocExpenseCatInfo.h"
#import "GovDocException.h"
#import "GovDocTripTypeCode.h"

@interface GovDocumentDetailData : MsgResponderCommon/*<ArchivedResponder>*/

@property (nonatomic, strong) NSString              *travelerId;
@property (nonatomic, strong) NSString              *docType;
@property (nonatomic, strong) NSString              *docName;
@property BOOL  inExpense;
@property BOOL  inAudit;
@property BOOL  inPerdiem;
@property BOOL  inException;
@property BOOL  inAccountCode;
@property BOOL  inReasonCode;
@property BOOL  inTripTypeListRow;
@property (nonatomic, strong) GovDocumentDetail     *currentDoc;
@property (nonatomic, strong) GovDocAccountCode     *currentAccountCode;
@property (nonatomic, strong) GovDocExpense         *currentExpense;
@property (nonatomic, strong) GovDocPerdiemTDY      *currentPerdiem;
@property (nonatomic, strong) GovDocReasonCode      *currentReasonCode;
@property (nonatomic, strong) GovDocException       *currentException;
@property (nonatomic, strong) GovDocTripTypeCode    *currentTripTypeCode;

@end
