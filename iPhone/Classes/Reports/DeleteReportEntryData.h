//
//  DeleteReportEntryData.h
//  ConcurMobile
//
//  Created by yiwen on 6/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ActiveReportDetailData.h"
#import "ActionStatus.h"

// MOB-4583 Inherit ArchivedResponder protocol from ActiveReportDetailData

@interface DeleteReportEntryData : ActiveReportDetailData 
{
	NSArray                 *rpeKeys;
	ActionStatus			*curStatus;
}

@property (nonatomic, strong) NSArray			*rpeKeys;
@property (nonatomic, strong) ActionStatus		*curStatus;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString*)getReportElementName;

@end
