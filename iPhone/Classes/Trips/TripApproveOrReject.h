//
//  TripApproveOrReject.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 07/05/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponder.h"

@interface TripApproveOrReject : MsgResponder

@property (nonatomic) BOOL isSuccess;
@property (nonatomic, strong) NSString *responseErrorMessage;

@end
