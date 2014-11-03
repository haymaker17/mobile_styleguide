//
//  CreditCardCellData.m
//  ConcurMobile
//
//  Created by Sally Yan on 8/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ReservationCreditCardCellData.h"

@interface ReservationCreditCardCellData()
@property (nonatomic, strong) NSArray *creditCards;
@end

@implementation ReservationCreditCardCellData

-(instancetype)initWithCTECreditCard:(NSArray *)creditCards
{
    self = [super init];
    if (!self) {
        return nil;
    }
    self.cellIdentifier = @"creditCardCell";
    self.cellHeight = 56.0;
    _creditCards = creditCards;
    return  self;
}

-(instancetype)init
{
    self = [self initWithCTECreditCard: nil];
    return self;
}

-(NSArray *)getCreditCards
{
    return self.creditCards;
}

@end
