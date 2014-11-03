//
//  PreSellOptions.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 20/08/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponder.h"
#import "TrainDeliveryData.h"
#import "AffinityProgram.h"
#import "PreSellCustomField.h"
#import "PreSellCustomFieldSelectOption.h"

@interface PreSellOptions : MsgResponder

@property (nonatomic, strong) TrainDeliveryData *trainDeliveryData;
@property (nonatomic, strong) NSMutableArray *creditCards;
@property (nonatomic, strong) NSMutableArray *affinityPrograms;
@property (nonatomic, strong) NSMutableArray *optionItems;
@property (nonatomic, strong) AffinityProgram *defaultProgram;
@property (nonatomic, strong) NSMutableArray *cancellationPolicyLines;
@property (nonatomic) BOOL isRequestSuccessful;
@property (nonatomic) BOOL isCreditCardCvvRequired;

@end
