//
//  ReceiptEditorDataModel.h
//  ConcurMobile
//
//  Created by AJ Cram on 6/17/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportData.h"

@interface ReceiptEditorDataModel : NSObject

- (id)initWithReportData:(ReportData*)reportData;
- (ReportData*) getReportData;

@end