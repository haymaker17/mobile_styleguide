//
//  FixedAllowance.h
//  ConcurMobile
//
//  Created by Wes Barton on 2/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AllowanceControl.h"
#import "CXRequest.h"

@class RXMLElement;

@interface FixedAllowance : NSObject

@property BOOL isFirstDay;
@property BOOL isLastDay;
@property BOOL isLocked;
@property BOOL isReadOnly;
@property BOOL markedExcluded;
@property BOOL inUseLock;
@property BOOL overnight;
@property BOOL applyExtendedTripRule;
@property BOOL applyPercentRule;
@property BOOL withinMunicipalArea;

@property BOOL isCollapsed;

@property NSString *taDayKey;
@property NSString *itinKey;
@property NSString *fixedRptKey;
@property NSString *lodgingType;
@property NSString *breakfastProvided;
@property NSString *lunchProvided;
@property NSString *dinnerProvided;
@property NSString *location;

@property NSString *allowanceDateString;
@property (nonatomic, strong) NSDate *allowanceDate;

@property NSString *allowanceAmount;

@property NSDecimalNumber *breakfastTransactionAmount;
@property NSDecimalNumber *breakfastPostedAmount;
@property NSDecimalNumber *breakfastExchangeRate;
@property NSString *breakfastErDirection;

@property NSInteger *breakfastCrnKey;
@property NSString *breakfastCrnCode;
@property NSString *breakfastCrnName;


@property NSString *aboveLimitAmount;
@property NSString *mealsBaseAmount;

- (id)initWithXML:(RXMLElement *)allowance;

- (NSString *)createUpdateSegmentXML:(AllowanceControl *)control;

+ (CXRequest *)getTaFixedAllowances:(NSString *)rptKey;

- (void)recalculateBreakfastPostedAmount;

+ (CXRequest *)getUpdateAllowancesRequest:(NSString *)rptKey;
+ (CXRequest *)getUpdatedFixedAllowanceAmounts:(NSString *)rptKey;

+ (NSString *)parseUpdateAllowancesResult:(NSString *)result;


+ (NSString *)parseGetUpdatedFixedAllowanceAmountsResult:(NSString *)result;
+ (FixedAllowance *)extractUpdatedFixedAllowance:(NSString *)result;

+(CXRequest *)getUpdatedFixedAllowanceAmounts:(FixedAllowance *)allowance rptKey:(NSString *)rptKey allowanceControl:(AllowanceControl *)allowanceControl;
+ (NSString *)composeGetUpdatedFixedAllowanceAmountsXml:(FixedAllowance *)allowance allowanceControl:(AllowanceControl *)allowanceControl;

+ (NSMutableArray *)parseFixedAllowanceXML:(NSString *)result;


/*
<FixedAllowanceRow>
<IsFirstDay>Y</IsFirstDay>
<IsLastDay>N</IsLastDay>
<IsLocked>N</IsLocked>
<IsReadOnly>N</IsReadOnly>
<TaDayKey>24</TaDayKey>
<ItinKey>nNYL70NG$pPPsWPqO5FlaeL1s</ItinKey>
<MarkedExcluded>N</MarkedExcluded>
<FixedRptKey>nfW$srqii4FHMSW$pviAYywnOo</FixedRptKey>
<InUseLock>N</InUseLock>
<AllowanceDate>2013-12-19 00:00</AllowanceDate>
<Overnight>N</Overnight>
<ApplyExtendedTripRule>N</ApplyExtendedTripRule>
<ApplyPercentRule>N</ApplyPercentRule>
<LodgingType>HOTEL</LodgingType>
<WithinMunicipalArea>N</WithinMunicipalArea>
<AllowanceAmount>145.00000000</AllowanceAmount>
<BreakfastProvided>NPR</BreakfastProvided>
<LunchProvided>NPR</LunchProvided>
<DinnerProvided>NPR</DinnerProvided>

<Location>Davis, California</Location>
<AboveLimitAmount>145.00000000</AboveLimitAmount>
<MealsBaseAmount>46.00000000</MealsBaseAmount>
</FixedAllowanceRow>


<BreakfastTransactionAmount>0</BreakfastTransactionAmount>
<BreakfastPostedAmount>0</BreakfastPostedAmount>
<BreakfastCrnKey>1</BreakfastCrnKey>
<BreakfastCrnCode> USD</BreakfastCrnCode>
<BreakfastCrnName> US, Dollar</BreakfastCrnName>
<BreakfastExchangeRate>0.71890000000000</BreakfastExchangeRate>
<BreakfastErDirection> M</BreakfastErDirection>

*/

+ (NSMutableArray *)filterAllowancesByDay:(NSMutableArray *)allowances taDayKey:(NSString *)taDayKey;
@end
