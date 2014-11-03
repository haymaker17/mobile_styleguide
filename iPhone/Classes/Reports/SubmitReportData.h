//
//  SubmitReportData.h
//  ConcurMobile
//
//  Created by yiwen on 4/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ActiveReportDetailData.h"
#import "ActionStatus.h"
#import "ApproverInfo.h"

// MOB-4583 Inherit ArchivedResponder protocol from ActiveReportDetailData
@interface SubmitReportData : ActiveReportDetailData 
{
    NSString                *approverEmpKey;    // User selected approver empKey
    ApproverInfo            *approver;          // Suggested Approver from server
	ActionStatus			*reportStatus;
}

@property (nonatomic, strong) ActionStatus		*reportStatus;
@property (nonatomic, strong) ApproverInfo		*approver;
@property (nonatomic, strong) NSString          *approverEmpKey;
@property (nonatomic, strong) NSString          *canDrawSubmit;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(void) flushData;
-(NSString*)getReportElementName;

@end
