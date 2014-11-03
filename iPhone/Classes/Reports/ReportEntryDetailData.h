//
//  ReportEntryDetailData.h
//  ConcurMobile
//
//  Created by yiwen on 3/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PartialReportDataBase.h"
#import "CarRateTypeData.h"
#import "TaxForms.h"

@interface ReportEntryDetailData : PartialReportDataBase 
{
	NSString		*rpeKey;

    CarRateData				*carRate;
	CarDetailData			*carDetail;
	CarRateTypeData			*carRateType;

}

@property(nonatomic, strong) NSString		*rpeKey;
@property (nonatomic, strong) CarRateData				*carRate;
@property (nonatomic, strong) CarDetailData				*carDetail;
@property (nonatomic, strong) CarRateTypeData			*carRateType;


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(ReportData*) updateReportObject:(ReportData*) rpt;

@end
