//
//  EntityReport.h
//  ConcurMobile
//
//  Created by yiwen on 6/14/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityReport : NSManagedObject {
@private
}
@property (nonatomic, strong) NSString * apsKey;
@property (nonatomic, strong) NSString * reportName;
@property (nonatomic, strong) NSString * apvStatusName;
@property (nonatomic, strong) NSString * totalPostedAmount;
@property (nonatomic, strong) NSString * crnCode;
@property (nonatomic, strong) NSString * hasException;
@property (nonatomic, strong) NSString * employeeName;
@property (nonatomic, strong) NSDate * reportDate;
@property (nonatomic, strong) NSString * rptKey;

@end
