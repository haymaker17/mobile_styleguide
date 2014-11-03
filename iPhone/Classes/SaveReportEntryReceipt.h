//
//  SaveReportEntryReceipt.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/12/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ReportEntryDetailData.h"
#import "ActionStatus.h"

@interface SaveReportEntryReceipt : ReportEntryDetailData 
{
	EntryData		*entry;
	ActionStatus	*actionStatus;
    NSString        *receiptImageId;
}

@property(nonatomic, strong) EntryData			*entry;
@property(nonatomic, strong) ActionStatus		*actionStatus;
@property(nonatomic, strong) NSString           *receiptImageId;
-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
	
@end
