//
//  ReceiptEditorDataModel.m
//  ConcurMobile
//
//  Created by AJ Cram on 6/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ReceiptEditorDataModel.h"

@interface ReceiptEditorDataModel ()
@property (atomic, strong) ReportData* reportData;
@end

@implementation ReceiptEditorDataModel

- (id)initWithReportData:(ReportData*)reportData
{
    self = [super init];
    if( nil != self )
    {
        self.reportData = reportData;
    }
    
    return self;
}

- (ReportData*) getReportData
{
    return self.reportData;
}

@end