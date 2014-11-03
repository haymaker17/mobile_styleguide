//
//  AgencyAssistanceData.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 14/06/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponder.h"

@interface AgencyAssistanceData : MsgResponder

@property (nonatomic, strong) NSString* dayPhoneNumber;
@property (nonatomic, strong) NSString* nightPhoneNumber;
@property (nonatomic, strong) NSString* preferredCallingOption;
@property (nonatomic, strong) NSString* preferredPhoneNumber;
@property (nonatomic, strong) NSString* recordLocator;
@property (nonatomic, strong) NSString* errorMessage;
@property (nonatomic, strong) NSString* changedItinLocator;

@end
