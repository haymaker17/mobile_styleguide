//
//  EntityReportReceiptInfo.h
//  ConcurMobile
//
//  Created by yiwen on 11/17/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityReportReceiptInfo : NSManagedObject

@property (nonatomic, strong) NSString * rptKey;
@property (nonatomic, strong) NSString * rpeKey;
@property (nonatomic, strong) NSString * imagePath;
@property (nonatomic, strong) NSDate * dateLastModified;
@property (nonatomic, strong) NSString * receiptId;

@end
